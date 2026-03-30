package com.springmart.service;

import com.springmart.dto.OrderItemRequest;
import com.springmart.dto.OrderRequest;
import com.springmart.entity.Inventory;
import com.springmart.repository.InventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.ArrayList;

import jakarta.persistence.EntityManager;

@Service
public class ConcurrentTestService {

    private static final Logger log = LoggerFactory.getLogger(ConcurrentTestService.class);

    private final OrderService orderService;
    private final InventoryRepository inventoryRepository;
    private final EntityManager entityManager;

    public ConcurrentTestService(OrderService orderService, InventoryRepository inventoryRepository, EntityManager entityManager) {
        this.orderService = orderService;
        this.inventoryRepository = inventoryRepository;
        this.entityManager = entityManager;
    }

    public Map<String, Object> runConcurrentOrderTest(Long productId, int threadCount, int orderQuantity) {
        log.info("注文の並行テストを開始します - productId: {}, スレッド数: {}, 1注文あたりの数量: {}", productId, threadCount, orderQuantity);
        
        Inventory initialInventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("商品が見つかりません"));
        int initialStock = initialInventory.getStockQuantity();
        
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();
        List<String> errorMessages = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    OrderRequest request = new OrderRequest();
                    OrderItemRequest item = new OrderItemRequest();
                    item.setProductId(productId);
                    item.setQuantity(orderQuantity);
                    request.setItems(Collections.singletonList(item));

                    // 注文処理（内部で在庫引き当てのトランザクションが走る）
                    orderService.createOrder(request);
                    successCount.incrementAndGet();
                    log.info("スレッド {} が注文に成功しました", Thread.currentThread().getName());
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    errorMessages.add(e.getMessage());
                    log.warn("スレッド {} が注文に失敗: {}", Thread.currentThread().getName(), e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await(); // 全てのスレッド（注文リクエスト）が終わるまで待機
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("テストが中断されました", e);
        } finally {
            executorService.shutdown();
        }

        // 全て終わったあとの最新の在庫を取得
        entityManager.clear(); // Spring BootのOpen EntityManager in Viewによる1次キャッシュの古いまんま（Stale）を破棄し、確実にDBから読む
        Inventory finalInventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("商品が見つかりません"));
        int finalStock = finalInventory.getStockQuantity();

        Map<String, Object> result = new HashMap<>();
        result.put("1_productId", productId);
        result.put("2_initialStock", initialStock);
        result.put("3_totalRequests", threadCount);
        result.put("4_successCount", successCount.get());
        result.put("5_failCount", failCount.get());
        result.put("6_finalStock", finalStock);
        result.put("7_errors", errorMessages);

        log.info("テスト完了 - 初期在庫: {}, 最終在庫: {}", initialStock, finalStock);
        return result;
    }
}
