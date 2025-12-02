# Spring Mart Frontend

Spring Martのフロントエンドアプリケーション（React + Vite）

## 機能

- **管理者向け管理画面**
  - 商品管理（CRUD操作）
  - 注文管理（今後実装予定）

- **顧客向けECサイト**
  - 商品一覧表示
  - カート機能
  - 注文機能

## 開発環境での起動

```bash
# 依存関係のインストール
npm install

# 開発サーバー起動
npm run dev
```

開発サーバーは `http://localhost:3000` で起動します。

## ビルド

```bash
npm run build
```

ビルド結果は `dist` ディレクトリに出力されます。

## Docker Composeでの起動

プロジェクトルートから以下を実行：

```bash
docker compose up -d --build
```

フロントエンドは `http://localhost:3000` でアクセスできます。

## 技術スタック

- React 18
- React Router 6
- Vite
- Axios
- Nginx (本番環境)

