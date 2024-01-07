package com.example.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.net.HttpURLConnection;
import java.util.Optional;

@Component
public class LinkConsumer {

    private final Logger logger = LoggerFactory.getLogger(LinkConsumer.class.getName());

    private static final String TOPIC = "LINKS";
    private final RestClient restClient = createRestClient();

    @KafkaListener(topics = TOPIC, groupId = "LINK_CONSUMERS")
    public void consumeMessage(Link message) {
        logger.info("Received Link {}}", message.href());
        downloadPage(message);
    }

    private void downloadPage(Link message) {
        try {
            ResponseEntity<String> response = restClient
                    .get()
                    .uri(message.href().trim())
                    .retrieve()
                    .toEntity(String.class);

            String body = Optional.ofNullable(response.getBody()).orElse("BODY IS NULL");
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Request is successful, Brief Content : {}", body.substring(0, 255));
            } else {
                logger.warn("Request isn't successful :{}", response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private static RestClient createRestClient() {
        return RestClient.create(new RestTemplate(simpleClientHttpRequestFactory()));
    }

    private static SimpleClientHttpRequestFactory simpleClientHttpRequestFactory() {
        return new SimpleClientHttpRequestFactory() {
            @Override
            protected void prepareConnection(HttpURLConnection connection, String httpMethod) {
                connection.setInstanceFollowRedirects(true);
            }
        };
    }
}
