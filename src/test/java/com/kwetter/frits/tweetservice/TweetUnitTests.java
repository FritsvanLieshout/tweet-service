package com.kwetter.frits.tweetservice;

import com.kwetter.frits.tweetservice.interfaces.TweetLogic;
import com.kwetter.frits.tweetservice.logic.TweetLogicImpl;
import com.kwetter.frits.tweetservice.repository.TweetRepository;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class TweetUnitTests {

    private static final Logger LOG = LoggerFactory.getLogger(TweetUnitTests.class);

    private TweetLogic tweetLogic;

    @MockBean
    TweetRepository tweetRepository;

    @BeforeAll
    static void setupAll() {
        LOG.info("Logic Tests");
    }

    @BeforeEach
    void setup() {
        LOG.info("[Begin test]");
        this.tweetLogic = new TweetLogicImpl(tweetRepository);
    }

    @AfterEach
    void tearDown() {
        LOG.info("[End test]");
    }

    @AfterAll
    static void tearDownAll() {
        LOG.info("-- End --");
    }

    @Test
    void testCheckIfTweetContainsSwearWord() {
        var result = tweetLogic.checkSwearWords("Testing is goddamn annoying");
        LOG.info("Contains swear word? {}", result);
        Assertions.assertTrue(result);
    }

    @Test
    void testCheckIfTweetDoesNotContainsSwearWord() {
        var result = tweetLogic.checkSwearWords("Testing is my favorite task");
        LOG.info("Contains swear word? {}", result);
        Assertions.assertFalse(result);
    }
}
