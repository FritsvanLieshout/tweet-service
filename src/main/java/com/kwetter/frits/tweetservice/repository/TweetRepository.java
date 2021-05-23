package com.kwetter.frits.tweetservice.repository;

import com.kwetter.frits.tweetservice.entity.Tweet;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public interface TweetRepository extends MongoRepository<Tweet, String> {
    List<Tweet> findAllByOrderByPostedDesc();
    Tweet findTweetById(String id);
    List<Tweet> findTweetByTweetUser_UsernameAndTweetUser_UserId(String username, UUID userId);
    List<Tweet> findTweetByMentions(String username);
    List<Tweet> findTop20ByMentionsOrderByPostedDesc(String username);
}
