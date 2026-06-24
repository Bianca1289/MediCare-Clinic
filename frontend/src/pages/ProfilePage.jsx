import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  LayoutDashboard,
  User,
  CalendarDays,
  FileText,
  Activity,
  ShieldCheck,
  Save,
  CircleAlert,
  CircleCheck,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import Sidebar from '../components/Sidebar';
import { getMyPatientProfile, updateMyPatientProfile } from '../api/patient';
import './Dashboard.css';
import './ProfilePage.css';

const NAV_ITEMS = [
  { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/profile', icon: User, label: 'Profile' },
  { to: '/doctors', icon: Activity, label: 'Doctors' },
  { to: '/appointments', icon: CalendarDays, label: 'Appointments', disabled: true },
  { to: '/prescriptions', icon: FileText, label: 'Prescriptions' },
];

const GENDER_OPTIONS = ['', 'Male', 'Female', 'Other', 'Prefer not to say'];

const EMPTY_FORM = {
  fullName: '',
  phone: '',
  email: '',
  cnp: '',
  gender: '',
  address: '',
};

export default function ProfilePage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const [profile, setProfile] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    let active = true;
    getMyPatientProfile()
      .then((res) => {
        if (!active) return;
        const d = res.data;
        setProfile(d);
        setForm({
          fullName: d.fullName ?? '',
          phone: d.phone ?? '',
          email: d.email ?? '',
          cnp: d.cnp ?? '',
          gender: d.gender ?? '',
          address: d.address ?? '',
        });
      })
      .catch(() => {
        if (!active) return;
        setError('Unable to load your profile. Please try again.');
      })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, []);

  function field(name) {
    return (e) => setForm((f) => ({ ...f, [name]: e.target.value }));
  }

  async function handleLogout() {
    await logout();
    navigate('/login', { replace: true });
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setSuccess('');
    setSaving(true);
    try {
      const res = await updateMyPatientProfile({
        fullName: form.fullName.trim() || user.username,
        phone: form.phone.trim() || null,
        email: form.email.trim() || null,
        cnp: form.cnp.trim() || null,
        gender: form.gender || null,
        address: form.address.trim() || null,
      });
      const d = res.data;
      setProfile(d);
      setForm({
        fullName: d.fullName ?? '',
        phone: d.phone ?? '',
        email: d.email ?? '',
        cnp: d.cnp ?? '',
        gender: d.gender ?? '',
        address: d.address ?? '',
      });
      setSuccess('Profile saved successfully.');
    } catch (err) {
      const msg = err?.response?.data?.message || err?.response?.data?.error || 'Could not save profile.';
      setError(msg);
    } finally {
      setSaving(false);
    }
  }

  const roles = user?.roles ?? [];
  const isAdmin = roles.includes('ROLE_ADMIN');

  return (
    <div className="layout">
      <Sidebar navItems={NAV_ITEMS} onLogout={handleLogout} />

      <main className="main">
        <header className="page-header">
          <div>
            <h1 className="page-title">My Profile</h1>
            <p className="page-sub">Manage your personal information and account details</p>
          </div>
          <div className={`user-badge ${isAdmin ? 'user-badge--red' : 'user-badge--blue'}`}>
            {user?.username}
          </div>
        </header>

        {loading ? (
          <div className="info-card" style={{ textAlign: 'center', padding: '3rem' }}>
            <div className="profile-spinner" />
          </div>
        ) : (
          <div className="profile-layout">

            {/* Left column — account info */}
            <aside className="profile-aside">
              <div className="info-card profile-account-card">
                <div className="profile-avatar-wrap">
                  <div className="profile-avatar">
                    {(user?.username ?? '?').slice(0, 2).toUpperCase()}
                  </div>
                  <div>
                    <p className="profile-avatar-name">{profile?.fullName || user?.username}</p>
                    <p className="profile-avatar-sub">@{user?.username}</p>
                  </div>
                </div>

                <div className="profile-account-rows">
                  <div className="profile-account-row">
                    <span className="profile-account-label">Account ID</span>
                    <span className="profile-account-value">#{profile?.id ?? '—'}</span>
                  </div>
                  <div className="profile-account-row">
                    <span className="profile-account-label">Status</span>
                    <span className="status-badge status-badge--green">Active</span>
                  </div>
                  <div className="profile-account-row">
                    <span className="profile-account-label">Roles</span>
                    <div style={{ display: 'flex', gap: '0.35rem', flexWrap: 'wrap' }}>
                      {roles.map((r) => (
                        <span
                          key={r}
                          className={`role-badge role-badge--small ${r === 'ROLE_ADMIN' ? 'role-badge--red' : 'role-badge--green'}`}
                        >
                          {r.replace('ROLE_', '')}
                        </span>
                      ))}
                    </div>
                  </div>
                </div>

                <div className="profile-security-note">
                  <ShieldCheck size={14} />
                  <span>Your session is secure</span>
                </div>
              </div>
            </aside>

            {/* Right column — editable form */}
            <section className="profile-form-section">
              <div className="info-card">
                <p className="info-label">Personal Information</p>

                {error && (
                  <div className="profile-alert profile-alert--error">
                    <CircleAlert size={15} />
                    <span>{error}</span>
                  </div>
                )}
                {success && (
                  <div className="profile-alert profile-alert--success">
                    <CircleCheck size={15} />
                    <span>{success}</span>
                  </div>
                )}

                <form className="profile-edit-form" onSubmit={handleSubmit}>
                  <div className="pf-row">
                    <div className="pf-group">
                      <label className="pf-label" htmlFor="fullName">Full Name</label>
                      <input
                        id="fullName"
                        className="pf-input"
                        type="text"
                        value={form.fullName}
                        onChange={field('fullName')}
                        placeholder="e.g. Maria Ionescu"
                        maxLength={100}
                      />
                    </div>
                    <div className="pf-group">
                      <label className="pf-label" htmlFor="cnp">CNP</label>
                      <input
                        id="cnp"
                        className="pf-input"
                        type="text"
                        value={form.cnp}
                        onChange={field('cnp')}
                        placeholder="13-digit personal code"
                        maxLength={13}
                        pattern="[0-9]{13}"
                        title="CNP must be 13 digits"
                      />
                    </div>
                  </div>

                  <div className="pf-row">
                    <div className="pf-group">
                      <label className="pf-label" htmlFor="gender">Gender</label>
                      <select
                        id="gender"
                        className="pf-input pf-select"
                        value={form.gender}
                        onChange={field('gender')}
                      >
                        {GENDER_OPTIONS.map((g) => (
                          <option key={g} value={g}>{g || 'Select…'}</option>
                        ))}
                      </select>
                    </div>
                    <div className="pf-group">
                      <label className="pf-label" htmlFor="phone">Phone Number</label>
                      <input
                        id="phone"
                        className="pf-input"
                        type="tel"
                        value={form.phone}
                        onChange={field('phone')}
                        placeholder="+40 7XX XXX XXX"
                        maxLength={20}
                      />
                    </div>
                  </div>

                  <div className="pf-group">
                    <label className="pf-label" htmlFor="email">Email Address</label>
                    <input
                      id="email"
                      className="pf-input"
                      type="email"
                      value={form.email}
                      onChange={field('email')}
                      placeholder="your@email.com"
                    />
                  </div>

                  <div className="pf-group">
                    <label className="pf-label" htmlFor="address">Home Address</label>
                    <input
                      id="address"
                      className="pf-input"
                      type="text"
                      value={form.address}
                      onChange={field('address')}
                      placeholder="Street, City, County"
                      maxLength={255}
                    />
                  </div>

                  <div className="pf-actions">
                    <button className="pf-btn-save" type="submit" disabled={saving}>
                      {saving ? (
                        <span className="btn-spinner" />
                      ) : (
                        <>
                          <Save size={15} />
                          Save changes
                        </>
                      )}
                    </button>
                  </div>
                </form>
              </div>
            </section>
          </div>
        )}
      </main>
    </div>
  );
}
