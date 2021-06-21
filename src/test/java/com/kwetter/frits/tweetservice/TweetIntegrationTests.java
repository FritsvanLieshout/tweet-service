package com.kwetter.frits.tweetservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kwetter.frits.tweetservice.configuration.KafkaProperties;
import com.kwetter.frits.tweetservice.controller.TweetController;
import com.kwetter.frits.tweetservice.entity.TweetUser;
import com.kwetter.frits.tweetservice.entity.TweetViewModel;
import com.kwetter.frits.tweetservice.interfaces.TimelineLogic;
import com.kwetter.frits.tweetservice.interfaces.TrendingLogic;
import com.kwetter.frits.tweetservice.interfaces.TweetLogic;
import com.kwetter.frits.tweetservice.logic.TimelineLogicImpl;
import com.kwetter.frits.tweetservice.logic.TrendingLogicImpl;
import com.kwetter.frits.tweetservice.logic.TweetLogicImpl;
import com.kwetter.frits.tweetservice.repository.TweetRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testcontainers.containers.KafkaContainer;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest
class TweetIntegrationTests {

    //TODO
    //Test the unhappy flow
    //Test for consumer user-deleted

    @InjectMocks
    private TweetController tweetController;
    @MockBean
    TweetRepository tweetRepository;
    @Mock
    private TweetLogic tweetLogic;
    @Mock
    private TrendingLogic trendingLogic;
    @Mock
    private TimelineLogic timelineLogic;

    private MockMvc mockMvc;
    private static boolean started = false;
    private static KafkaContainer kafkaContainer;

    private static final Logger LOG = LoggerFactory.getLogger(TweetIntegrationTests.class);

    @BeforeAll
    static void setupAll() {
        LOG.info("-- Start [Tweet - Test]--");
        if (!started) {
            startTestContainer();
            started = true;
        }
    }

    private static void startTestContainer() {
        kafkaContainer = new KafkaContainer("5.3.1");
        kafkaContainer.start();
    }

    @BeforeEach
    void setup() {
        LOG.info("[Begin test]");
        KafkaProperties kafkaProperties = new KafkaProperties();
        Map<String, String> producerProps = getProducerProps();
        kafkaProperties.setProducer(new HashMap<>(producerProps));

        Map<String, String> consumerProps = getConsumerProps("kwetter");
        consumerProps.put("client.id", "default-client");
        kafkaProperties.setConsumer(consumerProps);

        tweetController = new TweetController(new TweetLogicImpl(tweetRepository), new TimelineLogicImpl(kafkaProperties), new TrendingLogicImpl(kafkaProperties));
        mockMvc = MockMvcBuilders.standaloneSetup(tweetController).build();
    }

    @AfterEach
    void tearDown() {
        LOG.info("[End test]");
        getConsumerProps("kwetter").clear();
    }

    @AfterAll
    static void tearDownAll() {
        LOG.info("-- End --");
        started = false;
        kafkaContainer.close();
    }

    @Test
    @Order(0)
    void testPostTweetWithoutSwearWord() throws Exception {
        var user = new TweetUser(UUID.randomUUID(), "tester040", "Integration test", null, true, "Mockito Tester");
        var tweet = new TweetViewModel(user, "Testing is my favorite task", DateTimeFormatter.ISO_INSTANT.format(Instant.now()), "tester", "mockito");

        mockMvc.perform(post("/api/tweets/tweet").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(jsonString(tweet)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tweetUser.username", is(tweet.getTweetUser().getUsername())))
                .andExpect(jsonPath("$.message", is(tweet.getMessage())))
                .andExpect(jsonPath("$.mentions", hasSize(1)))
                .andExpect(jsonPath("$.hashtags", hasSize(1)));

        Map<String, Object> consumerProps = new HashMap<>(getConsumerProps("kwetter"));
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singletonList("tweet-posted"));
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));

        assertThat(records.count()).isEqualTo(1);
        ConsumerRecord<String, String> record = records.iterator().next();
        LOG.info("Consumed message in {} : {}", "tweet-posted", record.value());
        Assertions.assertNotNull(record.value());

        consumer.close();
    }

    @Test
    @Order(1)
    void testPostTweetWithSwearWord() throws Exception {
        var user = new TweetUser(UUID.randomUUID(), "tester040", "Integration test", null, true, "Mockito Tester");
        var tweet = new TweetViewModel(user, "Testing is goddamn annoying", DateTimeFormatter.ISO_INSTANT.format(Instant.now()), "tester", "mockito");
        mockMvc.perform(post("/api/tweets/tweet").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(jsonString(tweet)))
                .andExpect(status().isNoContent())
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @Order(2)
    void testPostTweetWith3Hashtags() throws Exception {
        var user = new TweetUser(UUID.randomUUID(), "tester040", "Integration test", null, true, "Mockito Tester");
        var tweet = new TweetViewModel();
        tweet.setTweetUser(user);
        tweet.setMessage("Testing is my favorite task");
        tweet.setPosted(DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
        tweet.setHashtags("mockito,java,junit");

        mockMvc.perform(post("/api/tweets/tweet").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(jsonString(tweet)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is(tweet.getMessage())))
                .andExpect(jsonPath("$.hashtags", hasSize(3)));

        Map<String, Object> consumerProps = new HashMap<>(getConsumerProps("kwetter"));
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singletonList("tweet-posted"));
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));

        assertThat(records.count()).isEqualTo(1);
        ConsumerRecord<String, String> record = records.iterator().next();
        LOG.info("Consumed message in {} : {}", "tweet-posted", record.value());
        Assertions.assertNotNull(record.value());

        consumer.close();
    }

    @Test
    @Order(3)
    void testPostTweetWithNullHashtags() throws Exception {
        var user = new TweetUser(UUID.randomUUID(), "tester040", "Integration test", null, true, "Mockito Tester");
        var tweet = new TweetViewModel();
        tweet.setTweetUser(user);
        tweet.setMessage("Testing is my favorite task");
        tweet.setPosted(DateTimeFormatter.ISO_INSTANT.format(Instant.now()));

        mockMvc.perform(post("/api/tweets/tweet").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(jsonString(tweet)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is(tweet.getMessage())))
                .andExpect(jsonPath("$.hashtags").isEmpty());

        Map<String, Object> consumerProps = new HashMap<>(getConsumerProps("kwetter"));
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singletonList("tweet-posted"));
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));

        assertThat(records.count()).isEqualTo(1);
        ConsumerRecord<String, String> record = records.iterator().next();
        LOG.info("Consumed message in {} : {}", "tweet-posted", record.value());
        Assertions.assertNotNull(record.value());

        consumer.close();
    }

    @Test
    @Order(4)
    void testPostTweetWith2Mentions() throws Exception {
        var user = new TweetUser(UUID.randomUUID(), "tester040", "Integration test", null, true, "Mockito Tester");
        var tweet = new TweetViewModel();
        tweet.setTweetUser(user);
        tweet.setMessage("Testing is my favorite task");
        tweet.setPosted(DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
        tweet.setMentions("tester,azure");

        mockMvc.perform(post("/api/tweets/tweet").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(jsonString(tweet)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is(tweet.getMessage())))
                .andExpect(jsonPath("$.mentions", hasSize(2)));

        Map<String, Object> consumerProps = new HashMap<>(getConsumerProps("kwetter"));
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singletonList("tweet-posted"));
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));

        assertThat(records.count()).isEqualTo(1);
        ConsumerRecord<String, String> record = records.iterator().next();
        LOG.info("Consumed message in {} : {}", "tweet-posted", record.value());
        Assertions.assertNotNull(record.value());

        consumer.close();
    }

    @Test
    @Order(5)
    void testPostTweetWithNullMentions() throws Exception {
        var user = new TweetUser(UUID.randomUUID(), "tester040", "Integration test", null, true, "Mockito Tester");
        var tweet = new TweetViewModel();
        tweet.setTweetUser(user);
        tweet.setMessage("Testing is my favorite task");
        tweet.setPosted(DateTimeFormatter.ISO_INSTANT.format(Instant.now()));

        mockMvc.perform(post("/api/tweets/tweet").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(jsonString(tweet)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is(tweet.getMessage())))
                .andExpect(jsonPath("$.mentions").isEmpty());

        Map<String, Object> consumerProps = new HashMap<>(getConsumerProps("kwetter"));
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singletonList("tweet-posted"));
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));

        assertThat(records.count()).isEqualTo(1);
        ConsumerRecord<String, String> record = records.iterator().next();
        LOG.info("Consumed message in {} : {}", "tweet-posted", record.value());
        Assertions.assertNotNull(record.value());

        consumer.close();
    }

    private Map<String, String> getProducerProps() {
        Map<String, String> producerProps = new HashMap<>();
        producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.put("bootstrap.servers", kafkaContainer.getBootstrapServers());
        return producerProps;
    }

    private Map<String, String> getConsumerProps(String group) {
        Map<String, String> consumerProps = new HashMap<>();
        consumerProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put("bootstrap.servers", kafkaContainer.getBootstrapServers());
        consumerProps.put("auto.offset.reset", "earliest");
        consumerProps.put("group.id", group);
        return consumerProps;
    }

    private String jsonString(Object object) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }
}
