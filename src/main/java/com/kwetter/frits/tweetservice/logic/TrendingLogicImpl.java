package com.kwetter.frits.tweetservice.logic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kwetter.frits.tweetservice.configuration.KafkaProperties;
import com.kwetter.frits.tweetservice.interfaces.TrendingLogic;
import com.kwetter.frits.tweetservice.logic.dto.TrendingDTO;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;

@Service
public class TrendingLogicImpl implements TrendingLogic {

    private final Logger log = LoggerFactory.getLogger(TrendingLogicImpl.class);

    private static final String TOPIC = "trending-item-added";

    private final KafkaProperties kafkaProperties;

    private final static Logger logger = LoggerFactory.getLogger(TrendingLogicImpl.class);
    private KafkaProducer<String, String> producer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TrendingLogicImpl(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
        initialize();
    }

    @PostConstruct
    public void initialize() {
        log.info("Kafka producer initializing...");
        this.producer = new KafkaProducer<>(kafkaProperties.getProducerProps());
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
        log.info("Kafka producer initialized");
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutdown Kafka producer");
        producer.close();
    }

    @Override
    public void trendingItemCreate(List<String> trends) {
        try {
            var trending = new TrendingDTO(trends);
            String message = objectMapper.writeValueAsString(trending);
            ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, message);
            producer.send(record);
        } catch (JsonProcessingException e) {
            logger.error("Could not send tweet", e);
        }
    }
}
