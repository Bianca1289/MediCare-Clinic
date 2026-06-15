import API from './auth';
export function getMyProfile() {
  return API.get('/api/profile/me');
}
export function updateMyProfile(username) {
  return API.put('/api/profile/me', { username });
}
export function changePassword(currentPassword, newPassword) {
  return API.post('/api/profile/me/password', { currentPassword, newPassword });
}
export default { getMyProfile, updateMyProfile, changePassword };
