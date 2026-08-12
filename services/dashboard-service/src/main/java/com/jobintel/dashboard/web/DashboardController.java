package com.jobintel.dashboard.web;

import com.jobintel.dashboard.dto.DashboardResponse;
import com.jobintel.dashboard.service.DashboardProjectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardProjectionService service;

    @GetMapping
    public DashboardResponse get(@RequestHeader("X-User-Id") UUID userId,
                                @RequestParam(name = "global", defaultValue = "false") boolean global) {
        if (global) {
            return service.getGlobalDashboard();
        }
        DashboardResponse res = service.getDashboard(userId);
        if (res.totalJobs() == 0) {
            return service.getGlobalDashboard();
        }
        return res;
    }
}
