package com.netflix.vms.transformer.hollowinput;

import com.netflix.hollow.api.custom.HollowTypeAPI;
import com.netflix.hollow.api.objects.delegate.HollowCachedDelegate;
import com.netflix.hollow.api.objects.delegate.HollowObjectAbstractDelegate;
import com.netflix.hollow.core.read.dataaccess.HollowObjectTypeDataAccess;
import com.netflix.hollow.core.schema.HollowObjectSchema;

@SuppressWarnings("all")
public class DefaultExtensionRecipeDelegateCachedImpl extends HollowObjectAbstractDelegate implements HollowCachedDelegate, DefaultExtensionRecipeDelegate {

    private final int recipeNameOrdinal;
    private final int extensionOrdinal;
   private DefaultExtensionRecipeTypeAPI typeAPI;

    public DefaultExtensionRecipeDelegateCachedImpl(DefaultExtensionRecipeTypeAPI typeAPI, int ordinal) {
        this.recipeNameOrdinal = typeAPI.getRecipeNameOrdinal(ordinal);
        this.extensionOrdinal = typeAPI.getExtensionOrdinal(ordinal);
        this.typeAPI = typeAPI;
    }

    public int getRecipeNameOrdinal(int ordinal) {
        return recipeNameOrdinal;
    }

    public int getExtensionOrdinal(int ordinal) {
        return extensionOrdinal;
    }

    @Override
    public HollowObjectSchema getSchema() {
        return typeAPI.getTypeDataAccess().getSchema();
    }

    @Override
    public HollowObjectTypeDataAccess getTypeDataAccess() {
        return typeAPI.getTypeDataAccess();
    }

    public DefaultExtensionRecipeTypeAPI getTypeAPI() {
        return typeAPI;
    }

    public void updateTypeAPI(HollowTypeAPI typeAPI) {
        this.typeAPI = (DefaultExtensionRecipeTypeAPI) typeAPI;
    }

}