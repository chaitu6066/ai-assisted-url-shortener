package com.example.urlshortener.url.infrastructure;

import com.example.urlshortener.url.domain.UrlMapping;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PostgresUrlMappingCommandRepository
        implements UrlMappingCommandRepository {

    private static final String INSERT_SQL =
            "INSERT INTO url_mapping "
            + "(short_code, original_url) "
            + "VALUES (:shortCode, :originalUrl) "
            + "ON CONFLICT (short_code) DO NOTHING "
            + "RETURNING id, short_code, original_url, "
            + "created_at, last_accessed_at, click_count";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final UrlMappingRowMapper rowMapper =
            new UrlMappingRowMapper();

    public PostgresUrlMappingCommandRepository(
            NamedParameterJdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<UrlMapping> insertIfAbsent(
            String shortCode,
            String originalUrl
    ) {
        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue("shortCode", shortCode)
                        .addValue("originalUrl", originalUrl);

        List<UrlMapping> insertedRows =
                jdbcTemplate.query(
                        INSERT_SQL,
                        parameters,
                        rowMapper
                );

        return insertedRows.stream().findFirst();
    }
}
