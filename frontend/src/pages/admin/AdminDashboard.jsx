import React from 'react';
import { Routes, Route, Link, useNavigate, Navigate } from 'react-router-dom';
import { logout, getUser } from '../../api/auth';
import ProductManagement from './ProductManagement';
import OrderManagement from './OrderManagement';
import '../../App.css';

const AdminDashboard = () => {
  const navigate = useNavigate();
  const user = getUser();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="app">
      <header className="header">
        <div className="header-content">
          <h1>Spring Mart 管理画面</h1>
          <nav>
            <Link to="/admin/products">商品管理</Link>
            <Link to="/admin/orders">注文管理</Link>
            <span style={{ color: 'white', margin: '0 10px' }}>
              {user?.userName} (管理者)
            </span>
            <button onClick={handleLogout}>ログアウト</button>
          </nav>
        </div>
      </header>

      <main className="main-content">
        <Routes>
          <Route path="products" element={<ProductManagement />} />
          <Route path="orders" element={<OrderManagement />} />
          <Route path="*" element={<Navigate to="/admin/products" replace />} />
        </Routes>
      </main>
    </div>
  );
};

export default AdminDashboard;

