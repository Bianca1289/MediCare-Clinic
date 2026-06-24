import API from './auth';

export function getMyPrescriptions() {
  return API.get('/api/patient/prescriptions');
}
