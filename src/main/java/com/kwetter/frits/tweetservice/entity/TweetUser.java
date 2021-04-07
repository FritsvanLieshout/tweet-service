package com.kwetter.frits.tweetservice.entity;

import java.util.UUID;

public class TweetUser {

    private UUID userId;
    private String username;
    private String nickName;
    private String profileImage;

    public TweetUser() {}

    public TweetUser(UUID userId, String username, String nickName, String profileImage) {
        this.userId = userId;
        this.username = username;
        this.nickName = nickName;
        this.profileImage = profileImage;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
