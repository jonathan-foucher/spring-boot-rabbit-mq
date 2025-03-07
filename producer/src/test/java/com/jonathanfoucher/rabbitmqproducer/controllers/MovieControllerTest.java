package com.jonathanfoucher.rabbitmqproducer.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jonathanfoucher.rabbitmqproducer.data.dto.MovieDto;
import com.jonathanfoucher.rabbitmqproducer.services.MovieProducerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;

import static com.jonathanfoucher.rabbitmqproducer.data.enums.MovieMessageType.DELETE;
import static com.jonathanfoucher.rabbitmqproducer.data.enums.MovieMessageType.UPDATE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitConfig(MovieController.class)
class MovieControllerTest {
    private MockMvc mockMvc;
    @Autowired
    private MovieController movieController;
    @MockitoBean
    private MovieProducerService movieProducerService;

    private static final String MOVIES_PATH = "/movies";
    private static final String MOVIES_WITH_ID_PATH = "/movies/{movie_id}";
    private static final String MOVIE_ID_HEADER_KEY = "movie_id";
    private static final String MOVIE_MESSAGE_TYPE_HEADER_KEY = "type";
    private static final Long ID = 15L;
    private static final String TITLE = "Some movie";
    private static final LocalDate RELEASE_DATE = LocalDate.of(2022, 7, 19);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @BeforeEach
    void initEach() {
        mockMvc = MockMvcBuilders.standaloneSetup(movieController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    @Test
    void saveMovie() throws Exception {
        // GIVEN
        MovieDto movie = new MovieDto();
        movie.setId(ID);
        movie.setTitle(TITLE);
        movie.setReleaseDate(RELEASE_DATE);

        // WHEN / THEN
        mockMvc.perform(post(MOVIES_PATH)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movie))
                )
                .andExpect(status().isOk());

        ArgumentCaptor<Message<MovieDto>> movieMessageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(movieProducerService, times(1)).sendMovieMessage(movieMessageCaptor.capture());

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
    }

    @Test
    void deleteMovie() throws Exception {
        // WHEN / THEN
        mockMvc.perform(delete(MOVIES_WITH_ID_PATH, ID))
                .andExpect(status().isOk());

        ArgumentCaptor<Message<MovieDto>> movieMessageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(movieProducerService, times(1)).sendMovieMessage(movieMessageCaptor.capture());

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
    }
}
