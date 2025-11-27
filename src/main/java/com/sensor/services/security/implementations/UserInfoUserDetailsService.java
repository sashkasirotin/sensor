package com.sensor.services.security.implementations;


import com.sensor.services.models.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class UserInfoUserDetailsService implements UserDetailsService {

    @Autowired
    private FileStorageService fileStorageService;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            List<UserInfo> users = fileStorageService.loadUsers();

            for (UserInfo u : users) {
                if (u.getName().equals(username)) {
                    return new org.springframework.security.core.userdetails.User(
                            u.getName(),
                            u.getPassword(),
                            List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole()))
                    );
                }
            }
            throw new UsernameNotFoundException("User not found");
        } catch (Exception e) {
            throw new UsernameNotFoundException("User not found");
        }
    }

}
