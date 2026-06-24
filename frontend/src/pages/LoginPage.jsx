import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { User, Lock, AlertTriangle } from 'lucide-react';
import { login as apiLogin } from '../api/auth';
import { useAuth } from '../context/AuthContext';
import './LoginPage.css';

export default function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [rememberMe, setRememberMe] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const res = await apiLogin(username, password, rememberMe);
      login(res.data);
      const isAdmin = res.data.roles.includes('ROLE_ADMIN');
      navigate(isAdmin ? '/admin/dashboard' : '/dashboard', { replace: true });
    } catch {
      setError('Invalid username or password. Please try again.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="login-bg">
      <div className="login-card">
        <div className="login-header">
          <div className="login-logo">
            <svg viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
              <rect width="40" height="40" rx="12" fill="rgba(255,255,255,0.15)" />
              <path
                d="M25 16h-4v-4a1 1 0 00-2 0v4h-4a1 1 0 000 2h4v4a1 1 0 002 0v-4h4a1 1 0 000-2z"
                fill="white"
              />
            </svg>
          </div>
          <h1 className="login-title">MediCare Clinic</h1>
          <p className="login-subtitle">Sign in to your account</p>
        </div>

        <form className="login-form" onSubmit={handleSubmit} noValidate>
          {error && (
            <div className="alert alert-error" role="alert">
              <AlertTriangle size={16} />
              <span>{error}</span>
            </div>
          )}

          <div className="form-group">
            <label htmlFor="username">Username</label>
            <div className="input-wrapper">
              <User size={16} className="input-icon" />
              <input
                id="username"
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="Enter your username"
                required
                autoFocus
                autoComplete="username"
              />
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="password">Password</label>
            <div className="input-wrapper">
              <Lock size={16} className="input-icon" />
              <input
                id="password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Enter your password"
                required
                autoComplete="current-password"
              />
            </div>
          </div>

          <div className="form-check">
            <input
              type="checkbox"
              id="rememberMe"
              checked={rememberMe}
              onChange={(e) => setRememberMe(e.target.checked)}
            />
            <label htmlFor="rememberMe">Remember me for 7 days</label>
          </div>

          <button type="submit" className="btn-login" disabled={loading}>
            {loading ? <span className="btn-spinner" /> : 'Sign In'}
          </button>

          <p style={{ textAlign: 'center', margin: 0, fontSize: '0.875rem', color: '#6b7280' }}>
            Don&apos;t have an account?{' '}
            <Link to="/register" style={{ color: '#1d6fc4', fontWeight: 600 }}>
              Sign up
            </Link>
          </p>
        </form>

        <div className="login-footer">&copy; 2025 MediCare Clinic</div>
      </div>
    </div>
  );
}
