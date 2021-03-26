package com.kwetter.frits.tweetservice.repository;

import com.kwetter.frits.tweetservice.entity.Tweet;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TweetRepository extends MongoRepository<Tweet, String> {
}
