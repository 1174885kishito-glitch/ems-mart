import React from 'react';

class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    console.error('Error caught by boundary:', error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div style={{ padding: '20px', textAlign: 'center' }}>
          <h2>エラーが発生しました</h2>
          <p>{this.state.error?.message || '予期しないエラーが発生しました'}</p>
          <button onClick={() => window.location.href = '/login'}>
            ログイン画面に戻る
          </button>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;

