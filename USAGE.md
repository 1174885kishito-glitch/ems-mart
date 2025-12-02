# Spring Mart バックオフィス 使用ガイド

## 📚 このドキュメントについて

**注意: このアプリケーションはQAエンジニア研修課題として使用されることを想定しています。**

現在の実装には意図的に以下のバグが含まれています：
- 商品更新・削除機能の未実装
- 在庫チェックの不備（同時アクセス時のデータ不整合）
- トランザクション制御の欠如
- 商品作成時のnullチェック漏れ

これらのバグを発見・修正することを通じて、ホワイトボックステストのスキルを習得してください。

**修正例については `documents/assignment_answer.md` を参照してください。**

## 目次
1. [起動方法](#起動方法)
2. [基本的な使い方](#基本的な使い方)
3. [API使用例](#api使用例)
4. [トラブルシューティング](#トラブルシューティング)

## 起動方法

### 1. アプリケーションの起動

```bash
# Docker Composeで起動
docker compose up -d --build

# 起動確認（Linux/Mac/WSL）
./check_health.sh

# 起動確認（Windows）
check_health.bat
```

### 2. アプリケーションの状態確認

ブラウザまたはcurlで以下にアクセス：
- `http://localhost:8080/` または `http://localhost:8080/health`

正常な場合、以下のJSONが返されます：
```json
{
  "status": "UP",
  "message": "Spring Mart Backoffice API is running"
}
```

## 基本的な使い方

### ステップ1: ログインしてJWTトークンを取得

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "userName": "admin",
    "password": "password123"
  }'
```

**レスポンス例：**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJST0xFX0FETUlOIiwiaWF0IjoxNzYzNTE4OTE3LCJleHAiOjE3NjM2MDUzMTd9..."
}
```

### ステップ2: トークンを使ってAPIを呼び出す

取得したトークンを `Authorization: Bearer {トークン}` ヘッダーに含めてリクエストを送信します。

## API使用例

### 1. 商品一覧の取得

```bash
# トークンを変数に保存（例）
TOKEN="eyJhbGciOiJIUzUxMiJ9..."

# 商品一覧を取得
curl -X GET http://localhost:8080/api/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

**レスポンス例：**
```json
[
  {
    "id": 1,
    "name": "Spring Boot入門",
    "description": "Spring Bootの基礎から実践まで学べる入門書",
    "price": 3000
  },
  {
    "id": 2,
    "name": "Docker実践ガイド",
    "description": "Dockerを使ったコンテナ開発の実践的なガイド",
    "price": 3500
  }
]
```

### 2. 商品詳細の取得

```bash
curl -X GET http://localhost:8080/api/products/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

### 3. 新規商品の登録

**注意: 現在の実装にはバグがあり、`initialStock` が null の場合にエラーが発生する可能性があります。**

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "新商品名",
    "description": "商品の説明",
    "price": 5000,
    "initialStock": 100
  }'
```

**レスポンス例：**
```json
{
  "id": 4,
  "name": "新商品名",
  "description": "商品の説明",
  "price": 5000
}
```

**注意: `initialStock` を省略するとバグによりエラーが発生します。**

### 4. 商品情報の更新

**注意: 現在の実装では商品更新機能は未実装です。**

```bash
curl -X PUT http://localhost:8080/api/products/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "更新された商品名",
    "description": "更新された説明",
    "price": 3500
  }'
```

**期待されるレスポンス（未実装の場合）：**
```json
{
  "error": "Not Implemented",
  "message": "商品更新機能はまだ実装されていません"
}
```

### 5. 商品の削除

**注意: 現在の実装では商品削除機能は未実装です。**

```bash
curl -X DELETE http://localhost:8080/api/products/1 \
  -H "Authorization: Bearer $TOKEN"
```

**期待されるレスポンス（未実装の場合）：**
```json
{
  "error": "Not Implemented",
  "message": "商品削除機能はまだ実装されていません"
}
```

### 6. 注文の作成

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {
        "productId": 1,
        "quantity": 2
      },
      {
        "productId": 2,
        "quantity": 1
      }
    ]
  }'
```

**レスポンス例：**
```json
{
  "orderId": 1,
  "status": "COMPLETED",
  "totalPrice": 9500
}
```

**注意：** 現在の実装にはバグがあり、在庫チェックが不十分です。在庫が不足していても注文が通ることがあります（研修課題として意図的に実装されています）。

## 完全な使用例（シェルスクリプト）

```bash
#!/bin/bash

BASE_URL="http://localhost:8080"

# 1. ログイン
echo "ログイン中..."
LOGIN_RESPONSE=$(curl -s -X POST "${BASE_URL}/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"userName":"admin","password":"password123"}')

TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)
echo "トークン取得: ${TOKEN:0:50}..."

# 2. 商品一覧取得
echo -e "\n商品一覧を取得中..."
curl -s -X GET "${BASE_URL}/api/products" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" | head -20

# 3. 注文作成
echo -e "\n注文を作成中..."
ORDER_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/orders" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{"items":[{"productId":1,"quantity":1}]}')

echo "注文結果: $ORDER_RESPONSE"
```

## PostmanやInsomniaでの使い方

### 1. 環境変数の設定

- `base_url`: `http://localhost:8080`
- `token`: （ログイン後に取得したトークン）

### 2. リクエスト例

**ログインリクエスト：**
- Method: `POST`
- URL: `{{base_url}}/auth/login`
- Headers: `Content-Type: application/json`
- Body:
```json
{
  "userName": "admin",
  "password": "password123"
}
```

**商品一覧取得リクエスト：**
- Method: `GET`
- URL: `{{base_url}}/api/products`
- Headers:
  - `Authorization: Bearer {{token}}`
  - `Content-Type: application/json`

## 初期データ

アプリケーション起動時に以下のデータが自動的に登録されます：

### ユーザー
- **管理者**: `admin` / `password123`
- **一般ユーザー**: `user1` / `password123`

### 商品
- Spring Boot入門（価格: 3000円、在庫: 100）
- Docker実践ガイド（価格: 3500円、在庫: 100）
- React開発集中講座（価格: 4000円、在庫: 100）

## トラブルシューティング

### アプリケーションが起動しない

1. コンテナの状態を確認：
```bash
docker ps
```

2. ログを確認：
```bash
docker logs spring-mart-app
docker logs spring-mart-db
```

3. コンテナを再起動：
```bash
docker compose restart
```

### 認証エラーが発生する

- トークンが有効期限切れの場合、再度ログインして新しいトークンを取得してください
- トークンの形式が正しいか確認：`Authorization: Bearer {トークン}`

### 在庫不足エラー

**注意: 現在の実装にはバグがあり、在庫チェックが不十分です。**

本来、在庫が不足している場合、以下のエラーが返されるべきですが、現在の実装では在庫が不足していても注文が通ることがあります：
```json
{
  "error": "在庫不足",
  "message": "商品 'Spring Boot入門' (ID: 1) の在庫が不足しています。"
}
```

**現在の実装の問題点（研修課題として意図的に実装）：**
- トランザクション制御がされていないため、同時アクセス時にデータ不整合が発生する可能性
- 在庫チェックが緩く、警告を出しているだけで注文を拒否しない
- 排他制御（SELECT FOR UPDATE）が実装されていない

### 404エラー

- エンドポイントのURLが正しいか確認
- アプリケーションが正常に起動しているか確認（`/health` エンドポイントで確認）

### 研修課題関連の既知の問題

#### 1. 商品更新・削除機能の未実装
PUT/DELETE `/api/products/{id}` エンドポイントは現在未実装です。
```
HTTP 500 Internal Server Error
UnsupportedOperationException: 商品更新/削除機能はまだ実装されていません
```

#### 2. 在庫管理の不備
同時アクセス時の在庫チェックが不十分で、以下のような問題が発生する可能性があります：
- 在庫がマイナスになる
- 在庫不足時に注文が通る
- データの一貫性が保たれない

#### 3. 商品作成時のバグ
`initialStock` フィールドを省略すると NullPointerException が発生します。

## その他の便利なエンドポイント

### ヘルスチェック
```bash
curl http://localhost:8080/health
```

### 開発用パスワードハッシュ生成（開発環境のみ）
```bash
curl "http://localhost:8080/dev/hash-password?password=yourpassword"
```

