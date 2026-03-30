package com.springmart.service;

import com.springmart.entity.Inventory;
import com.springmart.repository.InventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 開発用サービス（本番環境では削除または無効化すること）
 */
@Service
public class DevService {
    
    private static final Logger log = LoggerFactory.getLogger(DevService.class);
    private final InventoryRepository inventoryRepository;

    public DevService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public String testPessimisticLock(Long productId, long sleepMillis) {
        log.info("スレッド {} が悲観的ロック取得を開始: productId={}", Thread.currentThread().getName(), productId);
        
        // 悲観的ロックを使用して在庫を取得 (SELECT ... FOR UPDATE が実行される)
        // 他のトランザクションが既にロックを取得している場合、ここで待機（ブロック）されます。
        Inventory inventory = inventoryRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new RuntimeException("商品が見つかりません: " + productId));
                
        log.info("スレッド {} が悲観的ロックを取得成功！現在の在庫: {}", Thread.currentThread().getName(), inventory.getStockQuantity());
        
        if (sleepMillis > 0) {
            try {
                log.info("スレッド {} が処理を休止（{}ミリ秒）... 他のトランザクションをブロックしています", Thread.currentThread().getName(), sleepMillis);
                Thread.sleep(sleepMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        log.info("スレッド {} の処理が完了し、ロックを解放します", Thread.currentThread().getName());
        return "処理完了（ロック解放） [productId=" + productId + "]";
    }
}
