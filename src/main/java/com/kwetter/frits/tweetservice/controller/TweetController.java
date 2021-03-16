package com.kwetter.frits.tweetservice.controller;

import com.kwetter.frits.tweetservice.entity.Tweet;
import com.kwetter.frits.tweetservice.logic.TweetLogicImpl;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/tweets")
public class TweetController {

    private final TweetLogicImpl tweetLogic;

    @Autowired
    private AmqpTemplate rabbitTemplate;
    @Value("${kwetter.rabbitmq.exchange}")
    private String exchange;
    @Value("${kwetter.rabbitmq.routingkey}")
    private String routingkey;

    public TweetController(TweetLogicImpl tweetLogic) { this.tweetLogic = tweetLogic; }

    @GetMapping()
    public ResponseEntity<List<Tweet>> retrieveAllTweets() {
        try {
            List<Tweet> _tweets = new ArrayList<>(tweetLogic.findAll());

            if (_tweets.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            rabbitTemplate.convertAndSend(exchange, routingkey, _tweets);
            return new ResponseEntity<>(_tweets, HttpStatus.OK);

        }
        catch (Exception ex) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping()
    public ResponseEntity<Tweet> postTweet(@RequestBody Tweet tweet) {
        try {
            System.out.println(tweet.toString());
            Tweet _tweet = tweetLogic.post(new Tweet(tweet.getUserId(), tweet.getMessage()));
            return new ResponseEntity<>(_tweet, HttpStatus.CREATED);
        }

        catch (Exception ex) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
