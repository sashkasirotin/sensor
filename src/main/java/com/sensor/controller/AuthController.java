package com.sensor.controller;

import com.sensor.dto.AuthenticationRequest;
import com.sensor.services.models.UserInfo;
import com.sensor.services.security.implementations.JwtApplicationService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/auth")
@OpenAPIDefinition(info = @Info(title = "Contact API", version = "1.0", description = "Manage authentication"))
@Tag(name = "Contacts", description = "Endpoints for managing authentication")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private JwtApplicationService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    public AuthController(JwtApplicationService jwtService) {
        this.jwtService = jwtService;
    }

    @Operation(summary = "Register a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered")
    })
    @PostMapping("/newUser")
    public UserInfo addNewUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "New user info avoid setting id and version",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserInfo.class)))
            @RequestBody UserInfo userInfo) throws Exception {
        logger.info("Registering new user: {}", userInfo.getName());
        return jwtService.register(userInfo);
    }

    @Operation(summary = "Authenticate user and return JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication successful"),
            @ApiResponse(responseCode = "403", description = "Invalid credentials")
    })
    @PostMapping("/authenticate")
    public String authenticateAndGetToken(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Username and password",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AuthenticationRequest.class)))
            @RequestBody AuthenticationRequest authRequest) {
        logger.info("Authenticating user: {}", authRequest.getUsername());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
        );
        if (authentication.isAuthenticated()) {
            logger.info("Authentication successful for user: {}", authRequest.getUsername());
            return jwtService.generateToken(authRequest.getUsername());
        } else {
            logger.warn("Authentication failed for user: {}", authRequest.getUsername());
            throw new UsernameNotFoundException("Invalid user request!");
        }
    }
}
