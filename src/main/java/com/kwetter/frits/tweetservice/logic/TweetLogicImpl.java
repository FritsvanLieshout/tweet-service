package com.kwetter.frits.tweetservice.logic;

import com.kwetter.frits.tweetservice.entity.Tweet;
import com.kwetter.frits.tweetservice.interfaces.TweetLogic;
import com.kwetter.frits.tweetservice.repository.TweetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Service
public class TweetLogicImpl implements TweetLogic {

    private final TweetRepository tweetRepository;
    private final Logger log = LoggerFactory.getLogger(TweetLogicImpl.class);

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

    @Override
    public Tweet getTweetById(String id) {
        return tweetRepository.findTweetById(id);
    }

    @Override
    public List<String> convertCSVToList(String csv) {
        return Arrays.asList(csv.split(","));
    }

    @Override
    public List<Tweet> findAllByMentions(String username) {
        return tweetRepository.findTop20ByMentionsOrderByPostedDesc(username);
    }

    @Override
    public Boolean checkSwearWords(String text) {
        var url = "https://kwetter-functionapp-swearwords.azurewebsites.net/api/HttpTrigger1";
        var restTemplate = new RestTemplate();
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String content = "{\"input\":\"" + text + "\"}";
        HttpEntity<String> entity = new HttpEntity<>(content, headers);

        log.info("--- START CHECK SWEAR WORD ---");
        Boolean answer = restTemplate.postForObject(url, entity, Boolean.class);
        log.info("--- SWEAR WORDS CHECKED ---");
        log.info("--- CONTAINS SWEAR WORD : {}", answer);

        return answer;
    }
}
