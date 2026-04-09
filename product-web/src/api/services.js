import { api } from './client';

export const authApi = {
  login: (payload) => api.post('/auth/login', payload).then((response) => response.data),
  me: () => api.get('/auth/me').then((response) => response.data),
};

export const dashboardApi = {
  get: () => api.get('/dashboard').then((response) => response.data),
};

export const usersApi = {
  getAll: () => api.get('/users').then((response) => response.data),
  getById: (id) => api.get(`/users/${id}`).then((response) => response.data),
  create: (payload) => api.post('/users', payload).then((response) => response.data),
  update: (id, payload) => api.put(`/users/${id}`, payload).then((response) => response.data),
  updateOwnPhoto: (payload) => api.put('/users/me/photo', payload).then((response) => response.data),
};

export const employeesApi = {
  getAll: () => api.get('/employees').then((response) => response.data),
  getById: (id) => api.get(`/employees/${id}`).then((response) => response.data),
};

export const payoutsApi = {
  getAll: () => api.get('/payouts').then((response) => response.data),
  getMine: () => api.get('/payouts/mine').then((response) => response.data),
  getById: (id) => api.get(`/payouts/${id}`).then((response) => response.data),
  create: (payload) => api.post('/payouts', payload).then((response) => response.data),
  update: (id, payload) => api.put(`/payouts/${id}`, payload).then((response) => response.data),
  updateStatus: (id, status) => api.put(`/payouts/${id}/status`, { status }).then((response) => response.data),
  generateComment: (payload) => api.post('/payouts/generate-comment', payload).then((response) => response.data),
};

export const aiApi = {
  getUsage: () => api.get('/ai/usage').then((response) => response.data),
  getSettings: () => api.get('/ai/settings').then((response) => response.data),
  updateSettings: (payload) => api.put('/ai/settings', payload).then((response) => response.data),
};

export const reportsApi = {
  download: async (type, params) => {
    const response = await api.get(`/reports/${type}`, {
      params,
      responseType: 'blob',
    });
    return response.data;
  },
};
