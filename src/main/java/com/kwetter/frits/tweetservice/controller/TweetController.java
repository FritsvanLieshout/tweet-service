package com.kwetter.frits.tweetservice.controller;

import com.kwetter.frits.tweetservice.entity.Tweet;
import com.kwetter.frits.tweetservice.entity.TweetViewModel;
import com.kwetter.frits.tweetservice.exceptions.SwearWordException;
import com.kwetter.frits.tweetservice.logic.TimelineLogicImpl;
import com.kwetter.frits.tweetservice.logic.TrendingLogicImpl;
import com.kwetter.frits.tweetservice.logic.TweetLogicImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/tweets")
public class TweetController {

    private final TweetLogicImpl tweetLogic;
    private final TimelineLogicImpl timelineLogic;
    private final TrendingLogicImpl trendingLogic;

    public TweetController(TweetLogicImpl tweetLogic, TimelineLogicImpl timelineLogic, TrendingLogicImpl trendingLogic) {
        this.tweetLogic = tweetLogic;
        this.timelineLogic = timelineLogic;
        this.trendingLogic = trendingLogic;
    }

    @GetMapping("/all")
    public ResponseEntity<List<Tweet>> retrieveAllTweets() {
        try {
            List<Tweet> _tweets = new ArrayList<>(tweetLogic.findAll());

            if (_tweets.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(_tweets, HttpStatus.OK);
        }
        catch (Exception ex) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/tweet")
    public ResponseEntity<?> postTweet(@RequestBody TweetViewModel tweetViewModel) {
        try {
            var tweet = new Tweet(tweetViewModel.getTweetUser(), tweetViewModel.getMessage());
            if (!tweetLogic.checkSwearWords(tweet.getMessage())) {
                tweet.setMentions(new ArrayList<>());
                tweet.setHashtags(new ArrayList<>());
                if (tweetViewModel.getMentions() != null) {
                    tweet.setMentions(tweetLogic.convertCSVToList(tweetViewModel.getMentions()));
                }
                if (tweetViewModel.getHashtags() != null) {
                    tweet.setHashtags(tweetLogic.convertCSVToList(tweetViewModel.getHashtags()));
                    trendingLogic.trendingItemCreate(tweet.getHashtags());
                }
                tweetLogic.post(tweet);
                timelineLogic.timeLineTweetPost(tweet);
                return new ResponseEntity<>(tweet, HttpStatus.CREATED);
            }
            return new ResponseEntity<>(new SwearWordException(), HttpStatus.NO_CONTENT); //ERROR HANDLING SWEAR WORDS
        }
        catch (Exception ex) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/tweet")
    public ResponseEntity<Tweet> getTweetById(@RequestParam String tweetId) {
        try {
            Tweet tweet = tweetLogic.getTweetById(tweetId);
            if (tweet != null) {
                return new ResponseEntity<>(tweet, HttpStatus.OK);
            }
            return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
        }
        catch (Exception ex) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/mentions")
    public ResponseEntity<List<Tweet>> getMentionsByUsername(@RequestParam String username) {
        try {
            var mentions = tweetLogic.findAllByMentions(username);
            if (mentions.isEmpty()) {
                return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(mentions, HttpStatus.OK);
        }
        catch (Exception ex) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
