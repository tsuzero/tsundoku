package com.tsuzero.tsundoku.sync;

import com.tsuzero.tsundoku.goodreads.GoodreadsSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private final SyncLogRepository syncLogRepository;
    private final GoodreadsSyncService goodreadsSyncService;

    public SyncController(SyncLogRepository syncLogRepository,
                          GoodreadsSyncService goodreadsSyncService) {
        this.syncLogRepository = syncLogRepository;
        this.goodreadsSyncService = goodreadsSyncService;
    }

    @GetMapping("/status")
    public List<SyncLog> status() {
        return syncLogRepository.findLatestPerType();
    }

    @PostMapping("/goodreads")
    public ResponseEntity<SyncLog> triggerGoodreadsSync() {
        SyncLog result = goodreadsSyncService.sync();
        return ResponseEntity.ok(result);
    }
}
