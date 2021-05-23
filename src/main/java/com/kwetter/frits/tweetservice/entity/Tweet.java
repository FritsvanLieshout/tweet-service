package com.kwetter.frits.tweetservice.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "tweet")
public class Tweet {

    @Id
    private String id;

    private TweetUser tweetUser;
    private String message;
    private String posted;
    private List<String> mentions;
    private List<String> hashtags;

    public Tweet() {}

    public Tweet(TweetUser tweetUser, String message) {
        this.tweetUser = tweetUser;
        this.message = message;
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

    public List<String> getMentions() { return mentions; }

    public void setMentions(List<String> mentions) { this.mentions = mentions; }

    public List<String> getHashtags() { return hashtags; }

    public void setHashtags(List<String> hashtags) { this.hashtags = hashtags; }

    @Override
    public String toString() {
        return "Tweet [id=" + id + ", user=" + tweetUser.toString() + ", message=" + message + ", posted=" + posted + "]";
    }
}
