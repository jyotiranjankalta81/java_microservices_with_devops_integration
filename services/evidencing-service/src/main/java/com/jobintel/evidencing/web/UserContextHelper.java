package com.jobintel.evidencing.web;

import com.jobintel.common.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class UserContextHelper {

    private final JwtTokenProvider jwtTokenProvider;

    // Default demo user (Sarah Jenkins, Teacher) if no header or token provided
    public static final UUID DEFAULT_DEMO_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    public UUID getUserId(HttpServletRequest request) {
        String userIdHeader = request.getHeader("X-User-Id");
        if (userIdHeader != null && !userIdHeader.isBlank()) {
            try {
                return UUID.fromString(userIdHeader.trim());
            } catch (Exception ignored) {
            }
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtTokenProvider.isValid(token)) {
                Claims claims = jwtTokenProvider.parseToken(token);
                try {
                    return UUID.fromString(claims.getSubject());
                } catch (Exception ignored) {
                }
            }
        }

        return DEFAULT_DEMO_USER_ID;
    }

    public List<String> getUserRoles(HttpServletRequest request) {
        String rolesHeader = request.getHeader("X-User-Roles");
        if (rolesHeader != null && !rolesHeader.isBlank()) {
            return parseRolesString(rolesHeader);
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtTokenProvider.isValid(token)) {
                Claims claims = jwtTokenProvider.parseToken(token);
                Object rolesClaim = claims.get("roles");
                if (rolesClaim != null) {
                    return parseRolesString(rolesClaim.toString());
                }
            }
        }

        return List.of("TEACHER");
    }

    private List<String> parseRolesString(String raw) {
        String clean = raw.replaceAll("[\\[\\]\"]", "");
        return Arrays.stream(clean.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
