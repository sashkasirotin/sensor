
package com.sensor.services.security.implementations;


import com.sensor.services.models.UserInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.security.KeyRep.Type.SECRET;

@Component
public class JwtApplicationService {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private final FileStorageService storage;

    //bad practice to save the key here better in secrets.yaml config map in key vault not hardcoded
    public static final String SECRET = "3f9a1c7d4e8b2f6a9c3d7e5f1b4a8d02e7c6f9a1d3b5e8f7c2a4d6e9b0f1c3d";
    public JwtApplicationService(FileStorageService storage) {
        this.storage = storage;
    }


    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }


    public String generateToken(String userName){
        Map<String,Object> claims=new HashMap<>();
        return createToken(claims,userName);
    }

    private String createToken(Map<String, Object> claims, String userName) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userName)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+1000*60*30))
                .signWith(getSignKey(), SignatureAlgorithm.HS256).compact();
    }

    private Key getSignKey() {
        byte[] keyBytes= Decoders.BASE64.decode(SECRET);
         return Keys.hmacShaKeyFor(keyBytes);
    }


    public UserInfo register(UserInfo userInfo) throws Exception {
        List<UserInfo> users = storage.loadUsers();

        // Check if user exists
        for (UserInfo u : users) {
            if (u.getName().equals(userInfo.getName())) {
                throw new Exception("User already exists");
            }
        }

        UserInfo newUser = new UserInfo(
                System.currentTimeMillis(),
                userInfo.getName(),
                passwordEncoder.encode(userInfo.getPassword()),
                "USER"
        );

        users.add(newUser);
        storage.saveUsers(users);

        return newUser;
    }

    public UserInfo authenticate(String username, String password) throws Exception {
        List<UserInfo> users = storage.loadUsers();

        for (UserInfo u : users) {
            if (u.getName().equals(username)) {
                if (passwordEncoder.matches(password, u.getPassword())) {
                    return u; // SUCCESS
                }
            }
        }

        return null; // failed
    }

    public List<UserInfo> getAllUsers() throws Exception {
        return storage.loadUsers();
    }

    public UserInfo findByUsername(String username) throws Exception {
        List<UserInfo> users = storage.loadUsers();
        for (UserInfo u : users) {
            if (u.getName().equals(username)) {
                return u;
            }
        }
        return null;
    }


}

