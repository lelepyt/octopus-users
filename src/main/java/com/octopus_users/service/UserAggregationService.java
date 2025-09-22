package com.octopus_users.service;

import com.octopus_users.config.DataSourcesProperties;
import com.octopus_users.model.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class UserAggregationService {

    private static final Logger log = LoggerFactory.getLogger(UserAggregationService.class);
    private final List<UserSourceClient> clients = new ArrayList<>();

    public UserAggregationService(DataSourcesProperties props) {
        if (props.getDataSources() != null) {
            for (var cfg : props.getDataSources()) {
                clients.add(new JdbcUserSourceClient(cfg));
                log.info("Added client for source: {}", cfg.getName());
            }
        }
    }

    public List<UserDTO> getAllUsers(Map<String, String> filters) {
        List<UserDTO> result = new ArrayList<>();
        for (UserSourceClient client : clients) {
            try {
                result.addAll(client.fetchUsers(filters));
            } catch (Exception e) {
                log.warn("Source failed: {}", e.getMessage());
            }
        }
        return result;
    }
}