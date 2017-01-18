package com.netflix.vms.transformer.hollowinput;

import com.netflix.hollow.api.objects.HollowObject;

@SuppressWarnings("all")
public class StreamProfileIdHollow extends HollowObject {

    public StreamProfileIdHollow(StreamProfileIdDelegate delegate, int ordinal) {
        super(delegate, ordinal);
    }

    public long _getValue() {
        return delegate().getValue(ordinal);
    }

    public Long _getValueBoxed() {
        return delegate().getValueBoxed(ordinal);
    }

    public VMSHollowInputAPI api() {
        return typeApi().getAPI();
    }

    public StreamProfileIdTypeAPI typeApi() {
        return delegate().getTypeAPI();
    }

    protected StreamProfileIdDelegate delegate() {
        return (StreamProfileIdDelegate)delegate;
    }

}