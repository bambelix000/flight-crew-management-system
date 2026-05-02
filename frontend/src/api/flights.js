import { request } from './client';

export function listFlights() {
  return request('/flights');
}
