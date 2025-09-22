package com.octopus_users.service;

import com.octopus_users.config.DataSourcesProperties;
import com.octopus_users.model.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAggregationServiceTest {

    @Mock
    private DataSourcesProperties dataSourcesProperties;

    private UserAggregationService userAggregationService;

    @BeforeEach
    void setUp() {
        List<DataSourcesProperties.DataSourceConfig> configs = Arrays.asList(
                createMockConfig("source1"),
                createMockConfig("source2")
        );
        when(dataSourcesProperties.getDataSources()).thenReturn(configs);
    }

    @Test
    void shouldCreateJdbcClients_whenDataSourcesAreProvided() {
        try (MockedConstruction<JdbcUserSourceClient> mockedConstruction =
                     mockConstruction(JdbcUserSourceClient.class)) {

            userAggregationService = new UserAggregationService(dataSourcesProperties);

            assertEquals(2, mockedConstruction.constructed().size());
        }
    }

    @Test
    void shouldNotThrow_whenDataSourcesAreNull() {
        when(dataSourcesProperties.getDataSources()).thenReturn(null);

        assertDoesNotThrow(() -> {
            userAggregationService = new UserAggregationService(dataSourcesProperties);
        });
    }

    @Test
    void shouldAggregateUsersFromMultipleSources() {
        List<UserDTO> users1 = List.of(
                createTestUser("1", "john_doe", "John", "Doe")
        );
        List<UserDTO> users2 = List.of(
                createTestUser("2", "jane_smith", "Jane", "Smith")
        );

        try (MockedConstruction<JdbcUserSourceClient> ignored =
                     mockConstruction(JdbcUserSourceClient.class, (mock, context) -> {
                         if (context.getCount() == 1) {
                             when(mock.fetchUsers(any())).thenReturn(users1);
                         } else {
                             when(mock.fetchUsers(any())).thenReturn(users2);
                         }
                     })) {

            userAggregationService = new UserAggregationService(dataSourcesProperties);
            Map<String, String> filters = new HashMap<>();

            List<UserDTO> result = userAggregationService.getAllUsers(filters);

            assertEquals(2, result.size());
            assertTrue(result.stream().anyMatch(u -> "john_doe".equals(u.getUsername())));
            assertTrue(result.stream().anyMatch(u -> "jane_smith".equals(u.getUsername())));
        }
    }

    @Test
    void shouldPassFiltersToAllClients_whenFiltersAreProvided() {
        Map<String, String> filters = new HashMap<>();
        filters.put("name", "John");
        filters.put("surname", "Doe");

        List<UserDTO> expectedUsers = Arrays.asList(
                createTestUser("1", "john_doe", "John", "Doe")
        );

        try (MockedConstruction<JdbcUserSourceClient> ignored =
                     mockConstruction(JdbcUserSourceClient.class, (mock, context) -> {
                         if (context.getCount() == 1) {
                             when(mock.fetchUsers(filters)).thenReturn(List.of(
                                     createTestUser("1", "john_doe", "John", "Doe")
                             ));
                         } else {
                             when(mock.fetchUsers(filters)).thenReturn(List.of());
                         }
                     })) {
            userAggregationService = new UserAggregationService(dataSourcesProperties);

            List<UserDTO> result = userAggregationService.getAllUsers(filters);

            assertEquals(1, result.size());
            assertEquals("John", result.get(0).getName());
            assertEquals("Doe", result.get(0).getSurname());

            for (JdbcUserSourceClient client : ignored.constructed()) {
                verify(client).fetchUsers(filters);
            }
        }
    }

    @Test
    void shouldContinueAggregation_whenOneClientFails() {
        List<UserDTO> users2 = Arrays.asList(
                createTestUser("2", "jane_smith", "Jane", "Smith")
        );

        try (MockedConstruction<JdbcUserSourceClient> ignored =
                     mockConstruction(JdbcUserSourceClient.class, (mock, context) -> {
                         if (context.getCount() == 1) {
                             when(mock.fetchUsers(any())).thenThrow(new RuntimeException("Database connection failed"));
                         } else {
                             when(mock.fetchUsers(any())).thenReturn(users2);
                         }
                     })) {
            userAggregationService = new UserAggregationService(dataSourcesProperties);
            List<UserDTO> result = userAggregationService.getAllUsers(new HashMap<>());

            assertEquals(1, result.size());
            assertEquals("jane_smith", result.get(0).getUsername());
        }
    }

    @Test
    void shouldReturnEmptyList_whenAllClientsFail() {
        try (MockedConstruction<JdbcUserSourceClient> mockedConstruction =
                     mockConstruction(JdbcUserSourceClient.class, (mock, context) -> {
                         when(mock.fetchUsers(any())).thenThrow(new RuntimeException("Database error"));
                     })) {

            userAggregationService = new UserAggregationService(dataSourcesProperties);
            Map<String, String> filters = new HashMap<>();

            List<UserDTO> result = userAggregationService.getAllUsers(filters);

            assertTrue(result.isEmpty());
        }
    }

    @Test
    void shouldReturnAllUsers_whenFiltersAreEmpty() {
        List<UserDTO> allUsers = Arrays.asList(
                createTestUser("1", "john_doe", "John", "Doe"),
                createTestUser("2", "jane_smith", "Jane", "Smith")
        );

        try (MockedConstruction<JdbcUserSourceClient> mockedConstruction =
                     mockConstruction(JdbcUserSourceClient.class, (mock, context) -> {
                         when(mock.fetchUsers(any())).thenReturn(allUsers);
                     })) {

            userAggregationService = new UserAggregationService(dataSourcesProperties);
            Map<String, String> emptyFilters = new HashMap<>();

            List<UserDTO> result = userAggregationService.getAllUsers(emptyFilters);

            assertEquals(4, result.size());
        }
    }

    private DataSourcesProperties.DataSourceConfig createMockConfig(String name) {
        DataSourcesProperties.DataSourceConfig config = new DataSourcesProperties.DataSourceConfig();
        config.setName(name);
        config.setUrl("jdbc:postgresql://localhost:5432/test");
        config.setUser("testuser");
        config.setPassword("testpass");
        config.setTable("users");

        Map<String, String> mapping = new HashMap<>();
        mapping.put("id", "user_id");
        mapping.put("username", "login");
        mapping.put("name", "first_name");
        mapping.put("surname", "last_name");
        config.setMapping(mapping);

        return config;
    }

    private UserDTO createTestUser(String id, String username, String name, String surname) {
        UserDTO user = new UserDTO();
        user.setId(id);
        user.setUsername(username);
        user.setName(name);
        user.setSurname(surname);
        return user;
    }
}