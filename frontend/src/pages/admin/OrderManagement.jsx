import React, { useState, useEffect } from 'react';
import '../../App.css';

const OrderManagement = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // 注文一覧取得APIは未実装のため、プレースホルダー
    setLoading(false);
  }, []);

  return (
    <div>
      <h2>注文管理</h2>
      <div className="card">
        <p>注文一覧機能は今後実装予定です。</p>
        <p>現在は注文作成APIのみ利用可能です。</p>
      </div>
    </div>
  );
};

export default OrderManagement;

