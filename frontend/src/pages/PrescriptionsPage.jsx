import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  LayoutDashboard,
  User,
  CalendarDays,
  FileText,
  Activity,
  Pill,
  Clock,
  CalendarCheck,
  Stethoscope,
  FileX,
  ChevronRight,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import Sidebar from '../components/Sidebar';
import { getMyPrescriptions } from '../api/prescriptions';
import './Dashboard.css';
import './PrescriptionsPage.css';

const NAV_ITEMS = [
  { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/profile', icon: User, label: 'Profile' },
  { to: '/doctors', icon: Activity, label: 'Doctors' },
  { to: '/appointments', icon: CalendarDays, label: 'Appointments', disabled: true },
  { to: '/prescriptions', icon: FileText, label: 'Prescriptions' },
];

function formatDoctorName(username = '') {
  const base = username.replace(/^dr\./i, '');
  return 'Dr. ' + base.charAt(0).toUpperCase() + base.slice(1);
}

function formatDate(iso) {
  if (!iso) return '—';
  const d = new Date(iso);
  return d.toLocaleDateString('en-GB', { day: '2-digit', month: 'long', year: 'numeric' });
}

function isExpired(validUntil) {
  if (!validUntil) return false;
  return new Date(validUntil) < new Date();
}

export default function PrescriptionsPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const [prescriptions, setPrescriptions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [expanded, setExpanded] = useState(null);

  useEffect(() => {
    getMyPrescriptions()
      .then((res) => setPrescriptions(res.data ?? []))
      .catch(() => setPrescriptions([]))
      .finally(() => setLoading(false));
  }, []);

  async function handleLogout() {
    await logout();
    navigate('/login', { replace: true });
  }

  function toggleExpand(id) {
    setExpanded((prev) => (prev === id ? null : id));
  }

  const active = prescriptions.filter((p) => !isExpired(p.validUntil));
  const expired = prescriptions.filter((p) => isExpired(p.validUntil));

  return (
    <div className="layout">
      <Sidebar navItems={NAV_ITEMS} onLogout={handleLogout} />

      <main className="main">
        <header className="page-header">
          <div>
            <h1 className="page-title">My Prescriptions</h1>
            <p className="page-sub">Medications prescribed by your doctors</p>
          </div>
          <div className="user-badge user-badge--blue">{user?.username}</div>
        </header>

        {loading ? (
          <p className="loading-text">Loading prescriptions…</p>
        ) : prescriptions.length === 0 ? (
          <div className="dash-empty">
            <FileX size={40} strokeWidth={1.5} className="dash-empty-icon" />
            <p>No prescriptions yet.</p>
            <button className="dash-book-btn" onClick={() => navigate('/doctors')}>
              Browse Doctors
            </button>
          </div>
        ) : (
          <>
            {active.length > 0 && (
              <section className="rx-section">
                <h2 className="rx-section-title">
                  <CalendarCheck size={18} />
                  Active Prescriptions
                  <span className="rx-count">{active.length}</span>
                </h2>
                <div className="rx-grid">
                  {active.map((p) => (
                    <PrescriptionCard
                      key={p.id}
                      prescription={p}
                      expanded={expanded === p.id}
                      onToggle={() => toggleExpand(p.id)}
                      expired={false}
                    />
                  ))}
                </div>
              </section>
            )}

            {expired.length > 0 && (
              <section className="rx-section">
                <h2 className="rx-section-title rx-section-title--muted">
                  <Clock size={18} />
                  Expired Prescriptions
                  <span className="rx-count rx-count--muted">{expired.length}</span>
                </h2>
                <div className="rx-grid">
                  {expired.map((p) => (
                    <PrescriptionCard
                      key={p.id}
                      prescription={p}
                      expanded={expanded === p.id}
                      onToggle={() => toggleExpand(p.id)}
                      expired={true}
                    />
                  ))}
                </div>
              </section>
            )}
          </>
        )}
      </main>
    </div>
  );
}

function PrescriptionCard({ prescription: p, expanded, onToggle, expired }) {
  return (
    <div className={`rx-card${expired ? ' rx-card--expired' : ''}`}>
      <div className="rx-card-header" onClick={onToggle}>
        <div className="rx-icon-wrap">
          <Pill size={22} strokeWidth={1.8} />
        </div>
        <div className="rx-card-main">
          <h3 className="rx-medication">{p.medicationName}</h3>
          <div className="rx-badges">
            {p.dosage && <span className="rx-badge rx-badge--blue">{p.dosage}</span>}
            {p.frequency && <span className="rx-badge rx-badge--purple">{p.frequency}</span>}
            {p.duration && <span className="rx-badge rx-badge--green">{p.duration}</span>}
          </div>
          <div className="rx-meta">
            <span className="rx-meta-item">
              <Stethoscope size={13} />
              {formatDoctorName(p.doctorUsername)}
            </span>
            <span className="rx-meta-sep">·</span>
            <span className="rx-meta-item">
              <Clock size={13} />
              Issued {formatDate(p.issuedAt)}
            </span>
            {p.validUntil && (
              <>
                <span className="rx-meta-sep">·</span>
                <span className={`rx-meta-item${expired ? ' rx-meta-item--expired' : ''}`}>
                  {expired ? 'Expired' : 'Valid until'} {formatDate(p.validUntil)}
                </span>
              </>
            )}
          </div>
        </div>
        <ChevronRight
          size={18}
          className={`rx-chevron${expanded ? ' rx-chevron--open' : ''}`}
        />
      </div>

      {expanded && p.instructions && (
        <div className="rx-instructions">
          <p className="rx-instructions-label">Instructions</p>
          <p className="rx-instructions-text">{p.instructions}</p>
        </div>
      )}
    </div>
  );
}
