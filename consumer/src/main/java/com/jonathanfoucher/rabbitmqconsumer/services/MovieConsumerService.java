package com.jonathanfoucher.rabbitmqconsumer.services;

import com.jonathanfoucher.rabbitmqconsumer.data.dto.MovieDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
@Slf4j
public class MovieConsumerService {
    @Bean
    public Consumer<Message<MovieDto>> consumeMovie() {
        return message -> log.info("Received movie: {} with headers: {}", message.getPayload(), message.getHeaders());
    }
}
