package com.sensor.services.security.config;

import com.sensor.services.security.filter.JwtAuthenticationFilter;
import com.sensor.services.security.implementations.UserInfoUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
public class ServicesConfig {
    @Bean
    @Primary
    public JwtAuthenticationFilter JwtAuthenticationFilter(){
        return new JwtAuthenticationFilter();
    }

    @Bean
    @Primary
    //authentication
    public UserDetailsService userDetailsService() {
        return new UserInfoUserDetailsService();
    }
}
