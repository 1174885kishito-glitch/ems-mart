package com.springmart.controller;

import com.springmart.service.DevService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 開発用コントローラー（本番環境では削除または無効化すること）
 */
@RestController
@RequestMapping("/dev")
public class DevController {

    private final DevService devService;

    public DevController(DevService devService) {
        this.devService = devService;
    }
    
    @GetMapping("/hash-password")
    public ResponseEntity<Map<String, String>> hashPassword(@RequestParam String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode(password);
        
        Map<String, String> response = new HashMap<>();
        response.put("password", password);
        response.put("hash", hash);
        response.put("matches", String.valueOf(encoder.matches(password, hash)));
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/lock-test")
    public ResponseEntity<String> lockTest(
            @RequestParam(defaultValue = "1") Long productId,
            @RequestParam(defaultValue = "10000") long sleepMillis) {
        String result = devService.testPessimisticLock(productId, sleepMillis);
        return ResponseEntity.ok(result);
    }
}
