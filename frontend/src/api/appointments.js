import API from './auth';

export function bookAppointment(doctorUsername, startTime, notes) {
  return API.post('/api/patient/appointments', { doctorUsername, startTime, notes });
}

export function getMyAppointments(page = 0, size = 20) {
  return API.get('/api/patient/appointments', { params: { page, size, sort: 'startTime,desc' } });
}

export function cancelAppointment(id) {
  return API.patch(`/api/patient/appointments/${id}/cancel`);
}

export function rescheduleAppointment(id, startTime) {
  return API.patch(`/api/patient/appointments/${id}/reschedule`, { startTime });
}
