package com.kwetter.frits.tweetservice.logic.consumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kwetter.frits.tweetservice.configuration.KafkaProperties;
import com.kwetter.frits.tweetservice.logic.dto.TweetUserDTO;
import com.kwetter.frits.tweetservice.repository.TweetRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class UserRemoveConsumer {

    private final Logger log = LoggerFactory.getLogger(UserRemoveConsumer.class);
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final KafkaProperties kafkaProperties;

    public static final String TOPIC = "permanent-user-deleted";

    private KafkaConsumer<String, String> kafkaConsumer;
    private TweetRepository tweetRepository;
    private ExecutorService executorService = Executors.newCachedThreadPool();

    public UserRemoveConsumer(KafkaProperties kafkaProperties, TweetRepository tweetRepository) {
        this.kafkaProperties = kafkaProperties;
        this.tweetRepository = tweetRepository;
    }

    @PostConstruct
    public void start() {

        log.info("Kafka consumer starting...");
        this.kafkaConsumer = new KafkaConsumer<>(kafkaProperties.getConsumerProps());
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
        kafkaConsumer.subscribe(Collections.singletonList(TOPIC));
        log.info("Kafka consumer started");

        executorService.execute(() -> {
            try {
                while (!closed.get()) {
                    ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofSeconds(3));
                    for (ConsumerRecord<String, String> record : records) {
                        log.info("Consumed message in {} : {}", TOPIC, record.value());

                        var objectMapper = new ObjectMapper();
                        var user = objectMapper.readValue(record.value(), TweetUserDTO.class);
                        if (user != null) {
                            var tweets = tweetRepository.findTweetByTweetUser_UsernameAndTweetUser_UserId(user.getUsername(), user.getUserId());
                            tweetRepository.deleteAll(tweets);
                        }
                    }
                }
                kafkaConsumer.commitSync();
            } catch (WakeupException e) {
                if (!closed.get()) throw e;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                log.info("Kafka consumer close");
                kafkaConsumer.close();
            }
        });
    }

    public KafkaConsumer<String, String> getKafkaConsumer() {
        return kafkaConsumer;
    }

    public void shutdown() {
        log.info("Shutdown Kafka consumer");
        closed.set(true);
        kafkaConsumer.wakeup();
    }
}
