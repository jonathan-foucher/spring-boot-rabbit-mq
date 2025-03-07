package com.jonathanfoucher.rabbitmqproducer.services;

import com.jonathanfoucher.rabbitmqproducer.data.dto.MovieDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MovieProducerService {
    private final StreamBridge streamBridge;

    private static final String MOVIES = "movies-out-0";

    public void sendMovieMessage(Message<MovieDto> message) {
        log.info("Sending movie: {} with headers: {}", message.getPayload(), message.getHeaders());
        streamBridge.send(MOVIES, message);
    }
}
