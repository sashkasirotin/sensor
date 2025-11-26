package com.sensor.services.security.implementations;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sensor.services.models.UserInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileStorageService {
    private final ObjectMapper mapper = new ObjectMapper();
    private final File file = new File("users.json");

    public List<UserInfo> loadUsers() throws Exception {
        if (!file.exists()) {
            file.createNewFile();
            mapper.writeValue(file, new ArrayList<UserInfo>());
        }
        return mapper.readValue(file, new TypeReference<List<UserInfo>>() {});
    }

    public void saveUsers(List<UserInfo> users) throws Exception {
        mapper.writeValue(file, users);
    }
}
