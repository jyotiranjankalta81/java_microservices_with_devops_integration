package com.jobintel.audit.web;

import com.jobintel.audit.domain.AuditRecord;
import com.jobintel.audit.repository.AuditRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditRecordRepository auditRecordRepository;

    @GetMapping
    public Page<AuditRecord> getAuditRecords(@PageableDefault(size = 20) Pageable pageable) {
        return auditRecordRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
}
