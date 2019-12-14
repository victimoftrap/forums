package net.thumbtack.forums.controller;

import net.thumbtack.forums.exception.ServerException;
import net.thumbtack.forums.service.DebugService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/debug")
public class DebugController {
    private final DebugService debugService;

    @Autowired
    public DebugController(final DebugService debugService) {
        this.debugService = debugService;
    }

    @PostMapping("/clear")
    public ResponseEntity<Void> clearDatabase() throws ServerException {
        debugService.clearDatabase();
        return ResponseEntity
                .ok()
                .build();
    }
}
