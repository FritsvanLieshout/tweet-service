package com.kwetter.frits.tweetservice.interfaces;

import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface TrendingLogic {
    void trendingItemCreate(List<String> trends);
}
