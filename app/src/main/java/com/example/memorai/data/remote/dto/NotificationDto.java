package com.example.memorai.data.remote.dto;

public class NotificationDto {
    private String id;
    private String title;
    private String message;
    private long timestamp;

    // Constructor mặc định (Firebase yêu cầu)
    public NotificationDto() {}

    // Constructor đầy đủ
    public NotificationDto(String id, String title, String message, long timestamp) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
    }

    // Getter & Setter chuẩn
    public String getId() {
        return id;
    }

    public void setId(String notificationId) {
        this.id = notificationId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
