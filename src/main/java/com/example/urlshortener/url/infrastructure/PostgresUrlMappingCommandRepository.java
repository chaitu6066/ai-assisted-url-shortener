package com.example.urlshortener.url.infrastructure;

import com.example.urlshortener.url.domain.UrlMapping;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class PostgresUrlMappingCommandRepository
        implements UrlMappingCommandRepository {

    private static final String INSERT_SQL = """
            INSERT INTO url_mapping (
                short_code,
                original_url
            )
            VALUES (
                :shortCode,
                :originalUrl
            )
            ON CONFLICT (short_code) DO NOTHING
            RETURNING
                id,
                short_code,
                original_url,
                created_at,
                last_accessed_at,
                click_count
            """;

    private static final String RECORD_CLICK_SQL = """
            UPDATE url_mapping
            SET
                click_count = click_count + 1,
                last_accessed_at = CURRENT_TIMESTAMP
            WHERE short_code = :shortCode
            RETURNING original_url
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final UrlMappingRowMapper rowMapper;

    public PostgresUrlMappingCommandRepository(
            NamedParameterJdbcTemplate jdbcTemplate,
            UrlMappingRowMapper rowMapper
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = rowMapper;
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

        return jdbcTemplate.query(
                INSERT_SQL,
                parameters,
                resultSet -> resultSet.next()
                        ? Optional.of(rowMapper.mapRow(resultSet, 0))
                        : Optional.empty()
        );
    }

    @Override
    public Optional<String> recordClickAndGetOriginalUrl(
            String shortCode
    ) {
        MapSqlParameterSource parameters =
                new MapSqlParameterSource()
                        .addValue("shortCode", shortCode);

        return jdbcTemplate.query(
                RECORD_CLICK_SQL,
                parameters,
                resultSet -> resultSet.next()
                        ? Optional.of(resultSet.getString("original_url"))
                        : Optional.empty()
        );
    }
}
