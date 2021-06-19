package com.kwetter.frits.tweetservice.logic;

import com.kwetter.frits.tweetservice.entity.Tweet;
import com.kwetter.frits.tweetservice.interfaces.TweetLogic;
import com.kwetter.frits.tweetservice.repository.TweetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("input", text);
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(map, headers);

        try {
            log.info("--- START CHECK SWEAR WORD ---");
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
            var result = responseEntity.getBody();
            log.info("--- SWEAR WORDS CHECKED ---");
            log.info("--- CONTAINS SWEAR WORD : {}", result);

            if (result != null) {
                return Objects.equals(result, "True");
            }
            return false;
        }

        catch (Exception e) {
            log.info("Error: {}", e.getMessage());
        }
        return false;
    }
}
