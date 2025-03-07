package com.jonathanfoucher.rabbitmqproducer.services;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.jonathanfoucher.rabbitmqproducer.data.dto.MovieDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDate;
import java.util.List;

import static com.jonathanfoucher.rabbitmqproducer.data.enums.MovieMessageType.DELETE;
import static com.jonathanfoucher.rabbitmqproducer.data.enums.MovieMessageType.UPDATE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringJUnitConfig(MovieProducerService.class)
@SpringBootTest
class MovieProducerServiceTest {
    @Autowired
    private MovieProducerService movieProducerService;
    @MockitoBean
    private StreamBridge streamBridge;

    private static final Logger log = (Logger) LoggerFactory.getLogger(MovieProducerService.class);
    private static final ListAppender<ILoggingEvent> listAppender = new ListAppender<>();

    private static final String MOVIE_QUEUE_NAME = "movies-out-0";
    private static final String MOVIE_ID_HEADER_KEY = "movie_id";
    private static final String MOVIE_MESSAGE_TYPE_HEADER_KEY = "type";
    private static final Long ID = 15L;
    private static final String TITLE = "Some movie";
    private static final LocalDate RELEASE_DATE = LocalDate.of(2022, 7, 19);

    @BeforeEach
    void init() {
        listAppender.list.clear();
        listAppender.start();
        log.addAppender(listAppender);
    }

    @AfterEach
    void reset() {
        log.detachAppender(listAppender);
        listAppender.stop();
    }

    @Test
    void sendMovieUpdateMessage() {
        // GIVEN
        MovieDto movie = initMovie();
        Message<MovieDto> movieMessage = MessageBuilder.withPayload(movie)
                .setHeader(MOVIE_ID_HEADER_KEY, ID)
                .setHeader(MOVIE_MESSAGE_TYPE_HEADER_KEY, UPDATE.name())
                .build();

        // WHEN
        movieProducerService.sendMovieMessage(movieMessage);

        // THEN
        ArgumentCaptor<Message<MovieDto>> movieMessageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(streamBridge, times(1)).send(eq(MOVIE_QUEUE_NAME), movieMessageCaptor.capture());

        Message<MovieDto> savedMovieMessage = movieMessageCaptor.getValue();
        assertNotNull(savedMovieMessage);

        MovieDto messagePayload = savedMovieMessage.getPayload();
        assertNotNull(messagePayload);
        assertEquals(ID, messagePayload.getId());
        assertEquals(TITLE, messagePayload.getTitle());
        assertEquals(RELEASE_DATE, messagePayload.getReleaseDate());

        MessageHeaders messageHeaders = savedMovieMessage.getHeaders();
        assertNotNull(messageHeaders);
        assertNotNull(messageHeaders.getId());
        assertNotNull(messageHeaders.getTimestamp());
        assertEquals(ID, messageHeaders.get(MOVIE_ID_HEADER_KEY));
        assertEquals(UPDATE.name(), messageHeaders.get(MOVIE_MESSAGE_TYPE_HEADER_KEY));

        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(1, logsList.size());
        assertNotNull(logsList.getFirst());
        assertEquals(Level.INFO, logsList.getFirst().getLevel());
        assertEquals(
                String.format(
                        "Sending movie: { id=15, title=\"Some movie\", release_date=2022-07-19 } with headers: {id=%s, movie_id=15, type=UPDATE, timestamp=%s}",
                        movieMessage.getHeaders().getId(),
                        movieMessage.getHeaders().getTimestamp()
                ), logsList.getFirst().getFormattedMessage()
        );
    }

    @Test
    void sendMovieDeleteMessage() {
        // GIVEN
        MovieDto movie = new MovieDto();
        movie.setId(ID);
        Message<MovieDto> movieMessage = MessageBuilder.withPayload(movie)
                .setHeader(MOVIE_ID_HEADER_KEY, ID)
                .setHeader(MOVIE_MESSAGE_TYPE_HEADER_KEY, DELETE.name())
                .build();

        // WHEN
        movieProducerService.sendMovieMessage(movieMessage);

        // THEN
        ArgumentCaptor<Message<MovieDto>> movieMessageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(streamBridge, times(1)).send(eq(MOVIE_QUEUE_NAME), movieMessageCaptor.capture());

        Message<MovieDto> deletedMovieMessage = movieMessageCaptor.getValue();
        assertNotNull(deletedMovieMessage);

        MovieDto deletedMovie = deletedMovieMessage.getPayload();
        assertNotNull(deletedMovie);
        assertEquals(ID, deletedMovie.getId());
        assertNull(deletedMovie.getTitle());
        assertNull(deletedMovie.getReleaseDate());

        MessageHeaders messageHeaders = deletedMovieMessage.getHeaders();
        assertNotNull(messageHeaders);
        assertNotNull(messageHeaders.getId());
        assertNotNull(messageHeaders.getTimestamp());
        assertEquals(ID, messageHeaders.get(MOVIE_ID_HEADER_KEY));
        assertEquals(DELETE.name(), messageHeaders.get(MOVIE_MESSAGE_TYPE_HEADER_KEY));

        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(1, logsList.size());
        assertNotNull(logsList.getFirst());
        assertEquals(Level.INFO, logsList.getFirst().getLevel());
        assertEquals(
                String.format(
                        "Sending movie: { id=15 } with headers: {id=%s, movie_id=15, type=DELETE, timestamp=%s}",
                        movieMessage.getHeaders().getId(),
                        movieMessage.getHeaders().getTimestamp()
                ), logsList.getFirst().getFormattedMessage()
        );
    }

    private MovieDto initMovie() {
        MovieDto movie = new MovieDto();
        movie.setId(ID);
        movie.setTitle(TITLE);
        movie.setReleaseDate(RELEASE_DATE);
        return movie;
    }
}
