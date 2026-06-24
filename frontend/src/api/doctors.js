import API from './auth';

export function getDoctors(page = 0, size = 100) {
  return API.get('/api/doctors/profiles', { params: { page, size } });
}

export function getDoctorByUsername(username) {
  return API.get(`/api/doctors/profiles/username/${username}`);
}

export function getSpecialties() {
  return API.get('/api/specialties');
}
