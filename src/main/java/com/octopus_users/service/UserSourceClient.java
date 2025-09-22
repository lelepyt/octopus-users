package com.octopus_users.service;

import com.octopus_users.model.UserDTO;

import java.util.List;
import java.util.Map;

public interface UserSourceClient {
    List<UserDTO> fetchUsers(Map<String, String> filters);
}