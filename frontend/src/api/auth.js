import { request } from './client';

const TOKEN_KEY = 'token';
const USER_KEY = 'user';

export async function login(credentials) {
  const res = await request('/auth/login', { method: 'POST', body: credentials, auth: false });
  localStorage.setItem(TOKEN_KEY, res.token);
  // TODO: replace hardcoded fields when GET /users/me ships
  const userPayload = {
    login: res.login,
    userRole: res.role,
    name: res.login,
    surname: '',
    twentyDaysAirTime: 0,
    annualAirTime: 0,
  };
  localStorage.setItem(USER_KEY, JSON.stringify(userPayload));
  return { token: res.token, user: userPayload };
}

export async function register(payload) {
  return request('/auth/register', { method: 'POST', body: payload, auth: false });
}

export function logout() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function getUser() {
  const raw = localStorage.getItem(USER_KEY);
  if (!raw) return null;
  try { return JSON.parse(raw); } catch { return null; }
}
