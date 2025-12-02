import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import AdminDashboard from './pages/admin/AdminDashboard';
import CustomerHome from './pages/customer/CustomerHome';
import ErrorBoundary from './components/ErrorBoundary';
import { isAuthenticated, getUser } from './api/auth';

const PrivateRoute = ({ children, requireAdmin = false }) => {
  if (!isAuthenticated()) {
    return <Navigate to="/login" replace />;
  }
  
  if (requireAdmin) {
    const user = getUser();
    if (user?.role !== 'ROLE_ADMIN') {
      return <Navigate to="/customer" replace />;
    }
  }
  
  return children;
};

function App() {
  return (
    <ErrorBoundary>
      <Router>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route
            path="/admin/*"
            element={
              <PrivateRoute requireAdmin={true}>
                <ErrorBoundary>
                  <AdminDashboard />
                </ErrorBoundary>
              </PrivateRoute>
            }
          />
          <Route
            path="/customer/*"
            element={
              <PrivateRoute>
                <ErrorBoundary>
                  <CustomerHome />
                </ErrorBoundary>
              </PrivateRoute>
            }
          />
          <Route path="/" element={<Navigate to="/customer" replace />} />
        </Routes>
      </Router>
    </ErrorBoundary>
  );
}

export default App;

