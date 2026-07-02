import axios from 'axios';

const apiClient = axios.create({
baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
});

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  const metodo = (config.method || 'get').toLowerCase();
  const esEscritura = ['post', 'put', 'patch', 'delete'].includes(metodo);

  if (esEscritura && !navigator.onLine) {
    return Promise.reject(new Error('Sin conexión a internet. No se pueden guardar cambios en este momento.'));
  }

  return config;
});

export default apiClient;