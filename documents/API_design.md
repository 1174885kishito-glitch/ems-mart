# **API仕様書 (抜粋)**

(※Step 3でSwagger UIに移行するまでの叩き台)

## **認証 API**

### **POST /auth/login**

**概要:** ログインし、JWTトークンを取得する。

**リクエストボディ:**

{  
  "userName": "admin",  
  "password": "password123"  
}

**レスポンス (200 OK):**

{  
  "token": "eyJh...\[JWT Token\]...c4"  
}

## **商品管理 API (要認証)**

**認証:** Authorization: Bearer \[JWT Token\] がヘッダーに必要。

### **GET /api/products**

**概要:** 商品一覧を取得する。

**レスポンス (200 OK):**

\[  
  {  
    "id": 1,  
    "name": "Spring Boot入門",  
    "price": 3000  
  },  
  {  
    "id": 2,  
    "name": "Docker実践ガイド",  
    "price": 3500  
  }  
\]

### **POST /api/products**

**概要:** 新規商品を登録する。

**リクエストボディ:**

{  
  "name": "React開発集中講座",  
  "description": "モダンフロントエンド...",  
  "price": 4000,  
  "initialStock": 50  
}

*(Note: initialStock は在庫テーブル(INVENTORY)への登録に使用)*

**レスポンス (201 Created):**

{  
  "id": 3,  
  "name": "React開発集中講座",  
  "price": 4000  
}

## **注文 API (要認証)**

### **POST /api/orders**

**概要:** 新規注文を作成する。（Step 2の最重要機能）

**リクエストボディ:**

{  
  "userId": 1, // 実際は認証トークンから取得  
  "items": \[  
    {  
      "productId": 1,  
      "quantity": 2  
    },  
    {  
      "productId": 3,  
      "quantity": 1  
    }  
  \]  
}

**レスポンス (201 Created):**

{  
  "orderId": 101,  
  "status": "COMPLETED",  
  "totalPrice": 10000 // (3000 \* 2\) \+ (4000 \* 1\)  
}

**エラーレスポンス (400 Bad Request):**

* 在庫が不足している場合

{  
  "error": "在庫不足",  
  "message": "商品 'Spring Boot入門' (ID: 1\) の在庫が不足しています。"  
}  
