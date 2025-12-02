@echo off
REM Spring Mart バックオフィス ヘルスチェックスクリプト (Windows版)
REM 使用方法: check_health.bat

setlocal enabledelayedexpansion

set BASE_URL=http://localhost:8080
set MAX_RETRIES=30
set RETRY_INTERVAL=2

echo ==========================================
echo Spring Mart バックオフィス ヘルスチェック
echo ==========================================
echo.

REM 1. Dockerコンテナの状態確認
echo [1/5] Dockerコンテナの状態確認
docker ps | findstr "spring-mart-db" >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] データベースコンテナ (spring-mart-db) が起動しています
) else (
    echo [ERROR] データベースコンテナ (spring-mart-db) が起動していません
    exit /b 1
)

docker ps | findstr "spring-mart-app" >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] アプリケーションコンテナ (spring-mart-app) が起動しています
) else (
    echo [ERROR] アプリケーションコンテナ (spring-mart-app) が起動していません
    exit /b 1
)
echo.

REM 2. アプリケーションの起動待機
echo [2/5] アプリケーションの起動待機
echo アプリケーションが起動するまで待機しています...
set RETRY_COUNT=0
:wait_loop
curl -s -f "%BASE_URL%/auth/login" >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] アプリケーションが起動しました
    goto :app_ready
)
set /a RETRY_COUNT+=1
if !RETRY_COUNT! lss %MAX_RETRIES% (
    echo|set /p="."
    timeout /t %RETRY_INTERVAL% /nobreak >nul
    goto :wait_loop
) else (
    echo.
    echo [ERROR] アプリケーションが起動しませんでした（タイムアウト）
    echo    コンテナのログを確認してください: docker logs spring-mart-app
    exit /b 1
)
:app_ready
echo.

REM 3. ログインAPIのテスト
echo [3/5] ログインAPIのテスト
curl -s -X POST "%BASE_URL%/auth/login" -H "Content-Type: application/json" -d "{\"userName\":\"admin\",\"password\":\"password123\"}" > temp_login.json
findstr /c:"token" temp_login.json >nul 2>&1
if %errorlevel% equ 0 (
    REM PowerShellを使ってトークンを抽出
    for /f "delims=" %%i in ('powershell -Command "(Get-Content temp_login.json | ConvertFrom-Json).token"') do set TOKEN=%%i
    if defined TOKEN (
        echo [OK] ログイン成功（JWTトークン取得）
    ) else (
        echo [ERROR] ログイン失敗: トークンが取得できませんでした
        type temp_login.json
        del temp_login.json
        exit /b 1
    )
) else (
    echo [ERROR] ログイン失敗
    type temp_login.json
    del temp_login.json
    exit /b 1
)
del temp_login.json
echo.

REM 4. 商品一覧取得APIのテスト（認証付き）
echo [4/5] 商品一覧取得APIのテスト（認証付き）
curl -s -X GET "%BASE_URL%/api/products" -H "Authorization: Bearer %TOKEN%" -H "Content-Type: application/json" > temp_products.json
findstr /c:"id" temp_products.json >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] 商品一覧取得成功
    type temp_products.json
) else (
    echo [ERROR] 商品一覧取得失敗
    type temp_products.json
    del temp_products.json
    exit /b 1
)
del temp_products.json
echo.

REM 5. 注文APIのテスト（認証付き）
echo [5/5] 注文APIのテスト（認証付き）
curl -s -X POST "%BASE_URL%/api/orders" -H "Authorization: Bearer %TOKEN%" -H "Content-Type: application/json" -d "{\"items\":[{\"productId\":1,\"quantity\":1}]}" > temp_order.json
findstr /c:"orderId" temp_order.json >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] 注文作成成功
    type temp_order.json
) else (
    echo [ERROR] 注文作成失敗
    type temp_order.json
    del temp_order.json
    exit /b 1
)
del temp_order.json
echo.

REM 結果サマリー
echo ==========================================
echo [OK] すべてのチェックが成功しました！
echo ==========================================
echo.
echo APIエンドポイント:
echo   - ログイン: POST %BASE_URL%/auth/login
echo   - 商品一覧: GET %BASE_URL%/api/products
echo   - 注文作成: POST %BASE_URL%/api/orders
echo.
echo テスト用認証情報:
echo   - ユーザー名: admin
echo   - パスワード: password123
echo.

endlocal

