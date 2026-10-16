package com.university.booking.config;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import jakarta.annotation.Nonnull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

@Configuration
public class MongoConfig {

    @Bean
    public MongoTransactionManager transactionManager(MongoDatabaseFactory databaseFactory) {
        return new MongoTransactionManager(databaseFactory);
    }

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(List.of(LocalTimeToString.INSTANCE, StringToLocalTime.INSTANCE));
    }

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    @WritingConverter
    enum LocalTimeToString implements Converter<LocalTime, String> {
        INSTANCE;

        @Override
        public String convert(LocalTime source) {
            return source.format(TIME_FORMAT);
        }
    }

    @ReadingConverter
    enum StringToLocalTime implements Converter<String, LocalTime> {
        INSTANCE;

        @Override
        public LocalTime convert(@Nonnull String source) {
            return LocalTime.parse(source);
        }
    }
}