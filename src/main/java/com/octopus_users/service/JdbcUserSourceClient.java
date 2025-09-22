package com.octopus_users.service;

import com.octopus_users.config.DataSourcesProperties;
import com.octopus_users.model.UserDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JdbcUserSourceClient implements UserSourceClient {

    private final DataSourcesProperties.DataSourceConfig config;

    public JdbcUserSourceClient(DataSourcesProperties.DataSourceConfig config) {
        this.config = config;
    }

    private JdbcTemplate createJdbcTemplate() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setUrl(config.getUrl());
        ds.setUsername(config.getUser());
        ds.setPassword(config.getPassword());
        return new JdbcTemplate(ds);
    }

    @Override
    public List<UserDTO> fetchUsers(Map<String, String> filters) {
        JdbcTemplate jdbcTemplate = createJdbcTemplate();

        StringBuilder sql = new StringBuilder("SELECT * FROM " + config.getTable());

        List<Object> params = new ArrayList<>();

        if (filters != null && !filters.isEmpty()) {
            sql.append(" WHERE ");
            List<String> conditions = new ArrayList<>();

            filters.forEach((key, value) -> {
                String column = config.getMapping().get(key);
                if (column != null) {
                    conditions.add(column + " = ?");
                    params.add(value);
                }
            });

            sql.append(String.join(" AND ", conditions));
        }

        return jdbcTemplate.query(sql.toString(), params.toArray(), (rs, rowNum) -> {
            UserDTO user = new UserDTO();
            user.setId(rs.getString(config.getMapping().get("id")));
            user.setUsername(rs.getString(config.getMapping().get("username")));
            user.setName(rs.getString(config.getMapping().get("name")));
            user.setSurname(rs.getString(config.getMapping().get("surname")));
            return user;
        });
    }
}
