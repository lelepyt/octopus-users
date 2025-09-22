package com.octopus_users.controller;

import com.octopus_users.model.UserDTO;
import com.octopus_users.service.UserAggregationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserAggregationService service;

    public UserController(UserAggregationService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(
            summary = "Retrieve a list of users",
            description = "Optional filters can be applied, e.g. ?name=John&surname=Doe"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful response"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public List<UserDTO> getUsers(
            @Parameter(description = "Filters for user search")
            @RequestParam Map<String, String> filters) {
        return service.getAllUsers(filters);
    }
}
