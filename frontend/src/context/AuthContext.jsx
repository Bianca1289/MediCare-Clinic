/* eslint-disable react-refresh/only-export-components, react/prop-types */
import { createContext, useCallback, useContext, useEffect, useState } from 'react';
import { getMe, logout as apiLogout } from '../api/auth';
const AuthContext = createContext(null);
export function AuthProvider({ children }) {
  const [user, setUser] = useState(null); // { username, roles }
  const [loading, setLoading] = useState(true);
  const refreshUser = useCallback(async () => {
    try {
      const res = await getMe();
      setUser(res.data);
      return res.data;
    } catch {
      setUser(null);
      return null;
    }
  }, []);
  useEffect(() => {
    refreshUser().finally(() => setLoading(false));
  }, [refreshUser]);
  function login(userData) {
    setUser(userData);
  }
  async function logout() {
    await apiLogout();
    setUser(null);
  }
  function hasRole(role) {
    return user?.roles?.includes(role) ?? false;
  }
  return (
    <AuthContext.Provider value={{ user, loading, login, logout, hasRole, refreshUser }}>
      {children}
    </AuthContext.Provider>
  );
}
export function useAuth() {
  return useContext(AuthContext);
}
