package com.pk.common;

public class SensorData {
    private float _x;
    private float _y;
    private float _z;
    private long _timeStamp;

    public SensorData() {

    }

    public SensorData(float x, float y, float z, long timeStamp) {
        _x = x;
        _y = y;
        _z = z;
        _timeStamp = timeStamp;
    }

    public float get_x() {
        return _x;
    }

    public void set_x(float _x) {
        this._x = _x;
    }

    public float get_y() {
        return _y;
    }

    public void set_y(float _y) {
        this._y = _y;
    }

    public float get_z() {
        return _z;
    }

    public void set_z(float _z) {
        this._z = _z;
    }

    public long get_timeStamp() {
        return _timeStamp;
    }

    public void set_timeStamp(long _timeStamp) {
        this._timeStamp = _timeStamp;
    }

    @Override
    public String toString() {
        return new StringBuilder("[").append(_x).append(", ").append(_y).append(", ").append(_z).append(", ").append(_timeStamp).append("]").toString();
    }
}
