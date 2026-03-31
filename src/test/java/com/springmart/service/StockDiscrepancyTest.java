package com.springmart.service;

import com.springmart.dto.OrderItemRequest;
import com.springmart.dto.OrderRequest;
import com.springmart.entity.Inventory;
import com.springmart.repository.InventoryRepository;
import com.springmart.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.security.test.context.support.WithMockUser;

import jakarta.persistence.EntityManager;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class StockDiscrepancyTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    @DisplayName("在庫ズレが発生しないことを検証するテスト（ペシミスティックロック）")
    @WithMockUser(username = "user1")
    void verifyNoStockDiscrepancy() throws InterruptedException {
        // data.sql によって初期データとして ID=1 の商品の在庫が 100 ある前提
        Long productId = 1L;
        int threadCount = 20;
        int quantityPerOrder = 2; // 計 40 注文

        // テスト前の在庫数を確認
        Inventory initialInventory = inventoryRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("商品が見つかりません"));
        int startStock = initialInventory.getStockQuantity();

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // concurrent execution
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    OrderRequest request = new OrderRequest();
                    OrderItemRequest item = new OrderItemRequest();
                    item.setProductId(productId);
                    item.setQuantity(quantityPerOrder);
                    request.setItems(Collections.singletonList(item));

                    // 注文作成
                    orderService.createOrder(request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // テスト後の在庫数をDBから再度最新として取得
        // EntityManagerのキャッシュをクリアして確実にDBの値を読む
        transactionTemplate.execute(status -> {
            entityManager.clear();
            Inventory finalInventory = inventoryRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("商品が見つかりません"));
            
            int finalStock = finalInventory.getStockQuantity();
            
            // 成功した注文数 分の在庫が引かれていること（在庫ズレがないこと）を確認
            int expectedStock = startStock - (successCount.get() * quantityPerOrder);
            
            assertThat(finalStock)
                .as("在庫ズレの検証: 開始在庫(%d) - 成功注文(%d) * 数量(%d) = 期待在庫(%d) 対して 実際の在庫(%d)",
                     startStock, successCount.get(), quantityPerOrder, expectedStock, finalStock)
                .isEqualTo(expectedStock);
                
            return null;
        });
    }
}
