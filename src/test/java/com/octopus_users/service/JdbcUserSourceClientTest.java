package com.octopus_users.service;

import com.octopus_users.config.DataSourcesProperties;
import com.octopus_users.model.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class JdbcUserSourceClientTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass")
            .withEnv("TZ", "Europe/Kyiv");

    private JdbcTemplate jdbcTemplate;
    private DataSourcesProperties.DataSourceConfig config;

    @BeforeEach
    void setUp() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setUrl(postgres.getJdbcUrl());
        ds.setUsername(postgres.getUsername());
        ds.setPassword(postgres.getPassword());

        jdbcTemplate = new JdbcTemplate(ds);

        jdbcTemplate.execute("DROP TABLE IF EXISTS users");
        jdbcTemplate.execute("""
                    CREATE TABLE users (
                        id VARCHAR(50) PRIMARY KEY,
                        username VARCHAR(50),
                        name VARCHAR(50),
                        surname VARCHAR(50)
                    )
                """);

        jdbcTemplate.update("INSERT INTO users VALUES ('1', 'john', 'John', 'Doe')");
        jdbcTemplate.update("INSERT INTO users VALUES ('2', 'jane', 'Jane', 'Smith')");

        config = new DataSourcesProperties.DataSourceConfig();
        config.setName("test-db");
        config.setUrl(postgres.getJdbcUrl());
        config.setUser(postgres.getUsername());
        config.setPassword(postgres.getPassword());
        config.setTable("users");
        config.setMapping(Map.of(
                "id", "id",
                "username", "username",
                "name", "name",
                "surname", "surname"
        ));
    }

    @Test
    void fetchAllUsers() {
        JdbcUserSourceClient client = new JdbcUserSourceClient(config);
        List<UserDTO> users = client.fetchUsers(Map.of());

        assertThat(users).hasSize(2);
        assertThat(users).extracting("username").containsExactlyInAnyOrder("john", "jane");
    }

    @Test
    void fetchUsersWithFilter() {
        JdbcUserSourceClient client = new JdbcUserSourceClient(config);
        List<UserDTO> users = client.fetchUsers(Map.of("name", "John"));

        assertThat(users).hasSize(1);
        assertThat(users.get(0).getUsername()).isEqualTo("john");
    }
}