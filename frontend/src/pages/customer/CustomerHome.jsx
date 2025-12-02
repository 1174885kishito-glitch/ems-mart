import React, { useState } from 'react';
import { Routes, Route, Link, useNavigate } from 'react-router-dom';
import { logout, getUser } from '../../api/auth';
import ProductList from './ProductList';
import CustomerCart from './CustomerCart';

const CustomerHome = () => {
  const navigate = useNavigate();
  const user = getUser();
  const [cartCount, setCartCount] = useState(0);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
      <header className="bg-white shadow-lg border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center">
              <h1 className="text-2xl font-bold text-gray-900">Spring Mart</h1>
            </div>
            <nav className="flex items-center space-x-6">
              <Link
                to="/customer"
                className="text-gray-700 hover:text-blue-600 px-3 py-2 rounded-md text-sm font-medium transition-colors duration-200"
              >
                商品一覧
              </Link>
              <Link
                to="/customer/cart"
                className="relative text-gray-700 hover:text-blue-600 px-3 py-2 rounded-md text-sm font-medium transition-colors duration-200"
              >
                カート
                {cartCount > 0 && (
                  <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center">
                    {cartCount}
                  </span>
                )}
              </Link>
              <div className="flex items-center space-x-4">
                <span className="text-sm text-gray-600">{user?.userName}</span>
                <button
                  onClick={handleLogout}
                  className="bg-red-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-red-700 transition-colors duration-200"
                >
                  ログアウト
                </button>
              </div>
            </nav>
          </div>
        </div>
      </header>

      <main className="flex-1 py-8">
        <Routes>
          <Route path="/" element={<ProductList onCartUpdate={setCartCount} />} />
          <Route path="cart" element={<CustomerCart onCartUpdate={setCartCount} />} />
        </Routes>
      </main>
    </div>
  );
};

export default CustomerHome;

