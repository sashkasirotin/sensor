package com.sensor.services.models;

//import jakarta.persistence.*;

//@Entity
//@Table(name = "users")
public class UserInfo {
    private Long id;
    private String username;
    private String passwordHash;
    private String role;

    public UserInfo() {
    }
    public UserInfo(Long id, String username, String passwordHash, String role) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }


    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPassword() {
        return passwordHash;
    }

    public void setPassword(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getName() {
        return username;
    }

    public void setName(String name) {
        this.username = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}

