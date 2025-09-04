import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/v1';

const api = axios.create({
  baseURL: API_BASE_URL,
});
// Request interceptor
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor - SIMPLIFIED VERSION
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // If 401 error and not already retrying
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      
      // Remove expired token and redirect to login
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    
    // If 403 error (forbidden)
    if (error.response?.status === 403) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }

    return Promise.reject(error);
  }
);
export const authAPI = {
  login: (credentials) => api.post('/auth/login', credentials),
  register: (userData) => api.post('/auth/register', userData),
  refreshToken: (refreshToken) => api.post('/auth/refresh', { refreshToken }),
  validateToken: (token) => api.post('/auth/validate', { token }),
  logout: () => api.post('/auth/logout'),
};

export const busAPI = {
  getAll: () => api.get('/buses'),
  create: (busData) => api.post('/buses', busData),
};

export const bookingAPI = {
  create: (bookingData) => api.post('/bookings', bookingData),
  getMyBookings: () => api.get('/bookings/me'),
  getBooking: (id) => api.get(`/bookings/${id}`),
  cancelBooking: (id) => api.delete(`/bookings/${id}`), 
  getUserBookings: (userId) => api.get(`/bookings/user/${userId}`),
};

export const tripAPI = {
// In tripAPI.search - fix the parameters
search: (params) => api.get('/trips/search', { 
  params: { 
    source: params.origin, 
    destination: params.destination, 
    date: params.date 
  }
}),  
  getById: (id) => api.get(`/trips/${id}`),
  getSeats: (tripId) => api.get(`/trips/${tripId}/seats`),
  create: (tripData) => api.post('/trips', tripData),
  getAll: () => api.get('/trips'),
  update: (id, tripData) => api.put(`/trips/${id}`, tripData),
  delete: (id) => api.delete(`/trips/${id}`),

  
};

export const paymentAPI = {
  // Get user's payments
  getMyPayments: () => api.get('/payments/me'),
  
  // Create payment
  create: (paymentData) => api.post('/payments', paymentData),
  
  // Get all payments (admin)
  getAll: () => api.get('/payments/all'),
};

export const routeAPI = {
  getAll: () => api.get('/routes'),
  create: (routeData) => api.post('/routes', routeData),
  update: (id, routeData) => api.put(`/routes/${id}`, routeData),
  delete: (id) => api.delete(`/routes/${id}`),
};

export default api;