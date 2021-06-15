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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SpringBootTest
class TweetIntegrationTests {

    private static final Logger LOG = LoggerFactory.getLogger(TweetIntegrationTests.class);

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

    private static boolean started = false;
    private static KafkaContainer kafkaContainer;

    @Autowired
    ApplicationContext context;

    private MockMvc mockMvc;

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

        Map<String, String> consumerProps = getConsumerProps("store");
        consumerProps.put("client.id", "default-client");
        kafkaProperties.setConsumer(consumerProps);

        tweetController = new TweetController(new TweetLogicImpl(tweetRepository), new TimelineLogicImpl(kafkaProperties), new TrendingLogicImpl(kafkaProperties));
        mockMvc = MockMvcBuilders.standaloneSetup(tweetController).build();
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
    void producesMessages() throws Exception {
        var user = new TweetUser();
        user.setUsername("frits1998");
        user.setVerified(true);
        var tweet = new TweetViewModel(user, "Test 1", DateTimeFormatter.ISO_INSTANT.format(Instant.now()), "kevindebruyne", "mockito");
        ObjectMapper mapper = new ObjectMapper();
        String jsonStr = mapper.writeValueAsString(tweet);

        mockMvc.perform(post("/api/tweets/tweet").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(jsonStr))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        Map<String, Object> consumerProps = new HashMap<>(getConsumerProps("store"));
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Collections.singletonList("tweet-posted"));
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));

        assertThat(records.count()).isEqualTo(1);
        ConsumerRecord<String, String> record = records.iterator().next();
        LOG.info("Consumed message in {} : {}", "tweet-posted", record.value());
        Assertions.assertNotNull(record.value());
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
}
