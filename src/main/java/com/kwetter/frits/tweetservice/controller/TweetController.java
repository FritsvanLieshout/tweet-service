package com.kwetter.frits.tweetservice.controller;

import com.kwetter.frits.tweetservice.entity.Tweet;
import com.kwetter.frits.tweetservice.logic.TimelineLogicImpl;
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
    private final TimelineLogicImpl timelineService;

    public TweetController(TweetLogicImpl tweetLogic, TimelineLogicImpl timelineService) { this.tweetLogic = tweetLogic; this.timelineService = timelineService; }

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
    public ResponseEntity<Tweet> postTweet(@RequestBody Tweet tweet) {
        try {
            Tweet _tweet = tweetLogic.post(new Tweet(tweet.getTweetUser(), tweet.getMessage()));
            timelineService.timeLineTweetPost(_tweet);

            return new ResponseEntity<>(_tweet, HttpStatus.CREATED);
        }
        catch (Exception ex) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/tweet")
    public ResponseEntity<Tweet> getTweetById(@RequestParam String tweetId) {
        try {
            //ID not found
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
}
