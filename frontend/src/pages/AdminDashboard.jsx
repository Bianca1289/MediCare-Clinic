import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import API from '../api/auth';
import './Dashboard.css';

export default function AdminDashboard() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [dashData, setDashData] = useState(null);
  const [loadingData, setLoadingData] = useState(true);

  useEffect(() => {
    API.get('/api/admin/dashboard')
      .then((res) => setDashData(res.data))
      .catch(() => setDashData(null))
      .finally(() => setLoadingData(false));
  }, []);

  async function handleLogout() {
    await logout();
    navigate('/login', { replace: true });
  }

  return (
    <div className="layout">
      <aside className="sidebar">
        <div className="sidebar-brand">
          <span className="brand-icon">&#x2764;</span>
          <span>MediCare</span>
        </div>
        <nav className="sidebar-nav">
          <a className="nav-item active" href="/admin/dashboard">
            <span className="nav-icon">&#x1F4CA;</span> Admin Dashboard
          </a>
          <a className="nav-item" href="#">
            <span className="nav-icon">&#x1F465;</span> Manage Users
          </a>
          <a className="nav-item" href="#">
            <span className="nav-icon">&#x1F4C5;</span> Appointments
          </a>
          <a className="nav-item" href="#">
            <span className="nav-icon">&#x1F489;</span> Doctors
          </a>
        </nav>
        <button className="btn-logout" onClick={handleLogout}>
          &#x2190; Logout
        </button>
      </aside>

      <main className="main">
        <header className="page-header">
          <div>
            <h1 className="page-title">Admin Dashboard</h1>
            <p className="page-sub">MediCare Clinic Administration</p>
          </div>
          <div className="user-badge user-badge--red">
            &#x1F6E1; {user?.username}
          </div>
        </header>

        <div className="stats-grid">
          <div className="stat-card stat-card--blue">
            <div className="stat-info">
              <p className="stat-label">Total Users</p>
              <p className="stat-value">
                {loadingData ? '…' : (dashData?.totalUsers ?? '—')}
              </p>
            </div>
            <span className="stat-icon">&#x1F465;</span>
          </div>
          <div className="stat-card stat-card--green">
            <div className="stat-info">
              <p className="stat-label">Appointments</p>
              <p className="stat-value">—</p>
            </div>
            <span className="stat-icon">&#x1F4C5;</span>
          </div>
          <div className="stat-card stat-card--orange">
            <div className="stat-info">
              <p className="stat-label">Doctors</p>
              <p className="stat-value">—</p>
            </div>
            <span className="stat-icon">&#x1F489;</span>
          </div>
        </div>

        {/* Users table */}
        <div className="table-card">
          <h2 className="table-title">Registered Users</h2>
          {loadingData ? (
            <p className="loading-text">Loading…</p>
          ) : (
            <table className="data-table">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Username</th>
                  <th>Status</th>
                  <th>Roles</th>
                </tr>
              </thead>
              <tbody>
                {dashData?.users?.map((u, i) => (
                  <tr key={u.id}>
                    <td>{i + 1}</td>
                    <td>{u.username}</td>
                    <td>
                      <span className={`status-badge ${u.enabled ? 'status-badge--green' : 'status-badge--red'}`}>
                        {u.enabled ? 'Active' : 'Disabled'}
                      </span>
                    </td>
                    <td>
                      {u.roles?.map((r) => (
                        <span key={r.name} className="role-badge role-badge--small">
                          {r.name}
                        </span>
                      ))}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </main>
    </div>
  );
}
