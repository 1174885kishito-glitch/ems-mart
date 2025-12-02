import React, { useState, useEffect, useMemo } from 'react';
import { getProducts } from '../../api/products';

const ProductList = ({ onCartUpdate }) => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [cart, setCart] = useState(() => {
    const saved = localStorage.getItem('cart');
    return saved ? JSON.parse(saved) : [];
  });
  const [searchTerm, setSearchTerm] = useState('');
  const [sortBy, setSortBy] = useState('name');
  const [addedToCart, setAddedToCart] = useState(null);

  useEffect(() => {
    loadProducts();
    updateCartCount();
  }, []);

  useEffect(() => {
    localStorage.setItem('cart', JSON.stringify(cart));
    updateCartCount();
  }, [cart]);

  const loadProducts = async () => {
    try {
      setLoading(true);
      const data = await getProducts();
      setProducts(data);
      setError('');
    } catch (err) {
      setError('商品一覧の取得に失敗しました');
    } finally {
      setLoading(false);
    }
  };

  const updateCartCount = () => {
    const count = cart.reduce((sum, item) => sum + item.quantity, 0);
    onCartUpdate(count);
  };

  const addToCart = (product) => {
    const existingItem = cart.find(item => item.productId === product.id);

    if (existingItem) {
      setCart(cart.map(item =>
        item.productId === product.id
          ? { ...item, quantity: item.quantity + 1 }
          : item
      ));
    } else {
      setCart([...cart, {
        productId: product.id,
        name: product.name,
        price: product.price,
        quantity: 1
      }]);
    }

    // アニメーション効果
    setAddedToCart(product.id);
    setTimeout(() => setAddedToCart(null), 1000);
  };

  // 商品のフィルタリングとソート
  const filteredAndSortedProducts = useMemo(() => {
    let filtered = products.filter(product =>
      product.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (product.description && product.description.toLowerCase().includes(searchTerm.toLowerCase()))
    );

    filtered.sort((a, b) => {
      switch (sortBy) {
        case 'price-low':
          return a.price - b.price;
        case 'price-high':
          return b.price - a.price;
        case 'name':
        default:
          return a.name.localeCompare(b.name);
      }
    });

    return filtered;
  }, [products, searchTerm, sortBy]);

  // 商品画像のプレースホルダー生成
  const getProductImage = (productId) => {
    const colors = [
      'bg-gradient-to-br from-blue-400 to-blue-600',
      'bg-gradient-to-br from-green-400 to-green-600',
      'bg-gradient-to-br from-purple-400 to-purple-600',
      'bg-gradient-to-br from-pink-400 to-pink-600',
      'bg-gradient-to-br from-indigo-400 to-indigo-600',
      'bg-gradient-to-br from-red-400 to-red-600'
    ];
    return colors[productId % colors.length];
  };

  if (loading) return (
    <div className="flex justify-center items-center h-64">
      <div className="animate-pulse">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {[...Array(8)].map((_, i) => (
            <div key={i} className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
              <div className="h-48 bg-gray-200 animate-pulse"></div>
              <div className="p-6">
                <div className="h-4 bg-gray-200 rounded animate-pulse mb-2"></div>
                <div className="h-3 bg-gray-200 rounded animate-pulse mb-4"></div>
                <div className="h-8 bg-gray-200 rounded animate-pulse"></div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-4xl font-bold text-gray-900 mb-2 bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
          商品一覧
        </h1>
        <p className="text-lg text-gray-600">素敵な商品をお選びください</p>
      </div>

      {/* Search and Filter */}
      <div className="mb-8 bg-white rounded-xl shadow-sm border border-gray-100 p-6">
        <div className="flex flex-col md:flex-row gap-4">
          <div className="flex-1">
            <label className="block text-sm font-medium text-gray-700 mb-2">商品を検索</label>
            <div className="relative">
              <input
                type="text"
                placeholder="商品名や説明で検索..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
              />
              <svg className="absolute left-3 top-3.5 h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
            </div>
          </div>
          <div className="md:w-48">
            <label className="block text-sm font-medium text-gray-700 mb-2">並び替え</label>
            <select
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value)}
              className="w-full px-3 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
            >
              <option value="name">名前順</option>
              <option value="price-low">価格が安い順</option>
              <option value="price-high">価格が高い順</option>
            </select>
          </div>
        </div>
      </div>

      {/* Error Message */}
      {error && (
        <div className="mb-6 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg flex items-center gap-2">
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          {error}
        </div>
      )}

      {/* Products Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
        {filteredAndSortedProducts.map((product, index) => (
          <div
            key={product.id}
            className="group bg-white rounded-2xl shadow-sm hover:shadow-2xl transition-all duration-500 border border-gray-100 overflow-hidden transform hover:-translate-y-2"
            style={{ animationDelay: `${index * 100}ms` }}
          >
            {/* Product Image Placeholder */}
            <div className={`h-48 ${getProductImage(product.id)} relative overflow-hidden`}>
              <div className="absolute inset-0 bg-black bg-opacity-20 group-hover:bg-opacity-10 transition-all duration-300"></div>
              <div className="absolute bottom-4 left-4">
                <div className="bg-white bg-opacity-90 backdrop-blur-sm rounded-full px-3 py-1 text-xs font-medium text-gray-700">
                  #{product.id}
                </div>
              </div>
            </div>

            <div className="p-6">
              <h3 className="text-xl font-bold text-gray-900 mb-2 group-hover:text-blue-600 transition-colors line-clamp-1">
                {product.name}
              </h3>

              {product.description && (
                <p className="text-gray-600 text-sm mb-4 line-clamp-2 leading-relaxed">
                  {product.description}
                </p>
              )}

              <div className="flex items-center justify-between mb-4">
                <span className="text-3xl font-bold text-green-600">
                  ¥{product.price.toLocaleString()}
                </span>
                {cart.find(item => item.productId === product.id) && (
                  <div className="bg-blue-100 text-blue-800 text-xs font-medium px-2 py-1 rounded-full">
                    カート内: {cart.find(item => item.productId === product.id).quantity}個
                  </div>
                )}
              </div>

              <button
                className={`w-full py-3 px-4 rounded-xl font-semibold transition-all duration-300 transform ${
                  addedToCart === product.id
                    ? 'bg-green-500 text-white scale-105 shadow-lg'
                    : 'bg-gradient-to-r from-blue-600 to-blue-700 text-white hover:from-blue-700 hover:to-blue-800 hover:scale-105 shadow-md hover:shadow-xl'
                }`}
                onClick={() => addToCart(product)}
                disabled={addedToCart === product.id}
              >
                <div className="flex items-center justify-center gap-2">
                  {addedToCart === product.id ? (
                    <>
                      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                      </svg>
                      追加しました！
                    </>
                  ) : (
                    <>
                      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 3h2l.4 2M7 13h10l4-8H5.4m0 0L7 13m0 0l-2.5 5M7 13l2.5 5m-2.5-5h10M17 18v2a2 2 0 01-2 2H9a2 2 0 01-2-2v-2m8 0V9a2 2 0 00-2-2H9a2 2 0 00-2 2v9.01" />
                      </svg>
                      カートに追加
                    </>
                  )}
                </div>
              </button>
            </div>
          </div>
        ))}
      </div>

      {/* Empty State */}
      {filteredAndSortedProducts.length === 0 && !loading && (
        <div className="text-center py-16">
          <div className="text-8xl mb-6 animate-bounce">🛒</div>
          <h3 className="text-2xl font-bold text-gray-900 mb-2">
            {searchTerm ? '検索結果が見つかりません' : '商品が見つかりません'}
          </h3>
          <p className="text-gray-600 text-lg">
            {searchTerm ? '別のキーワードで検索してみてください' : '現在、商品がありません。'}
          </p>
          {searchTerm && (
            <button
              onClick={() => setSearchTerm('')}
              className="mt-4 bg-blue-600 hover:bg-blue-700 text-white px-6 py-2 rounded-lg font-medium transition-colors"
            >
              検索をクリア
            </button>
          )}
        </div>
      )}

      {/* Results Count */}
      {filteredAndSortedProducts.length > 0 && (
        <div className="text-center mt-8 text-gray-600">
          {searchTerm && (
            <p className="text-sm">
              「{searchTerm}」の検索結果: {filteredAndSortedProducts.length}件
            </p>
          )}
        </div>
      )}
    </div>
  );
};

export default ProductList;

