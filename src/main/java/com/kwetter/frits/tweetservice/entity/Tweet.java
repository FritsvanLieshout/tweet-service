package com.kwetter.frits.tweetservice.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tweet")
public class Tweet {

    @Id
    private String id;

    private int userId;
    private String message;

    private String posted;

    public Tweet() {}

    public Tweet(int userId, String message) {
        this.userId = userId;
        this.message = message;
    }

    public Tweet(String id, int userId, String message, String posted) {
        this.id = id;
        this.userId = userId;
        this.message = message;
        this.posted = posted;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPosted() {
        return posted;
    }

    public void setPosted(String posted) {
        this.posted = posted;
    }

    @Override
    public String toString() {
        return "Tutorial [id=" + id + ", user_id=" + userId + ", message=" + message + ", posted=" + posted + "]";
    }
}
