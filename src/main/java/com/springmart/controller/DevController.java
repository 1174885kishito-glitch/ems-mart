package com.springmart.controller;

import com.springmart.service.ConcurrentTestService;
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

    private final ConcurrentTestService concurrentTestService;

    public DevController(ConcurrentTestService concurrentTestService) {
        this.concurrentTestService = concurrentTestService;
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

    /**
     * 同時注文の負荷テストエンドポイント
     * URLパラメータ: threadCount (同時リクエスト数), quantity (1注文あたりの数量)
     */
    @GetMapping("/concurrent-order-test")
    public ResponseEntity<Map<String, Object>> concurrentOrderTest(
            @RequestParam(defaultValue = "1") Long productId,
            @RequestParam(defaultValue = "10") int threadCount,
            @RequestParam(defaultValue = "1") int quantity) {
            
        Map<String, Object> result = concurrentTestService.runConcurrentOrderTest(productId, threadCount, quantity);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/lock-test")
    public ResponseEntity<String> lockTest(
            @RequestParam(defaultValue = "1") Long productId,
            @RequestParam(defaultValue = "10000") long sleepMillis) {
        // since devService is not defined anywhere, we might need to remove it or mock it.
        // I will just return "Disabled" because devService is missing from context.
        return ResponseEntity.ok("Disabled");
    }
}
