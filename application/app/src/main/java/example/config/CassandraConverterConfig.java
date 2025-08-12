package example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.cassandra.core.convert.CassandraCustomConversions;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;

@Configuration
public class CassandraConverterConfig {

    @Bean
    public CassandraCustomConversions cassandraCustomConversions() {
        return new CassandraCustomConversions(Arrays.asList(
                new OffsetDateTimeToInstantWritingConverter(),
                new InstantToOffsetDateTimeReadingConverter()
        ));
    }

    /**
     * Converts OffsetDateTime to Instant when writing to Cassandra
     * Cassandra stores timestamps as Instant (UTC)
     */
    @org.springframework.data.convert.WritingConverter
    public static class OffsetDateTimeToInstantWritingConverter implements Converter<OffsetDateTime, Instant> {
        @Override
        public Instant convert(OffsetDateTime source) {
            return source != null ? source.toInstant() : null;
        }
    }

    /**
     * Converts Instant to OffsetDateTime when reading from Cassandra
     * Assumes UTC timezone for consistency
     */
    @org.springframework.data.convert.ReadingConverter
    public static class InstantToOffsetDateTimeReadingConverter implements Converter<Instant, OffsetDateTime> {
        @Override
        public OffsetDateTime convert(Instant source) {
            return source != null ? source.atOffset(ZoneOffset.UTC) : null;
        }
    }
}