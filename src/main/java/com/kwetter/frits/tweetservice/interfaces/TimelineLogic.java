package com.kwetter.frits.tweetservice.interfaces;

import com.kwetter.frits.tweetservice.entity.Tweet;
import org.springframework.validation.annotation.Validated;

@Validated
public interface TimelineLogic {
    void timeLineTweetPost(Tweet tweet);
}
