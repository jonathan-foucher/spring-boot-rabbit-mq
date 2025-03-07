package com.jonathanfoucher.rabbitmqproducer.controllers;

import com.jonathanfoucher.rabbitmqproducer.data.dto.MovieDto;
import com.jonathanfoucher.rabbitmqproducer.data.enums.MovieMessageType;
import com.jonathanfoucher.rabbitmqproducer.services.MovieProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.*;

import static com.jonathanfoucher.rabbitmqproducer.data.enums.MovieMessageType.DELETE;
import static com.jonathanfoucher.rabbitmqproducer.data.enums.MovieMessageType.UPDATE;

@RequiredArgsConstructor
@RestController
@RequestMapping("/movies")
public class MovieController {
    private final MovieProducerService movieProducerService;

    @PostMapping
    public void saveMovie(@RequestBody MovieDto movie) {
        sendMovieMessage(movie.getId(), movie, UPDATE);
    }

    @DeleteMapping("/{movie_id}")
    public void deleteMovie(@PathVariable("movie_id") Long movieId) {
        sendMovieMessage(movieId, null, DELETE);
    }

    private void sendMovieMessage(Long movieId, MovieDto movie, MovieMessageType messageType) {
        Message<MovieDto> movieMessage = MessageBuilder.withPayload(movie != null ? movie : initMovieFromId(movieId))
                .setHeader("movie_id", movieId)
                .setHeader("type", messageType.name())
                .build();

        movieProducerService.sendMovieMessage(movieMessage);
    }

    private MovieDto initMovieFromId(Long movieId) {
        MovieDto movie = new MovieDto();
        movie.setId(movieId);
        return movie;
    }
}
