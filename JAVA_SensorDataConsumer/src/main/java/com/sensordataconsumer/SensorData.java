package com.sensordataconsumer;

/**
 * This is a simple class for creating a sensor data
 * object which makes creation and consumption of our
 * sensor data for charts easier
 */
public class SensorData{
    public Double getData() {
        return data;
    }

    public void setData(Double data) {
        this.data = data;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    private Double data;
    private String time;

    public SensorData(Double data, String time){
        this.data = data;
        this.time = time;
    }

}