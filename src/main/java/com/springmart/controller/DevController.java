package com.springmart.controller;

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
}

