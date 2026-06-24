import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { User, Lock, AlertTriangle } from 'lucide-react';
import { register as apiRegister, login as apiLogin } from '../api/auth';
import { useAuth } from '../context/AuthContext';
import './LoginPage.css';

export default function RegisterPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');

    if (password !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    setLoading(true);
    try {
      await apiRegister(username, password);
      const loginRes = await apiLogin(username, password);
      login(loginRes.data);
      navigate('/dashboard', { replace: true });
    } catch (err) {
      const msg = err.response?.data?.error;
      setError(
        msg === 'Username already taken'
          ? 'That username is already taken. Please choose another.'
          : 'Registration failed. Please try again.'
      );
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
          <p className="login-subtitle">Create your account</p>
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
                placeholder="Choose a username (min. 3 chars)"
                required
                minLength={3}
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
                placeholder="At least 6 characters"
                required
                minLength={6}
                autoComplete="new-password"
              />
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="confirmPassword">Confirm Password</label>
            <div className="input-wrapper">
              <Lock size={16} className="input-icon" />
              <input
                id="confirmPassword"
                type="password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                placeholder="Repeat your password"
                required
                autoComplete="new-password"
              />
            </div>
          </div>

          <button type="submit" className="btn-login" disabled={loading}>
            {loading ? <span className="btn-spinner" /> : 'Create Account'}
          </button>

          <p style={{ textAlign: 'center', margin: 0, fontSize: '0.875rem', color: '#6b7280' }}>
            Already have an account?{' '}
            <Link to="/login" style={{ color: '#1d6fc4', fontWeight: 600 }}>
              Sign in
            </Link>
          </p>
        </form>

        <div className="login-footer">&copy; 2025 MediCare Clinic</div>
      </div>
    </div>
  );
}
