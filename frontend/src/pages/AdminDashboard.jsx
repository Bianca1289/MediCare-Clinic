import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  LayoutDashboard,
  User,
  Users,
  CalendarDays,
  Activity,
  UsersRound,
  CalendarCheck,
  Stethoscope,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import Sidebar from '../components/Sidebar';
import API from '../api/auth';
import './Dashboard.css';

const NAV_ITEMS = [
  { to: '/admin/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/profile', icon: User, label: 'Profile' },
  { to: '/doctors', icon: Activity, label: 'Doctors' },
  { to: '/users', icon: Users, label: 'Manage Users', disabled: true },
  { to: '/appointments', icon: CalendarDays, label: 'Appointments', disabled: true },
];

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
      <Sidebar navItems={NAV_ITEMS} onLogout={handleLogout} />

      <main className="main">
        <header className="page-header">
          <div>
            <h1 className="page-title">Admin Dashboard</h1>
            <p className="page-sub">MediCare Clinic — Administration</p>
          </div>
          <div className="user-badge user-badge--red">
            {user?.username}
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
            <span className="stat-icon"><UsersRound size={36} strokeWidth={1.5} /></span>
          </div>
          <div className="stat-card stat-card--green">
            <div className="stat-info">
              <p className="stat-label">Appointments</p>
              <p className="stat-value">—</p>
            </div>
            <span className="stat-icon"><CalendarCheck size={36} strokeWidth={1.5} /></span>
          </div>
          <div className="stat-card stat-card--orange">
            <div className="stat-info">
              <p className="stat-label">Doctors</p>
              <p className="stat-value">—</p>
            </div>
            <span className="stat-icon"><Stethoscope size={36} strokeWidth={1.5} /></span>
          </div>
        </div>

        <div className="table-card">
          <h2 className="table-title">Registered Users</h2>
          {loadingData ? (
            <p className="loading-text">Loading...</p>
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
                      <span
                        className={`status-badge ${u.enabled ? 'status-badge--green' : 'status-badge--red'}`}
                      >
                        {u.enabled ? 'Active' : 'Disabled'}
                      </span>
                    </td>
                    <td>
                      {u.roles?.map((r) => (
                        <span key={r} className="role-badge role-badge--small">
                          {r}
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
