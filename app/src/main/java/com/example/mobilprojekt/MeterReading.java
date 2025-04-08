package com.example.mobilprojekt;

public class MeterReading {
    private String reading;
    private String date;

    public MeterReading() {
        
    }

    public MeterReading(String reading, String date) {
        this.reading = reading;
        this.date = date;
    }

    public String getReading() {
        return reading;
    }

    public void setReading(String reading) {
        this.reading = reading;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
} 