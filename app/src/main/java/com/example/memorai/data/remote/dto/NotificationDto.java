package com.example.memorai.data.remote.dto;

public class NotificationDto {
    public String id;
    public String title;
    public String message;
    public long timestamp;

    public NotificationDto() {}

    public NotificationDto(String id, String title, String message, long timestamp) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
    }
}
