# **詳細設計書 (主要機能抜粋)**

## **1\. 注文機能 (F-201, F-202, F-203)**

### **1.1. クラス設計（役割）**

* **OrderController:**  
  * POST /api/orders のリクエストを受け取る。  
  * リクエストボディ (OrderRequest DTO) のバリデーション。  
  * OrderService を呼び出す。  
  * 結果をレスポンス (OrderResponse DTO) として返す。  
* **OrderService:**  
  * **@Transactional** を付与し、一連の処理をトランザクション管理する。  
  * createOrder メソッドがビジネスロジックの本体。  
  * InventoryRepository を使って在庫を確認・更新する。  
  * OrderRepository, OrderDetailRepository を使って注文情報をDBに保存する。  
* **InventoryRepository:**  
  * 在庫テーブル (INVENTORY) を操作する。  
  * **排他制御:** 在庫更新時には SELECT ... FOR UPDATE を使用し、行ロックを取得することで、同時更新による在庫の不整合を防ぐ。  
* **OrderRepository / OrderDetailRepository:**  
  * 注文ヘッダー・明細テーブルを操作する。

### **1.2. 注文処理シーケンス**

```mermaid
sequenceDiagram
    participant C as OrderController
    participant S as OrderService
    participant IR as InventoryRepository
    participant OR as OrderRepository

    C->>S: createOrder(requestDto)
    activate S
    Note over S: 1. トランザクション開始 (@Transactional)
    Note over S: 2. 注文商品リストをループ
    loop 各商品 (item)
        S->>IR: 3. 在庫確認＆ロック取得<br/>(findByIdForUpdate(item.productId))
        activate IR
        IR-->>S: 在庫情報 (Inventory)
        deactivate IR
        Note over S: 4. 在庫チェック<br/>(stock >= item.quantity)
        alt 在庫あり
            S->>IR: 5. 在庫更新 (update(newStock))
            activate IR
            IR-->>S: 更新完了
            deactivate IR
        else 在庫なし
            Note over S: 6. 例外スロー<br/>(OutOfStockException)
            Note over S: 7. トランザクション ロールバック
            S-->>C: エラーレスポンス
        end
    end
    alt 全商品在庫あり
        S->>OR: 8. 注文情報保存<br/>(save(Order, OrderDetails))
        activate OR
        OR-->>S: 保存完了
        deactivate OR
        Note over S: 9. トランザクション コミット
        S-->>C: 正常レスポンス
    end
    deactivate S
```

### **1.3. トランザクション・排他制御のポイント**

* **トランザクション:** OrderServiceのcreateOrderメソッドに@Transactionalアノテーションを付与する。これにより、処理中にRuntimeException（カスタム例外OutOfStockExceptionなど）が発生した場合、DB操作（在庫更新、注文登録）がすべてロールバックされる。  
* **排他制御:** InventoryRepositoryでの在庫読み取り時に、データベースの**悲観的ロック** (SELECT ... FOR UPDATE) を使用する。これにより、同じ商品に対する同時注文処理が発生した場合、先行するトランザクションが完了（コミット or ロールバック）するまで、後続の処理は在庫読み取りで待機させられる。