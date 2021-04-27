package com.kwetter.frits.tweetservice.repository;

import com.kwetter.frits.tweetservice.entity.Tweet;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface TweetRepository extends MongoRepository<Tweet, String> {
    List<Tweet> findAllByOrderByPostedDesc();
    Tweet findTweetById(String id);
}
