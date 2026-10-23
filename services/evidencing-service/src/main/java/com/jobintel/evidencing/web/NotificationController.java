package com.jobintel.evidencing.web;

import com.jobintel.evidencing.dto.NotificationResponse;
import com.jobintel.evidencing.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/evidencing/notifications")
@RequiredArgsConstructor
@Tag(name = "Evidencing Notifications", description = "Endpoints for viewing and managing audience notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserContextHelper userContextHelper;

    @GetMapping
    @Operation(summary = "Get in-app notifications for current user")
    public List<NotificationResponse> getNotifications(HttpServletRequest httpRequest) {
        UUID userId = userContextHelper.getUserId(httpRequest);
        return notificationService.getUserNotifications(userId);
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get count of unread notifications for current user")
    public Map<String, Long> getUnreadCount(HttpServletRequest httpRequest) {
        UUID userId = userContextHelper.getUserId(httpRequest);
        long count = notificationService.getUnreadCount(userId);
        return Map.of("unreadCount", count);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read")
    public void markAsRead(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {
        UUID userId = userContextHelper.getUserId(httpRequest);
        notificationService.markAsRead(id, userId);
    }

    @PostMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public void markAllAsRead(HttpServletRequest httpRequest) {
        UUID userId = userContextHelper.getUserId(httpRequest);
        notificationService.markAllAsRead(userId);
    }
}
