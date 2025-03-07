package com.jonathanfoucher.rabbitmqconsumer.services;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.jonathanfoucher.rabbitmqconsumer.data.dto.MovieDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringJUnitConfig(MovieConsumerService.class)
@SpringBootTest
class MovieConsumerServiceTest {
    @Autowired
    private MovieConsumerService movieConsumerService;

    private static final Logger log = (Logger) LoggerFactory.getLogger(MovieConsumerService.class);
    private static final ListAppender<ILoggingEvent> listAppender = new ListAppender<>();

    private static final String MOVIE_ID_HEADER_KEY = "movie_id";
    private static final String MOVIE_MESSAGE_TYPE_HEADER_KEY = "type";
    private static final Long ID = 15L;
    private static final String TITLE = "Some movie";
    private static final LocalDate RELEASE_DATE = LocalDate.of(2022, 7, 19);
    private static final String HEADER_UPDATE_TYPE = "UPDATE";
    private static final String HEADER_DELETE_TYPE = "DELETE";

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
    void consumeMovieUpdateMessage() {
        // GIVEN
        MovieDto movie = initMovie();
        Message<MovieDto> movieMessage = MessageBuilder.withPayload(movie)
                .setHeader(MOVIE_ID_HEADER_KEY, ID)
                .setHeader(MOVIE_MESSAGE_TYPE_HEADER_KEY, HEADER_UPDATE_TYPE)
                .build();

        // WHEN
        movieConsumerService.consumeMovie()
                .accept(movieMessage);

        // THEN
        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(1, logsList.size());
        assertNotNull(logsList.getFirst());
        assertEquals(Level.INFO, logsList.getFirst().getLevel());
        assertEquals(
                String.format(
                        "Received movie: { id=15, title=\"Some movie\", release_date=2022-07-19 } with headers: {id=%s, movie_id=15, type=UPDATE, timestamp=%s}",
                        movieMessage.getHeaders().getId(),
                        movieMessage.getHeaders().getTimestamp()
                ), logsList.getFirst().getFormattedMessage()
        );
    }

    @Test
    void consumeMovieDeleteMessage() {
        // GIVEN
        MovieDto movie = new MovieDto();
        movie.setId(ID);
        Message<MovieDto> movieMessage = MessageBuilder.withPayload(movie)
                .setHeader(MOVIE_ID_HEADER_KEY, ID)
                .setHeader(MOVIE_MESSAGE_TYPE_HEADER_KEY, HEADER_DELETE_TYPE)
                .build();

        // WHEN
        movieConsumerService.consumeMovie()
                .accept(movieMessage);

        // THEN
        List<ILoggingEvent> logsList = listAppender.list;
        assertEquals(1, logsList.size());
        assertNotNull(logsList.getFirst());
        assertEquals(Level.INFO, logsList.getFirst().getLevel());
        assertEquals(
                String.format(
                        "Received movie: { id=15 } with headers: {id=%s, movie_id=15, type=DELETE, timestamp=%s}",
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
