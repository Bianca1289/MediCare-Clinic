import API from './auth';

export function getMyPatientProfile() {
  return API.get('/api/patient/me');
}

export function updateMyPatientProfile(data) {
  return API.put('/api/patient/me', data);
}
