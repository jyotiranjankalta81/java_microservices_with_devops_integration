import axios from 'axios';

const api = axios.create({ baseURL: '/api' });

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  
  const simulatedUser = localStorage.getItem('evidencing_simulated_user');
  if (simulatedUser) {
    try {
      const u = JSON.parse(simulatedUser);
      if (u.userId) config.headers['X-User-Id'] = u.userId;
      if (u.role) config.headers['X-User-Roles'] = u.role;
    } catch (e) {
      // ignore
    }
  }

  config.headers['X-Correlation-Id'] = crypto.randomUUID();
  return config;
});

export default api;
