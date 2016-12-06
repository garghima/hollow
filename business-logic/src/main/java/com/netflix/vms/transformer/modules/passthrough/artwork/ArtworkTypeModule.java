package com.netflix.vms.transformer.modules.passthrough.artwork;


import com.netflix.hollow.write.objectmapper.HollowObjectMapper;
import com.netflix.vms.transformer.CycleConstants;
import com.netflix.vms.transformer.common.TransformerContext;
import com.netflix.vms.transformer.hollowinput.ArtWorkImageTypeHollow;
import com.netflix.vms.transformer.hollowinput.VMSHollowInputAPI;
import com.netflix.vms.transformer.hollowoutput.ArtWorkImageTypeEntry;
import com.netflix.vms.transformer.modules.AbstractTransformModule;

public class ArtworkTypeModule extends AbstractTransformModule {
    private final String UNAVAILABLE_STRING = "unavailable";
    private final boolean TRUE = true;

    public ArtworkTypeModule(VMSHollowInputAPI api, TransformerContext ctx, CycleConstants cycleConstants, HollowObjectMapper mapper) {
        super(api, ctx, cycleConstants, mapper);
    }

    @Override
    public void transform() {
        for(ArtWorkImageTypeHollow inputImageType : api.getAllArtWorkImageTypeHollow()) {
            ArtWorkImageTypeEntry outputImageType = new ArtWorkImageTypeEntry();
            outputImageType.nameStr = inputImageType._getImageType()._getValue().toCharArray();
            outputImageType.recipeNameStr = inputImageType._getRecipe()._getValue().toCharArray();
            outputImageType.unavailableFileNameStr = UNAVAILABLE_STRING.toCharArray();
            outputImageType.allowMultiples = TRUE;
            mapper.addObject(outputImageType);
        }
    }

}