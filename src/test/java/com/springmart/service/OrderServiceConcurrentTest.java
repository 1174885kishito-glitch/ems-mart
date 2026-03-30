package com.springmart.service;

import com.springmart.entity.Inventory;
import com.springmart.entity.Product;
import com.springmart.entity.User;
import com.springmart.repository.InventoryRepository;
import com.springmart.repository.ProductRepository;
import com.springmart.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OrderServiceConcurrentTest {

    @Autowired
    private ConcurrentTestService concurrentTestService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private UserRepository userRepository;

    private Long testProductId;

    @BeforeEach
    void setUp() {
        // data.sql によって初期データ(User, Product, Inventory)がセットアップされている前提
        testProductId = 1L; // "Spring Boot入門"
    }

    @AfterEach
    void tearDown() {
        // do nothing
    }

    @Test
    @DisplayName("複数スレッドからの同時注文で在庫が正常に引き当てられること（ペシミスティックロックの検証）")
    void testConcurrentOrder() {
        int threadCount = 10;
        int orderQuantity = 5;

        // 10スレッド同時に5個ずつ注文（計50個）
        Map<String, Object> result = concurrentTestService.runConcurrentOrderTest(testProductId, threadCount, orderQuantity);

        // 検証
        assertThat(result.get("3_totalRequests")).isEqualTo(10);
        assertThat(result.get("4_successCount")).isEqualTo(10);
        assertThat(result.get("5_failCount")).isEqualTo(0);
        
        // 100 - (10 * 5) = 50 になっているはず
        assertThat(result.get("6_finalStock")).isEqualTo(50);
        
        // 更なる在庫不足を含む同時注文テスト
        int overThreadCount = 15; 
        int overOrderQuantity = 5;
        // 今の在庫は50、75個注文しようとするので、50/5=10回だけ成功して5回は失敗するはず
        
        Map<String, Object> resultOver = concurrentTestService.runConcurrentOrderTest(testProductId, overThreadCount, overOrderQuantity);
        
        assertThat(resultOver.get("4_successCount")).isEqualTo(10);
        assertThat(resultOver.get("5_failCount")).isEqualTo(5);
        assertThat(resultOver.get("6_finalStock")).isEqualTo(0);
    }
}
