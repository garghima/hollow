package com.netflix.vms.transformer.hollowinput;

import com.netflix.hollow.api.custom.HollowObjectTypeAPI;
import com.netflix.hollow.core.read.dataaccess.HollowObjectTypeDataAccess;

@SuppressWarnings("all")
public class IPLArtworkDerivativeSetTypeAPI extends HollowObjectTypeAPI {

    private final IPLArtworkDerivativeSetDelegateLookupImpl delegateLookupImpl;

    IPLArtworkDerivativeSetTypeAPI(VMSHollowInputAPI api, HollowObjectTypeDataAccess typeDataAccess) {
        super(api, typeDataAccess, new String[] {
            "derivativeSetId",
            "derivativesGroupBySource"
        });
        this.delegateLookupImpl = new IPLArtworkDerivativeSetDelegateLookupImpl(this);
    }

    public int getDerivativeSetIdOrdinal(int ordinal) {
        if(fieldIndex[0] == -1)
            return missingDataHandler().handleReferencedOrdinal("IPLArtworkDerivativeSet", ordinal, "derivativeSetId");
        return getTypeDataAccess().readOrdinal(ordinal, fieldIndex[0]);
    }

    public StringTypeAPI getDerivativeSetIdTypeAPI() {
        return getAPI().getStringTypeAPI();
    }

    public int getDerivativesGroupBySourceOrdinal(int ordinal) {
        if(fieldIndex[1] == -1)
            return missingDataHandler().handleReferencedOrdinal("IPLArtworkDerivativeSet", ordinal, "derivativesGroupBySource");
        return getTypeDataAccess().readOrdinal(ordinal, fieldIndex[1]);
    }

    public IPLDerivativeGroupSetTypeAPI getDerivativesGroupBySourceTypeAPI() {
        return getAPI().getIPLDerivativeGroupSetTypeAPI();
    }

    public IPLArtworkDerivativeSetDelegateLookupImpl getDelegateLookupImpl() {
        return delegateLookupImpl;
    }

    @Override
    public VMSHollowInputAPI getAPI() {
        return (VMSHollowInputAPI) api;
    }

}