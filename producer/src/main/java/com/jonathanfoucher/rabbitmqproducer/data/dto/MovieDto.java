package com.jonathanfoucher.rabbitmqproducer.data.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MovieDto {
    private Long id;
    private String title;
    private LocalDate releaseDate;

    @Override
    public String toString() {
        if (title == null && releaseDate == null) {
            return String.format("{ id=%s }", id);
        }
        return String.format("{ id=%s, title=\"%s\", release_date=%s }", id, title, releaseDate);
    }
}
