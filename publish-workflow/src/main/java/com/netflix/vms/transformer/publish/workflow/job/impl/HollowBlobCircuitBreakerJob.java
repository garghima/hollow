package com.netflix.vms.transformer.publish.workflow.job.impl;

import static com.netflix.vms.transformer.common.io.TransformerLogTag.CircuitBreaker;
import static com.netflix.vms.transformer.common.io.TransformerLogTag.TransformCycleFailed;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.apache.commons.lang.StringUtils;

import com.netflix.hollow.read.engine.HollowReadStateEngine;
import com.netflix.hollow.util.SimultaneousExecutor;
import com.netflix.servo.monitor.DynamicCounter;
import com.netflix.vms.transformer.publish.workflow.HollowBlobDataProvider;
import com.netflix.vms.transformer.publish.workflow.PublishWorkflowContext;
import com.netflix.vms.transformer.publish.workflow.circuitbreaker.CertificationSystemCircuitBreaker;
import com.netflix.vms.transformer.publish.workflow.circuitbreaker.DuplicateDetectionCircuitBreaker;
import com.netflix.vms.transformer.publish.workflow.circuitbreaker.HollowCircuitBreaker;
import com.netflix.vms.transformer.publish.workflow.circuitbreaker.HollowCircuitBreaker.CircuitBreakerResult;
import com.netflix.vms.transformer.publish.workflow.circuitbreaker.HollowCircuitBreaker.CircuitBreakerResults;
import com.netflix.vms.transformer.publish.workflow.circuitbreaker.SnapshotSizeCircuitBreaker;
import com.netflix.vms.transformer.publish.workflow.circuitbreaker.TopNViewShareAvailabilityCircuitBreaker;
import com.netflix.vms.transformer.publish.workflow.circuitbreaker.TypeCardinalityCircuitBreaker;
import com.netflix.vms.transformer.publish.workflow.job.CircuitBreakerJob;

public class HollowBlobCircuitBreakerJob extends CircuitBreakerJob {

	private final HollowBlobDataProvider hollowBlobDataProvider;
    private final HollowCircuitBreaker circuitBreakerRules[];

    private final boolean circuitBreakersDisabled;

    public HollowBlobCircuitBreakerJob(PublishWorkflowContext ctx, long cycleVersion, File snapshotFile, File deltaFile, File reverseDeltaFile, HollowBlobDataProvider hollowBlobDataProvider) {
        super(ctx, ctx.getVip(), cycleVersion, snapshotFile, deltaFile, reverseDeltaFile);
        this.hollowBlobDataProvider = hollowBlobDataProvider;

        this.circuitBreakerRules = createCircuitBreakerRules(ctx, cycleVersion, snapshotFile.length());
        
        this.circuitBreakersDisabled = !ctx.getConfig().isCircuitBreakersEnabled();
    }
    
	public static HollowCircuitBreaker[] createCircuitBreakerRules(PublishWorkflowContext ctx, long cycleVersion, long snapshotFileLength) {
		return new HollowCircuitBreaker[] {
                new DuplicateDetectionCircuitBreaker(ctx, cycleVersion),
                new CertificationSystemCircuitBreaker(ctx, cycleVersion),
                new CertificationSystemCircuitBreaker(ctx, cycleVersion, 100),
                new CertificationSystemCircuitBreaker(ctx, cycleVersion, 75),
                new CertificationSystemCircuitBreaker(ctx, cycleVersion, 50),
                new TypeCardinalityCircuitBreaker(ctx, cycleVersion, "NamedCollectionHolder"),
                new TypeCardinalityCircuitBreaker(ctx, cycleVersion, "CompleteVideo"),
                new TypeCardinalityCircuitBreaker(ctx, cycleVersion, "PackageData"),
                new TypeCardinalityCircuitBreaker(ctx, cycleVersion, "StreamData"),
                new TypeCardinalityCircuitBreaker(ctx, cycleVersion, "Artwork"),
                new TypeCardinalityCircuitBreaker(ctx, cycleVersion, "OriginServer"),
                new TypeCardinalityCircuitBreaker(ctx, cycleVersion, "DrmKey"),
                new TypeCardinalityCircuitBreaker(ctx, cycleVersion, "WmDrmKey"),
                new TypeCardinalityCircuitBreaker(ctx, cycleVersion, "GlobalPerson"),
                new SnapshotSizeCircuitBreaker(ctx, cycleVersion, snapshotFileLength),
                new TopNViewShareAvailabilityCircuitBreaker(ctx, cycleVersion),
        };
	}

    @Override
    protected boolean executeJob() {
        try {
            // @TODO Need to move this to separate job - CBs and PBM depends on this updateData
            hollowBlobDataProvider.updateData(snapshotFile, deltaFile, reverseDeltaFile);

            if (circuitBreakersDisabled) {
                ctx.getLogger().warn(CircuitBreaker, "Hollow/Master Circuit Breaker is disabled!");
                return true;
            }

            boolean allDataValid = validateAllData();

            if (!allDataValid) incrementAlertCounter();

            logResult(allDataValid);

            return allDataValid;
        } catch (Exception e) {
            logResult(false);
            ctx.getLogger().error(CircuitBreaker, e.getMessage(), e);
            incrementAlertCounter();
            e.printStackTrace();
            return false;
        }
    }

    private boolean validateAllData() {

        final HollowReadStateEngine stateEngine = hollowBlobDataProvider.getStateEngine();

        SimultaneousExecutor executor = new SimultaneousExecutor();

        List<Future<CircuitBreakerResults>> resultFutures = new ArrayList<Future<CircuitBreakerResults>>();

        for(final HollowCircuitBreaker rule : circuitBreakerRules) {
            FutureTask<CircuitBreakerResults> cbJob = new FutureTask<CircuitBreakerResults>(new Callable<CircuitBreakerResults>() {
                @Override
                public CircuitBreakerResults call() throws Exception {
                    return rule.run(stateEngine);
                }
            });

            resultFutures.add(cbJob);
            executor.execute(cbJob);
        }

        try {
            executor.awaitSuccessfulCompletion();

            boolean isAllDataValid = true;

            for(Future<CircuitBreakerResults> future : resultFutures) {
                CircuitBreakerResults results = future.get();

                for(CircuitBreakerResult result : results) {
                    if(!StringUtils.isEmpty(result.getMessage())) {
                        if(result.isPassed())
                            ctx.getLogger().info(CircuitBreaker, result.getMessage());
                        else
                            ctx.getLogger().error(CircuitBreaker, result.getMessage());
                    }

                    isAllDataValid = isAllDataValid && result.isPassed();
                }
            }

            if (isAllDataValid) {
                for(HollowCircuitBreaker rule : circuitBreakerRules)
                    rule.saveSuccessSizesForCycle(cycleVersion);
            }

            return isAllDataValid;
        } catch(Exception e) {
            /// convert to a RuntimeException and let the publish workflow framework deal with the failure.
            throw new RuntimeException(e);
        }
    }

    private void logResult(boolean isAllDataValid) {
        String logMessage = "Hollow data validation completed for version " + cycleVersion + " vip: " + ctx.getVip();

        if (!isAllDataValid) {
            ctx.getLogger().error(Arrays.asList(TransformCycleFailed, CircuitBreaker), "Circuit Breakers Failed: {}", logMessage);
        } else {
            ctx.getLogger().info(CircuitBreaker, "Circuit Breakers Successful: {}", logMessage);
        }
    }

    private void incrementAlertCounter() {
        DynamicCounter.increment("vms.hollow.validation.failed");
    }
}