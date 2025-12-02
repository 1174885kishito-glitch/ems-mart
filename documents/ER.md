# **ER図 (Entity-Relationship Diagram)**

```mermaid
erDiagram
    USERS {
        bigint id PK "自動採番"
        varchar user_name "ユーザー名 (ログインID)"
        varchar password "ハッシュ化パスワード"
        varchar role "権限 (ROLE_ADMIN, ROLE_USER)"
        timestamp created_at
    }

    PRODUCTS {
        bigint id PK "自動採番"
        varchar name "商品名"
        text description "商品説明"
        integer price "価格"
        timestamp created_at
    }

    INVENTORY {
        bigint product_id PK, FK "PRODUCTS(id)へのFK"
        integer stock_quantity "在庫数"
        timestamp updated_at
    }

    ORDERS {
        bigint id PK "自動採番"
        bigint user_id FK "USERS(id)へのFK"
        integer total_price "合計金額"
        varchar status "注文ステータス (e.g., PENDING, COMPLETED)"
        timestamp ordered_at "注文日時"
    }

    ORDER_DETAILS {
        bigint id PK "自動採番"
        bigint order_id FK "ORDERS(id)へのFK"
        bigint product_id FK "PRODUCTS(id)へのFK"
        integer quantity "数量"
        integer price_at_order "注文時価格"
    }

    USERS ||--o{ ORDERS : "has"
    ORDERS ||--|{ ORDER_DETAILS : "contains"
    PRODUCTS ||--|{ ORDER_DETAILS : "references"
    PRODUCTS ||--|| INVENTORY : "has one"
```

### **テーブル定義（抜粋）**

* **USERS (ユーザーテーブル):**  
  * role カラムで管理者(ADMIN)と一般ユーザー(USER)を区別する。  
* **PRODUCTS (商品マスタ):**  
  * 商品の基本情報。  
* **INVENTORY (在庫テーブル):**  
  * 商品の在庫数を管理。product\_id で商品マスタと1:1で紐づく。  
* **ORDERS (注文ヘッダー):**  
  * 1回の注文情報を管理。  
* **ORDER\_DETAILS (注文明細):**  
  * 1回の注文に含まれる商品ごとの明細。price\_at\_order で注文時点の価格を保持する（商品マスタの価格が変更されても影響を受けないため）。