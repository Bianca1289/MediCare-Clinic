import axios from 'axios';

const API = axios.create({
  baseURL: '',          // same origin — Vite proxy forwards /api to :8080
  withCredentials: true, // send session cookie + remember-me cookie
});

// Attach the CSRF token (written by Spring Security into XSRF-TOKEN cookie)
// to every state-changing request.
API.interceptors.request.use((config) => {
  if (['post', 'put', 'delete', 'patch'].includes(config.method)) {
    const token = getCookie('XSRF-TOKEN');
    if (token) config.headers['X-XSRF-TOKEN'] = token;
  }
  return config;
});

function getCookie(name) {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) return parts.pop().split(';').shift();
  return null;
}

// Spring Security form-login expects application/x-www-form-urlencoded
export function login(username, password, rememberMe = false) {
  const params = new URLSearchParams();
  params.append('username', username);
  params.append('password', password);
  if (rememberMe) params.append('remember-me', 'on');

  return API.post('/api/auth/login', params, {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  });
}

export function logout() {
  return API.post('/api/auth/logout');
}

export function getMe() {
  return API.get('/api/auth/me');
}

// Register creates a new account, then logs in automatically
export function register(username, password) {
  return API.post('/api/auth/register', { username, password });
}

export default API;
