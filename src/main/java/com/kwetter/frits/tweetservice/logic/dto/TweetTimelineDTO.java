package com.kwetter.frits.tweetservice.logic.dto;

import com.kwetter.frits.tweetservice.entity.TweetUser;

import java.util.List;

public class TweetTimelineDTO {

    private TweetUser tweetUser;
    private String tweetMessage;
    private String tweetPosted;
    private List<String> tweetMentions;
    private List<String> tweetHashtags;

    public TweetTimelineDTO(TweetUser tweetUser, String tweetMessage, String tweetPosted, List<String> tweetMentions, List<String> tweetHashtags) {
        this.tweetUser = tweetUser;
        this.tweetMessage = tweetMessage;
        this.tweetPosted = tweetPosted;
        this.tweetMentions = tweetMentions;
        this.tweetHashtags = tweetHashtags;
    }

    public TweetUser getTweetUser() {
        return tweetUser;
    }

    public void setTweetUser(TweetUser tweetUser) {
        this.tweetUser = tweetUser;
    }

    public String getTweetMessage() {
        return tweetMessage;
    }

    public void setTweetMessage(String tweetMessage) {
        this.tweetMessage = tweetMessage;
    }

    public String getTweetPosted() {
        return tweetPosted;
    }

    public void setTweetPosted(String tweetPosted) {
        this.tweetPosted = tweetPosted;
    }

    public List<String> getTweetMentions() { return tweetMentions; }

    public void setTweetMentions(List<String> tweetMentions) { this.tweetMentions = tweetMentions; }

    public List<String> getTweetHashtags() { return tweetHashtags; }

    public void setTweetHashtags(List<String> tweetHashtags) { this.tweetHashtags = tweetHashtags; }
}
