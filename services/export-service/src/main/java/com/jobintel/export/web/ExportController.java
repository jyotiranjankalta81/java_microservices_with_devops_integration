package com.jobintel.export.web;

import com.jobintel.export.dto.*;
import com.jobintel.export.service.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {
    private final ExportService exportService;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ExportStatusResponse request(@RequestHeader("X-User-Id") UUID userId,
                                        @RequestBody ExportRequest request) throws Exception {
        return exportService.requestExport(userId, request);
    }

    @GetMapping
    public List<ExportStatusResponse> history(@RequestHeader("X-User-Id") UUID userId) {
        return exportService.history(userId);
    }

    @GetMapping("/{id}")
    public ExportStatusResponse status(@RequestHeader("X-User-Id") UUID userId, @PathVariable UUID id) {
        return exportService.status(userId, id);
    }
}
