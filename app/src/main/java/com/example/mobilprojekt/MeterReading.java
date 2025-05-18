package com.example.mobilprojekt;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class MeterReading {
    @DocumentId
    private String id;
    private String reading;
    private String date;
    private String userId;
    private Double latitude;
    private Double longitude;
    private String photoUrl;
    private String address;
    private String notes;
    @ServerTimestamp
    private Date timestamp;
    private boolean isSubmitted;

    public MeterReading() {
        // Required empty constructor for Firestore
    }

    public MeterReading(String reading, String date, String userId) {
        this.reading = reading;
        this.date = date;
        this.userId = userId;
        this.isSubmitted = false;
    }

    public MeterReading(String reading, String date, String userId, Double latitude, Double longitude, String photoUrl) {
        this.reading = reading;
        this.date = date;
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.photoUrl = photoUrl;
        this.isSubmitted = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSubmitted() {
        return isSubmitted;
    }

    public void setSubmitted(boolean submitted) {
        isSubmitted = submitted;
    }
} 