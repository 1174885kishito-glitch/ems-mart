package com.springmart.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * パスワードハッシュ生成用のユーティリティクラス
 * 開発用：正しいBCryptハッシュを生成するために使用
 */
public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "password123";
        String hash = encoder.encode(password);
        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);
        
        // 検証
        boolean matches = encoder.matches(password, hash);
        System.out.println("Matches: " + matches);
    }
}

