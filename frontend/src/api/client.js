export const API_BASE = 'http://localhost:8080';

export class ApiError extends Error {
  constructor(status, message, body) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.body = body;
  }
}

const TOKEN_KEY = 'token';

export async function request(path, { method = 'GET', body, auth = true, headers = {} } = {}) {
  const finalHeaders = { ...headers };
  let finalBody;

  if (body !== undefined) {
    finalHeaders['Content-Type'] = 'application/json';
    finalBody = JSON.stringify(body);
  }

  if (auth) {
    const token = localStorage.getItem(TOKEN_KEY);
    if (token) finalHeaders['Authorization'] = `Bearer ${token}`;
  }

  let res;
  try {
    res = await fetch(`${API_BASE}${path}`, { method, headers: finalHeaders, body: finalBody });
  } catch {
    throw new ApiError(0, 'Błąd połączenia z serwerem');
  }

  if (res.status === 401 || res.status === 403) {
    if (auth) window.dispatchEvent(new Event('auth:expired'));
    const msg = await readBody(res);
    throw new ApiError(res.status, typeof msg === 'string' ? msg || 'Brak autoryzacji' : 'Brak autoryzacji', msg);
  }

  if (!res.ok) {
    const parsed = await readBody(res);
    const message = typeof parsed === 'string' && parsed
      ? parsed
      : parsed?.message || `Błąd ${res.status}`;
    throw new ApiError(res.status, message, parsed);
  }

  if (res.status === 204) return null;
  return readBody(res);
}

async function readBody(res) {
  const contentType = res.headers.get('content-type') || '';
  const text = await res.text();
  if (!text) return null;
  if (contentType.includes('application/json')) {
    try { return JSON.parse(text); } catch { return text; }
  }
  return text;
}
