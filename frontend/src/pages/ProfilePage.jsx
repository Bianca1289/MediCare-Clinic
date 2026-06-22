import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getMyProfile, updateMyProfile } from '../api/profile';
import './Dashboard.css';
import './ProfilePage.css';
export default function ProfilePage() {
  const { user, logout, refreshUser } = useAuth();
  const navigate = useNavigate();
  const [profile, setProfile] = useState(null);
  const [form, setForm] = useState({ username: '' });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  useEffect(() => {
    let isMounted = true;
    async function loadProfile() {
      try {
        const res = await getMyProfile();
        if (!isMounted) return;
        setProfile(res.data);
        setForm({ username: res.data.username ?? '' });
      } catch {
        if (!isMounted) return;
        setError('Unable to load your profile. Please try again.');
      } finally {
        if (isMounted) setLoading(false);
      }
    }
    loadProfile();
    return () => {
      isMounted = false;
    };
  }, []);
  async function handleLogout() {
    await logout();
    navigate('/login', { replace: true });
  }
  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setSuccess('');
    setSaving(true);
    try {
      const res = await updateMyProfile(form.username.trim());
      setProfile(res.data);
      setForm({ username: res.data.username ?? '' });
      setSuccess('Profile updated successfully.');
      await refreshUser();
    } catch (err) {
      const message = err?.response?.data?.message || err?.response?.data?.error || 'Could not update profile.';
      setError(message);
    } finally {
      setSaving(false);
    }
  }
  const roles = profile?.roles ?? user?.roles ?? [];
  return (
    <div className="layout">
      <aside className="sidebar">
        <div className="sidebar-brand">
          <span className="brand-icon">&#x2764;</span>
          <span>MediCare</span>
        </div>
        <nav className="sidebar-nav">
          <Link className="nav-item" to="/dashboard">
            <span className="nav-icon">&#x1F3E0;</span> Dashboard
          </Link>
          <Link className="nav-item active" to="/profile">
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
            <h1 className="page-title">My Profile</h1>
            <p className="page-sub">View and update your account details</p>
          </div>
          <div className="user-badge user-badge--blue">
            &#x1F464; {user?.username}
          </div>
        </header>
        {loading ? (
          <div className="info-card">
            <p className="loading-text">Loading profile...</p>
          </div>
        ) : (
          <>
            <div className="stats-grid profile-stats-grid">
              <div className="stat-card stat-card--blue">
                <div className="stat-info">
                  <p className="stat-label">Username</p>
                  <p className="stat-value stat-value--compact">{profile?.username}</p>
                </div>
                <span className="stat-icon">&#x270D;</span>
              </div>
              <div className="stat-card stat-card--green">
                <div className="stat-info">
                  <p className="stat-label">Account Status</p>
                  <p className="stat-value stat-value--compact">{profile?.enabled ? 'Active' : 'Disabled'}</p>
                </div>
                <span className="stat-icon">&#x2705;</span>
              </div>
              <div className="stat-card stat-card--purple">
                <div className="stat-info">
                  <p className="stat-label">Roles</p>
                  <p className="stat-value stat-value--compact">{roles.length}</p>
                </div>
                <span className="stat-icon">&#x1F511;</span>
              </div>
            </div>
            <div className="profile-grid">
              <section className="info-card">
                <p className="info-label">Account Details</p>
                <div className="detail-list">
                  <div className="detail-item">
                    <span className="detail-label">User ID</span>
                    <span className="detail-value">{profile?.id}</span>
                  </div>
                  <div className="detail-item">
                    <span className="detail-label">Username</span>
                    <span className="detail-value">{profile?.username}</span>
                  </div>
                  <div className="detail-item">
                    <span className="detail-label">Enabled</span>
                    <span className="detail-value">{profile?.enabled ? 'Yes' : 'No'}</span>
                  </div>
                  <div className="detail-item">
                    <span className="detail-label">Roles</span>
                    <span className="detail-value role-badges role-badges--right">
                      {roles.map((role) => (
                        <span
                          key={role}
                          className={`role-badge role-badge--small ${role === 'ROLE_ADMIN' ? 'role-badge--red' : 'role-badge--green'}`}
                        >
                          {role === 'ROLE_ADMIN' ? 'ADMIN' : 'USER'}
                        </span>
                      ))}
                    </span>
                  </div>
                </div>
              </section>
              <section className="info-card">
                <p className="info-label">Update Username</p>
                <form className="profile-form" onSubmit={handleSubmit}>
                  <div className="form-group">
                    <label className="form-label" htmlFor="username">
                      Username
                    </label>
                    <input
                      id="username"
                      className="form-input"
                      type="text"
                      value={form.username}
                      onChange={(e) => setForm({ username: e.target.value })}
                      minLength={3}
                      maxLength={50}
                      required
                    />
                  </div>
                  {error ? <div className="alert alert--error">{error}</div> : null}
                  {success ? <div className="alert alert--success">{success}</div> : null}
                  <div className="form-actions">
                    <button className="btn-primary" type="submit" disabled={saving}>
                      {saving ? 'Saving...' : 'Save changes'}
                    </button>
                    <Link className="btn-secondary" to="/dashboard">
                      Back to dashboard
                    </Link>
                  </div>
                  <p className="help-text">
                    This changes the username used to sign in. Your session will refresh automatically after saving.
                  </p>
                </form>
              </section>
            </div>
          </>
        )}
      </main>
    </div>
  );
}
