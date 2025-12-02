# Spring Mart フロントエンド 使い方

## 概要

Reactで作成されたフロントエンドアプリケーションです。管理者向け管理画面と顧客向けECサイトの両方を含んでいます。

## 起動方法

### Docker Composeで起動（推奨）

プロジェクトルートから以下を実行：

```bash
docker compose up -d --build
```

すべてのサービス（データベース、バックエンドAPI、フロントエンド）が起動します。

### 開発環境で起動

フロントエンドのみを開発環境で起動する場合：

```bash
cd frontend
npm install
npm run dev
```

## アクセス方法

### フロントエンド
- **URL**: `http://localhost:3000`
- ログイン画面が表示されます

### バックエンドAPI（直接アクセス）
- **URL**: `http://localhost:8080`
- APIエンドポイントに直接アクセスできます

## ログイン

### 管理者アカウント
- **ユーザー名**: `admin`
- **パスワード**: `password123`
- **アクセス先**: ログイン後、自動的に管理画面（`/admin`）に遷移

### 一般ユーザーアカウント
- **ユーザー名**: `user1`
- **パスワード**: `password123`
- **アクセス先**: ログイン後、自動的に顧客画面（`/customer`）に遷移

## 機能説明

### 管理者向け管理画面 (`/admin`)

1. **商品管理** (`/admin/products`)
   - 商品一覧の表示
   - 新規商品の登録
   - 商品情報の編集
   - 商品の削除

2. **注文管理** (`/admin/orders`)
   - 注文一覧の表示（今後実装予定）

### 顧客向けECサイト (`/customer`)

1. **商品一覧** (`/customer`)
   - 登録されている商品の一覧表示
   - カートへの追加機能

2. **カート** (`/customer/cart`)
   - カート内商品の確認
   - 数量の変更
   - 注文の確定

## 画面構成

```
/login
  ├─ ログイン画面

/admin (管理者のみ)
  ├─ /admin/products (商品管理)
  └─ /admin/orders (注文管理)

/customer (認証済みユーザー)
  ├─ /customer (商品一覧)
  └─ /customer/cart (カート)
```

## トラブルシューティング

### フロントエンドが起動しない

1. コンテナの状態を確認：
```bash
docker ps
```

2. ログを確認：
```bash
docker logs spring-mart-frontend
```

3. 再ビルド：
```bash
docker compose up -d --build frontend
```

### API接続エラー

- バックエンドAPI（`http://localhost:8080`）が起動しているか確認
- ブラウザの開発者ツールでネットワークエラーを確認

### ログインできない

- ユーザー名とパスワードが正しいか確認
- バックエンドAPIが正常に動作しているか確認（`http://localhost:8080/health`）

## 開発

### ファイル構成

```
frontend/
├── src/
│   ├── api/          # APIクライアント
│   ├── pages/        # ページコンポーネント
│   │   ├── admin/    # 管理者画面
│   │   ├── customer/ # 顧客画面
│   │   └── Login.jsx # ログイン画面
│   ├── App.jsx       # メインアプリケーション
│   └── main.jsx      # エントリーポイント
├── Dockerfile
├── nginx.conf
└── package.json
```

### カスタマイズ

- APIのベースURLは `frontend/src/api/client.js` で設定
- スタイルは各コンポーネントのCSSファイルで変更可能

