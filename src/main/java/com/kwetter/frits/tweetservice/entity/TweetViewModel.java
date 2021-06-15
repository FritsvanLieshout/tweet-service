package com.kwetter.frits.tweetservice.entity;

public class TweetViewModel {

    private TweetUser tweetUser;
    private String message;
    private String posted;
    private String mentions;
    private String hashtags;

    public TweetViewModel() {
    }

    public TweetViewModel(TweetUser tweetUser, String message, String posted, String mentions, String hashtags) {
        this.tweetUser = tweetUser;
        this.message = message;
        this.posted = posted;
        this.mentions = mentions;
        this.hashtags = hashtags;
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

    public String getMentions() { return mentions; }

    public void setMentions(String mentions) { this.mentions = mentions; }

    public String getHashtags() { return hashtags; }

    public void setHashtags(String hashtags) { this.hashtags = hashtags; }
}
