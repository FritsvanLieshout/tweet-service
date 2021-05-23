package com.kwetter.frits.tweetservice.interfaces;

import com.kwetter.frits.tweetservice.entity.Tweet;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface TweetLogic {
    List<Tweet> findAll();
    Tweet post(Tweet tweet);
    Tweet getTweetById(String id);
    List<String> convertCSVToList(String csv);
    List<Tweet> findAllByMentions(String username);
}
