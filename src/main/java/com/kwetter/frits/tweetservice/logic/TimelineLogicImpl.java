package com.kwetter.frits.tweetservice.logic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kwetter.frits.tweetservice.configuration.KafkaProperties;
import com.kwetter.frits.tweetservice.entity.Tweet;
import com.kwetter.frits.tweetservice.interfaces.TimelineLogic;
import com.kwetter.frits.tweetservice.logic.dto.TweetTimelineDTO;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
public class TimelineLogicImpl implements TimelineLogic {

    private final Logger log = LoggerFactory.getLogger(TimelineLogicImpl.class);

    private static final String TOPIC = "tweet-posted";

    private final KafkaProperties kafkaProperties;

    private final static Logger logger = LoggerFactory.getLogger(TimelineLogicImpl.class);
    private KafkaProducer<String, String> producer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TimelineLogicImpl(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
        initialize();
    }

    @PostConstruct
    public void initialize(){
        log.info("Kafka producer initializing...");
        this.producer = new KafkaProducer<>(kafkaProperties.getProducerProps());
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
        log.info("Kafka producer initialized");
    }

    @Override
    public void timeLineTweetPost(Tweet tweet) {
        try {
            var tweetTimelineDTO = new TweetTimelineDTO(tweet.getTweetUser(), tweet.getMessage(), tweet.getPosted(), tweet.getMentions(), tweet.getHashtags());
            var message = objectMapper.writeValueAsString(tweetTimelineDTO);
            ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, message);
            producer.send(record);
        } catch (JsonProcessingException e) {
            logger.error("Could not send tweet", e);
        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutdown Kafka producer");
        producer.close();
    }
}
