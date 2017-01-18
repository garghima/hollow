package com.netflix.vms.transformer.hollowinput;

import com.netflix.hollow.api.custom.HollowTypeAPI;
import com.netflix.hollow.api.objects.delegate.HollowCachedDelegate;
import com.netflix.hollow.api.objects.delegate.HollowObjectAbstractDelegate;
import com.netflix.hollow.core.read.dataaccess.HollowObjectTypeDataAccess;
import com.netflix.hollow.core.schema.HollowObjectSchema;

@SuppressWarnings("all")
public class StoriesSynopsesHookDelegateCachedImpl extends HollowObjectAbstractDelegate implements HollowCachedDelegate, StoriesSynopsesHookDelegate {

    private final int typeOrdinal;
    private final int rankOrdinal;
    private final int translatedTextsOrdinal;
   private StoriesSynopsesHookTypeAPI typeAPI;

    public StoriesSynopsesHookDelegateCachedImpl(StoriesSynopsesHookTypeAPI typeAPI, int ordinal) {
        this.typeOrdinal = typeAPI.getTypeOrdinal(ordinal);
        this.rankOrdinal = typeAPI.getRankOrdinal(ordinal);
        this.translatedTextsOrdinal = typeAPI.getTranslatedTextsOrdinal(ordinal);
        this.typeAPI = typeAPI;
    }

    public int getTypeOrdinal(int ordinal) {
        return typeOrdinal;
    }

    public int getRankOrdinal(int ordinal) {
        return rankOrdinal;
    }

    public int getTranslatedTextsOrdinal(int ordinal) {
        return translatedTextsOrdinal;
    }

    @Override
    public HollowObjectSchema getSchema() {
        return typeAPI.getTypeDataAccess().getSchema();
    }

    @Override
    public HollowObjectTypeDataAccess getTypeDataAccess() {
        return typeAPI.getTypeDataAccess();
    }

    public StoriesSynopsesHookTypeAPI getTypeAPI() {
        return typeAPI;
    }

    public void updateTypeAPI(HollowTypeAPI typeAPI) {
        this.typeAPI = (StoriesSynopsesHookTypeAPI) typeAPI;
    }

}