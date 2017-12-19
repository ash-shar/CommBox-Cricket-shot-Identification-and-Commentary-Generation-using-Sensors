package com.pk.csi;

import com.mbientlab.metawear.data.CartesianFloat;

/**
 * Created by PK on 11/11/16.
 */

public class TimeStampedCartesianFloat implements Comparable<TimeStampedCartesianFloat>{
    private CartesianFloat _cartesianFloat;
    private Long _timeStamp;

    public TimeStampedCartesianFloat(CartesianFloat cf) {
        _cartesianFloat = cf;
        _timeStamp = System.currentTimeMillis();
    }

    public Long get_timeStamp() {
        return _timeStamp;
    }

    public CartesianFloat get_cartesianFloat() {
        return _cartesianFloat;
    }

    @Override
    public int compareTo(TimeStampedCartesianFloat o) {
        return this._timeStamp.compareTo(o.get_timeStamp());
    }
}
