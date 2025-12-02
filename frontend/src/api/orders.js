import apiClient from './client';

export const createOrder = async (orderData) => {
  const response = await apiClient.post('/api/orders', orderData);
  return response.data;
};

