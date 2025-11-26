package com.sensor.dto.response;

public class SensorSample {
long timestamp;
String deviceId;
String channel;
double value;

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getChannel() {
        return channel;
    }

    public double getValue() {
        return value;
    }

    public SensorSample(long timestamp, String deviceId, String channel, double value) {
    this.timestamp = timestamp;
    this.deviceId = deviceId;
    this.channel = channel;
    this.value = value;
}
}
