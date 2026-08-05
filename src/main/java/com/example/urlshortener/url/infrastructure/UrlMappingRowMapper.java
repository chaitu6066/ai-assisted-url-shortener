package com.example.urlshortener.url.infrastructure;

import com.example.urlshortener.url.domain.UrlMapping;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

@Component
public class UrlMappingRowMapper implements RowMapper<UrlMapping> {

    @Override
    public UrlMapping mapRow(
            ResultSet resultSet,
            int rowNumber
    ) throws SQLException {
        OffsetDateTime lastAccessedAt =
                resultSet.getObject(
                        "last_accessed_at",
                        OffsetDateTime.class
                );

        return new UrlMapping(
                resultSet.getLong("id"),
                resultSet.getString("short_code"),
                resultSet.getString("original_url"),
                resultSet.getObject(
                        "created_at",
                        OffsetDateTime.class
                ).toInstant(),
                lastAccessedAt == null
                        ? null
                        : lastAccessedAt.toInstant(),
                resultSet.getLong("click_count")
        );
    }
}
