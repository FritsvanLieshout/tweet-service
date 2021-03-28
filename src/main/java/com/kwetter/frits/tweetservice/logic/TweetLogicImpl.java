package com.kwetter.frits.tweetservice.logic;

import com.kwetter.frits.tweetservice.entity.Tweet;
import com.kwetter.frits.tweetservice.interfaces.TweetLogic;
import com.kwetter.frits.tweetservice.repository.TweetRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TweetLogicImpl implements TweetLogic {

    private final TweetRepository tweetRepository;

    public TweetLogicImpl(TweetRepository tweetRepository) {
        this.tweetRepository = tweetRepository;
    }

    @Override
    public List<Tweet> findAll() {
        return tweetRepository.findAllByOrderByPostedDesc();
    }

    @Override
    public Tweet post(Tweet tweet) {
        String currentDateTime = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        tweet.setPosted(currentDateTime);

        return tweetRepository.save(tweet);
    }
}
