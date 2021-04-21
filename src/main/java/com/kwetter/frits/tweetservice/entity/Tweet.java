package com.kwetter.frits.tweetservice.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tweet")
public class Tweet {

    @Id
    private String id;

    private TweetUser tweetUser;
    private String message;
    private String posted;

    public Tweet() {}

    public Tweet(TweetUser tweetUser, String message) {
        this.tweetUser = tweetUser;
        this.message = message;
    }

    public Tweet(String id, TweetUser tweetUser, String message, String posted) {
        this.id = id;
        this.tweetUser = tweetUser;
        this.message = message;
        this.posted = posted;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TweetUser getTweetUser() {
        return tweetUser;
    }

    public void setTweetUser(TweetUser tweetUser) {
        this.tweetUser = tweetUser;
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
        return "Tweet [id=" + id + ", user=" + tweetUser.toString() + ", message=" + message + ", posted=" + posted + "]";
    }
}
