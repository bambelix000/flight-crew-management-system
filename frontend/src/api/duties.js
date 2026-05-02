import { request } from './client';

export function createDutyFromFlights(flightIds) {
  return request('/duties/create-from-flights', { method: 'POST', body: flightIds });
}

export function assignUserToDuty(dutyId, userId, role) {
  return request(`/duties/${dutyId}/assign/${userId}?role=${encodeURIComponent(role)}`, { method: 'POST' });
}
