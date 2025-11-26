package com.sensor.dto.response;

import java.util.HashMap;
import java.util.Map;

public class ChannelStats {
String deviceId;
String channel;
long count = 0;
double sum = 0;
double min = Double.MAX_VALUE;
double max = Double.MIN_VALUE;
double sumSquares = 0;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public double getSum() {
        return sum;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getSumSquares() {
        return sumSquares;
    }

    public void setSumSquares(double sumSquares) {
        this.sumSquares = sumSquares;
    }

    public ChannelStats(String deviceId, String channel) {
    this.deviceId = deviceId;
    this.channel = channel;
}

public void addValue(double value) {
    count++;
    sum += value;
    min = Math.min(min, value);
    max = Math.max(max, value);
    sumSquares += value * value;
}

public double getAverage() { return count > 0 ? sum / count : 0; }
public double getStdDev() {
    if (count < 2) return 0;
    double avg = getAverage();
    return Math.sqrt(sumSquares / count - avg * avg);
}

public Map<String, Object> toMap() {
    Map<String, Object> map = new HashMap<>();
    map.put("deviceId", deviceId);
    map.put("channel", channel);
    map.put("count", count);
    map.put("min", min);
    map.put("max", max);
    map.put("average", getAverage());
    map.put("stdDev", getStdDev());
    return map;
}
}
