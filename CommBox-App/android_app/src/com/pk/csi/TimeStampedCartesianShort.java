package com.pk.csi;

import com.mbientlab.metawear.data.CartesianShort;

public class TimeStampedCartesianShort implements Comparable<TimeStampedCartesianShort>{
    private CartesianShort _cartesianShort;
    private Long _timeStamp;

    public TimeStampedCartesianShort(CartesianShort cs) {
        _cartesianShort = cs;
        _timeStamp = System.currentTimeMillis();
    }

    public Long get_timeStamp() {
        return _timeStamp;
    }

    public CartesianShort get_cartesianShort() {
        return _cartesianShort;
    }

    @Override
    public int compareTo(TimeStampedCartesianShort o) {
        return this._timeStamp.compareTo(o.get_timeStamp());
    }
}
