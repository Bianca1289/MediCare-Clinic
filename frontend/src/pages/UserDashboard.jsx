import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import API from '../api/auth';
import './Dashboard.css';
export default function UserDashboard() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [quickProfile, setQuickProfile] = useState(null);
  useEffect(() => {
    API.get('/api/profile/me')
      .then((res) => setQuickProfile(res.data))
      .catch(() => setQuickProfile(null));
  }, []);
  async function handleLogout() {
    await logout();
    navigate('/login', { replace: true });
  }
  const roles = quickProfile?.roles ?? user?.roles ?? [];
  return (
    <div className="layout">
      <aside className="sidebar">
        <div className="sidebar-brand">
          <span className="brand-icon">&#x2764;</span>
          <span>MediCare</span>
        </div>
        <nav className="sidebar-nav">
          <Link className="nav-item active" to="/dashboard">
            <span className="nav-icon">&#x1F3E0;</span> Dashboard
          </Link>
          <Link className="nav-item" to="/profile">
            <span className="nav-icon">&#x1F464;</span> Profile
          </Link>
          <a className="nav-item" href="#">
            <span className="nav-icon">&#x1F4C5;</span> Appointments
          </a>
          <a className="nav-item" href="#">
            <span className="nav-icon">&#x1F4CB;</span> Prescriptions
          </a>
        </nav>
        <button className="btn-logout" onClick={handleLogout}>
          &#x2190; Logout
        </button>
      </aside>
      <main className="main">
        <header className="page-header">
          <div>
            <h1 className="page-title">Welcome back, {user?.username}!</h1>
            <p className="page-sub">MediCare Clinic Patient Portal</p>
          </div>
          <div className="user-badge user-badge--blue">
            &#x1F464; {user?.username}
          </div>
        </header>
        <div className="stats-grid">
          <div className="stat-card stat-card--purple">
            <div className="stat-info">
              <p className="stat-label">My Appointments</p>
              <p className="stat-value">—</p>
            </div>
            <span className="stat-icon">&#x1F4C5;</span>
          </div>
          <div className="stat-card stat-card--green">
            <div className="stat-info">
              <p className="stat-label">My Prescriptions</p>
              <p className="stat-value">—</p>
            </div>
            <span className="stat-icon">&#x1F4CB;</span>
          </div>
          <div className="stat-card stat-card--orange">
            <div className="stat-info">
              <p className="stat-label">Notifications</p>
              <p className="stat-value">—</p>
            </div>
            <span className="stat-icon">&#x1F514;</span>
          </div>
        </div>
        <div className="info-card">
          <p className="info-label">Your Role</p>
          <div className="role-badges">
            {roles.map((r) => (
              <span key={r} className={`role-badge ${r === 'ROLE_ADMIN' ? 'role-badge--red' : 'role-badge--green'}`}>
                {r === 'ROLE_ADMIN' ? '🛡 ADMINISTRATOR' : '✓ PATIENT / USER'}
              </span>
            ))}
          </div>
        </div>
      </main>
    </div>
  );
}
