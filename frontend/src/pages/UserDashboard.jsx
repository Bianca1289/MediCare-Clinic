import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  LayoutDashboard,
  User,
  CalendarDays,
  FileText,
  Activity,
  CalendarCheck,
  Clock,
  CalendarX,
  Stethoscope,
  CalendarClock,
  Ban,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import Sidebar from '../components/Sidebar';
import RescheduleModal from '../components/RescheduleModal';
import ConfirmCancelModal from '../components/ConfirmCancelModal';
import { getMyAppointments } from '../api/appointments';
import './Dashboard.css';

const NAV_ITEMS = [
  { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/profile', icon: User, label: 'Profile' },
  { to: '/doctors', icon: Activity, label: 'Doctors' },
  { to: '/appointments', icon: CalendarDays, label: 'Appointments', disabled: true },
  { to: '/prescriptions', icon: FileText, label: 'Prescriptions' },
];

const STATUS_CLASS = {
  SCHEDULED: 'status-badge--green',
  COMPLETED: 'status-badge--blue',
  CANCELLED: 'status-badge--red',
};

function formatDoctorName(username = '') {
  const base = username.replace(/^dr\./i, '');
  return 'Dr. ' + base.charAt(0).toUpperCase() + base.slice(1);
}

function formatDateTime(iso) {
  if (!iso) return '—';
  const d = new Date(iso);
  return (
    d.toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' }) +
    ' · ' +
    d.toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' })
  );
}

function formatDateShort(iso) {
  if (!iso) return '—';
  const d = new Date(iso);
  return d.toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });
}

export default function UserDashboard() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [cancelTarget, setCancelTarget] = useState(null);
  const [rescheduleTarget, setRescheduleTarget] = useState(null);

  const isPatient = user?.roles?.includes('ROLE_PATIENT');

  const loadAppointments = useCallback(() => {
    if (!isPatient) { setLoading(false); return; }
    setLoading(true);
    getMyAppointments(0, 50)
      .then((res) => setAppointments(res.data?.content ?? []))
      .catch(() => setAppointments([]))
      .finally(() => setLoading(false));
  }, [isPatient]);

  useEffect(() => { loadAppointments(); }, [loadAppointments]);

  async function handleLogout() {
    await logout();
    navigate('/login', { replace: true });
  }

  const now = new Date();
  const upcoming = appointments
    .filter((a) => a.status === 'SCHEDULED' && new Date(a.startTime) >= now)
    .sort((a, b) => new Date(a.startTime) - new Date(b.startTime));
  const past = appointments
    .filter((a) => a.status !== 'SCHEDULED' || new Date(a.startTime) < now)
    .sort((a, b) => new Date(b.startTime) - new Date(a.startTime));

  const nextAppt = upcoming[0];

  return (
    <div className="layout">
      <Sidebar navItems={NAV_ITEMS} onLogout={handleLogout} />

      <main className="main">
        <header className="page-header">
          <div>
            <h1 className="page-title">Welcome back, {user?.username}!</h1>
            <p className="page-sub">MediCare Clinic — Patient Portal</p>
          </div>
          <div className="user-badge user-badge--blue">{user?.username}</div>
        </header>

        {/* ── Stats ── */}
        <div className="stats-grid">
          <div className="stat-card stat-card--purple">
            <div className="stat-info">
              <p className="stat-label">Upcoming</p>
              <p className="stat-value">{loading ? '…' : upcoming.length}</p>
            </div>
            <span className="stat-icon"><CalendarCheck size={36} strokeWidth={1.5} /></span>
          </div>

          <div className="stat-card stat-card--green">
            <div className="stat-info">
              <p className="stat-label">Total Appointments</p>
              <p className="stat-value">{loading ? '…' : appointments.length}</p>
            </div>
            <span className="stat-icon"><Stethoscope size={36} strokeWidth={1.5} /></span>
          </div>

          <div className="stat-card stat-card--orange">
            <div className="stat-info">
              <p className="stat-label">Next Visit</p>
              <p className="stat-value stat-value--compact">
                {loading ? '…' : nextAppt ? formatDateShort(nextAppt.startTime) : 'None'}
              </p>
            </div>
            <span className="stat-icon"><Clock size={36} strokeWidth={1.5} /></span>
          </div>
        </div>

        {/* ── Upcoming appointments ── */}
        <div className="table-card" style={{ marginBottom: '1.25rem' }}>
          <h2 className="table-title">Upcoming Appointments</h2>
          {loading ? (
            <p className="loading-text">Loading…</p>
          ) : upcoming.length === 0 ? (
            <div className="dash-empty">
              <CalendarX size={32} strokeWidth={1.5} className="dash-empty-icon" />
              <p>No upcoming appointments.</p>
              <button className="dash-book-btn" onClick={() => navigate('/doctors')}>
                Browse Doctors
              </button>
            </div>
          ) : (
            <table className="data-table">
              <thead>
                <tr>
                  <th>Doctor</th>
                  <th>Date &amp; Time</th>
                  <th>Status</th>
                  <th>Notes</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {upcoming.map((a) => (
                  <tr key={a.id}>
                    <td>
                      <span className="appt-doctor-name">{formatDoctorName(a.doctorUsername)}</span>
                    </td>
                    <td className="appt-datetime">{formatDateTime(a.startTime)}</td>
                    <td>
                      <span className={`status-badge ${STATUS_CLASS[a.status] ?? 'status-badge--green'}`}>
                        {a.status}
                      </span>
                    </td>
                    <td className="appt-notes">{a.notes ?? <span className="appt-no-notes">—</span>}</td>
                    <td>
                      <div className="appt-actions">
                        <button
                          className="appt-action-btn appt-action-btn--reschedule"
                          onClick={() => setRescheduleTarget(a)}
                          title="Reschedule"
                        >
                          <CalendarClock size={14} />
                          Reschedule
                        </button>
                        <button
                          className="appt-action-btn appt-action-btn--cancel"
                          onClick={() => setCancelTarget(a)}
                          title="Cancel appointment"
                        >
                          <Ban size={14} />
                          Cancel
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        {/* ── Past appointments ── */}
        {!loading && past.length > 0 && (
          <div className="table-card">
            <h2 className="table-title">Past &amp; Cancelled Appointments</h2>
            <table className="data-table">
              <thead>
                <tr>
                  <th>Doctor</th>
                  <th>Date &amp; Time</th>
                  <th>Status</th>
                  <th>Notes</th>
                </tr>
              </thead>
              <tbody>
                {past.map((a) => (
                  <tr key={a.id} className="appt-row--past">
                    <td>
                      <span className="appt-doctor-name appt-doctor-name--muted">
                        {formatDoctorName(a.doctorUsername)}
                      </span>
                    </td>
                    <td className="appt-datetime appt-datetime--muted">{formatDateTime(a.startTime)}</td>
                    <td>
                      <span className={`status-badge ${STATUS_CLASS[a.status] ?? 'status-badge--red'}`}>
                        {a.status}
                      </span>
                    </td>
                    <td className="appt-notes">{a.notes ?? <span className="appt-no-notes">—</span>}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </main>

      {rescheduleTarget && (
        <RescheduleModal
          appointment={rescheduleTarget}
          onClose={() => setRescheduleTarget(null)}
          onRescheduled={() => {
            setRescheduleTarget(null);
            loadAppointments();
          }}
        />
      )}

      {cancelTarget && (
        <ConfirmCancelModal
          appointment={cancelTarget}
          onClose={() => setCancelTarget(null)}
          onCancelled={() => {
            setCancelTarget(null);
            loadAppointments();
          }}
        />
      )}
    </div>
  );
}
