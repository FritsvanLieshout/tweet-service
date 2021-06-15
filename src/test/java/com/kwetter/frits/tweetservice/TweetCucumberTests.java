package com.kwetter.frits.tweetservice;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.runner.RunWith;

@CucumberContextConfiguration
@RunWith(Cucumber.class)
@CucumberOptions(features = "classpath:features/tweet.feature")
public class TweetCucumberTests {
    //TESTS for models, with Cucumber is this an easy to do
}
