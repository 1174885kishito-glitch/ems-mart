package com.springmart.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenExpirationTest {

    @Test
    @DisplayName("有効期限内のJWTトークンは正常に検証（true）されるかを検証")
    void testValidateValidToken() {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", "thisisarandomsecretkeythatismorethan256bitslongfortestingpurposes1234567890");
        ReflectionTestUtils.setField(provider, "jwtExpiration", 3600000L); // 1時間

        String token = provider.generateToken("testUser", "ROLE_USER");
        assertNotNull(token);

        boolean isValid = provider.validateToken(token);
        assertTrue(isValid, "有効なトークンは true を返す必要があります");
    }

    @Test
    @DisplayName("有効期限切れのJWTトークンが正しく検証失敗（false）と判定されるかを検証")
    void testValidateExpiredToken() {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", "thisisarandomsecretkeythatismorethan256bitslongfortestingpurposes1234567890");
        ReflectionTestUtils.setField(provider, "jwtExpiration", -1000L); // 過去の有効期限を設定（-1秒前）

        // 強制的に過去の有効期限で作ったトークンを生成
        String expiredToken = provider.generateToken("testUser", "ROLE_USER");
        
        // 検証すると ExpiredJwtException が内部で発生 -> false が返ることを確認
        boolean isValid = provider.validateToken(expiredToken);
        
        assertFalse(isValid, "有効期限が切れたトークンは validateToken() で false を返す必要があります");
    }
}
