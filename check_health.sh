#!/bin/bash

# Spring Mart バックオフィス ヘルスチェックスクリプト
# 使用方法: ./check_health.sh

set -e

# カラー定義
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

BASE_URL="http://localhost:8080"
MAX_RETRIES=30
RETRY_INTERVAL=2

echo "=========================================="
echo "Spring Mart バックオフィス ヘルスチェック"
echo "=========================================="
echo ""

# 1. Dockerコンテナの状態確認
echo -e "${YELLOW}[1/5] Dockerコンテナの状態確認${NC}"
if command -v docker &> /dev/null; then
    if docker ps | grep -q "spring-mart-db"; then
        echo -e "${GREEN}✓ データベースコンテナ (spring-mart-db) が起動しています${NC}"
    else
        echo -e "${RED}✗ データベースコンテナ (spring-mart-db) が起動していません${NC}"
        exit 1
    fi
    
    if docker ps | grep -q "spring-mart-app"; then
        echo -e "${GREEN}✓ アプリケーションコンテナ (spring-mart-app) が起動しています${NC}"
    else
        echo -e "${RED}✗ アプリケーションコンテナ (spring-mart-app) が起動していません${NC}"
        exit 1
    fi
else
    echo -e "${YELLOW}⚠ Dockerコマンドが見つかりません。スキップします。${NC}"
fi
echo ""

# 2. アプリケーションの起動待機
echo -e "${YELLOW}[2/5] アプリケーションの起動待機${NC}"
echo "アプリケーションが起動するまで待機しています..."
RETRY_COUNT=0
while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if curl -s -f "${BASE_URL}/health" > /dev/null 2>&1 || \
       curl -s -f "${BASE_URL}/" > /dev/null 2>&1; then
        echo -e "${GREEN}✓ アプリケーションが起動しました${NC}"
        break
    fi
    
    RETRY_COUNT=$((RETRY_COUNT + 1))
    if [ $RETRY_COUNT -lt $MAX_RETRIES ]; then
        echo -n "."
        sleep $RETRY_INTERVAL
    else
        echo ""
        echo -e "${RED}✗ アプリケーションが起動しませんでした（タイムアウト）${NC}"
        echo "   コンテナのログを確認してください: docker logs spring-mart-app"
        exit 1
    fi
done
echo ""

# 3. ログインAPIのテスト
echo -e "${YELLOW}[3/5] ログインAPIのテスト${NC}"
LOGIN_RESPONSE=$(curl -s -X POST "${BASE_URL}/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"userName":"admin","password":"password123"}')

if echo "$LOGIN_RESPONSE" | grep -q "token"; then
    TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*' | cut -d'"' -f4)
    if [ -n "$TOKEN" ]; then
        echo -e "${GREEN}✓ ログイン成功（JWTトークン取得）${NC}"
    else
        echo -e "${RED}✗ ログイン失敗: トークンが取得できませんでした${NC}"
        echo "   レスポンス: $LOGIN_RESPONSE"
        exit 1
    fi
else
    echo -e "${RED}✗ ログイン失敗${NC}"
    echo "   レスポンス: $LOGIN_RESPONSE"
    exit 1
fi
echo ""

# 4. 商品一覧取得APIのテスト（認証付き）
echo -e "${YELLOW}[4/5] 商品一覧取得APIのテスト（認証付き）${NC}"
PRODUCTS_RESPONSE=$(curl -s -X GET "${BASE_URL}/api/products" \
    -H "Authorization: Bearer ${TOKEN}" \
    -H "Content-Type: application/json")

if echo "$PRODUCTS_RESPONSE" | grep -q "id"; then
    PRODUCT_COUNT=$(echo "$PRODUCTS_RESPONSE" | grep -o '"id"' | wc -l)
    echo -e "${GREEN}✓ 商品一覧取得成功（商品数: ${PRODUCT_COUNT}）${NC}"
    echo "   レスポンス（最初の100文字）: ${PRODUCTS_RESPONSE:0:100}..."
else
    echo -e "${RED}✗ 商品一覧取得失敗${NC}"
    echo "   レスポンス: $PRODUCTS_RESPONSE"
    exit 1
fi
echo ""

# 5. 注文APIのテスト（認証付き）
echo -e "${YELLOW}[5/5] 注文APIのテスト（認証付き）${NC}"
ORDER_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/orders" \
    -H "Authorization: Bearer ${TOKEN}" \
    -H "Content-Type: application/json" \
    -d '{"items":[{"productId":1,"quantity":1}]}')

if echo "$ORDER_RESPONSE" | grep -q "orderId"; then
    ORDER_ID=$(echo "$ORDER_RESPONSE" | grep -o '"orderId":[0-9]*' | cut -d':' -f2)
    TOTAL_PRICE=$(echo "$ORDER_RESPONSE" | grep -o '"totalPrice":[0-9]*' | cut -d':' -f2)
    echo -e "${GREEN}✓ 注文作成成功${NC}"
    echo "   注文ID: ${ORDER_ID}"
    echo "   合計金額: ${TOTAL_PRICE}円"
else
    echo -e "${RED}✗ 注文作成失敗${NC}"
    echo "   レスポンス: $ORDER_RESPONSE"
    exit 1
fi
echo ""

# 結果サマリー
echo "=========================================="
echo -e "${GREEN}✓ すべてのチェックが成功しました！${NC}"
echo "=========================================="
echo ""
echo "APIエンドポイント:"
echo "  - ログイン: POST ${BASE_URL}/auth/login"
echo "  - 商品一覧: GET ${BASE_URL}/api/products"
echo "  - 注文作成: POST ${BASE_URL}/api/orders"
echo ""
echo "テスト用認証情報:"
echo "  - ユーザー名: admin"
echo "  - パスワード: password123"
echo ""

