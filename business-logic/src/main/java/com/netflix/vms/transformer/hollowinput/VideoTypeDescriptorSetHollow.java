package com.netflix.vms.transformer.hollowinput;

import com.netflix.hollow.objects.HollowSet;
import com.netflix.hollow.HollowSetSchema;
import com.netflix.hollow.objects.delegate.HollowSetDelegate;
import com.netflix.hollow.objects.generic.GenericHollowRecordHelper;

@SuppressWarnings("all")
public class VideoTypeDescriptorSetHollow extends HollowSet<VideoTypeDescriptorHollow> {

    public VideoTypeDescriptorSetHollow(HollowSetDelegate delegate, int ordinal) {
        super(delegate, ordinal);
    }

    @Override
    public VideoTypeDescriptorHollow instantiateElement(int ordinal) {
        return (VideoTypeDescriptorHollow) api().getVideoTypeDescriptorHollow(ordinal);
    }

    @Override
    public boolean equalsElement(int elementOrdinal, Object testObject) {
        return GenericHollowRecordHelper.equalObject(getSchema().getElementType(), elementOrdinal, testObject);
    }

    public VMSHollowInputAPI api() {
        return typeApi().getAPI();
    }

    public VideoTypeDescriptorSetTypeAPI typeApi() {
        return (VideoTypeDescriptorSetTypeAPI) delegate.getTypeAPI();
    }

}