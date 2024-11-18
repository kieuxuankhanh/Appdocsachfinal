package com.example.appdocsachfinal.Model;

public class ModelNoti {
    String id;
    String title;
    String message;
    String uid;
    String bookId;
    long timestamp;

    public ModelNoti() {
    }

    public ModelNoti(String id, String title, String message, String uid, String bookId, long timestamp) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.uid = uid;
        this.bookId = bookId;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
