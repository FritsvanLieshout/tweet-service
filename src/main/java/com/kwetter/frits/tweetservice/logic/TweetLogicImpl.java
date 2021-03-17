package com.kwetter.frits.tweetservice.logic;

import com.kwetter.frits.tweetservice.entity.Tweet;
import com.kwetter.frits.tweetservice.interfaces.TweetLogic;
import com.kwetter.frits.tweetservice.repository.TweetRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        tweet.setPosted(currentDateTime.format(formatter));

        return tweetRepository.save(tweet);
    }
}
