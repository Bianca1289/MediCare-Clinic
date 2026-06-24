import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  LayoutDashboard,
  User,
  CalendarDays,
  FileText,
  Activity,
  MapPin,
  Phone,
  BadgeCheck,
  Star,
  CalendarPlus,
  ChevronLeft,
  Clock,
  Info,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import Sidebar from '../components/Sidebar';
import BookAppointmentModal from '../components/BookAppointmentModal';
import { getDoctorByUsername } from '../api/doctors';
import './Dashboard.css';
import './DoctorDetailPage.css';

const AVATAR_COLORS = [
  '#2563EB', '#7C3AED', '#059669', '#D97706', '#DC2626', '#0891B2', '#9333EA', '#0D9488',
];

const ALL_DAYS = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];
const DAY_LABEL = {
  MONDAY: 'Monday', TUESDAY: 'Tuesday', WEDNESDAY: 'Wednesday',
  THURSDAY: 'Thursday', FRIDAY: 'Friday', SATURDAY: 'Saturday', SUNDAY: 'Sunday',
};

const USER_NAV = [
  { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/profile', icon: User, label: 'Profile' },
  { to: '/doctors', icon: Activity, label: 'Doctors' },
  { to: '/appointments', icon: CalendarDays, label: 'Appointments', disabled: true },
  { to: '/prescriptions', icon: FileText, label: 'Prescriptions' },
];

const ADMIN_NAV = [
  { to: '/admin/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/profile', icon: User, label: 'Profile' },
  { to: '/doctors', icon: Activity, label: 'Doctors' },
  { to: '/appointments', icon: CalendarDays, label: 'Appointments', disabled: true },
];

function avatarColor(username = '') {
  let hash = 0;
  for (let i = 0; i < username.length; i++) hash = username.charCodeAt(i) + ((hash << 5) - hash);
  return AVATAR_COLORS[Math.abs(hash) % AVATAR_COLORS.length];
}

function formatName(username = '') {
  const base = username.replace(/^dr\./i, '');
  return 'Dr. ' + base.charAt(0).toUpperCase() + base.slice(1);
}

function StarRating({ value }) {
  if (value == null) return <span className="rating-na-detail">No rating yet</span>;
  const full = Math.floor(value);
  const half = value - full >= 0.5;
  return (
    <span className="star-rating-detail" title={`${value.toFixed(1)} / 5`}>
      {[1, 2, 3, 4, 5].map((i) => (
        <Star
          key={i}
          size={16}
          className={i <= full ? 'star-filled' : half && i === full + 1 ? 'star-half' : 'star-empty'}
          fill={i <= full ? 'currentColor' : 'none'}
        />
      ))}
      <span className="star-value-detail">{value.toFixed(1)} / 5</span>
    </span>
  );
}

export default function DoctorDetailPage() {
  const { username } = useParams();
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const [doctor, setDoctor] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showModal, setShowModal] = useState(false);

  const isAdmin = user?.roles?.includes('ROLE_ADMIN');
  const isPatient = user?.roles?.includes('ROLE_PATIENT');
  const navItems = isAdmin ? ADMIN_NAV : USER_NAV;

  useEffect(() => {
    let active = true;
    setLoading(true);
    getDoctorByUsername(username)
      .then((res) => { if (active) setDoctor(res.data); })
      .catch(() => { if (active) setError('Doctor profile not found.'); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [username]);

  async function handleLogout() {
    await logout();
    navigate('/login', { replace: true });
  }

  const scheduleByDay = {};
  (doctor?.availability ?? []).forEach((slot) => {
    if (!scheduleByDay[slot.dayOfWeek]) scheduleByDay[slot.dayOfWeek] = [];
    scheduleByDay[slot.dayOfWeek].push(slot);
  });

  const workingDays = ALL_DAYS.filter((d) => scheduleByDay[d]);

  return (
    <div className="layout">
      <Sidebar navItems={navItems} onLogout={handleLogout} />

      <main className="main">
        {/* Breadcrumb */}
        <button className="detail-back" onClick={() => navigate('/doctors')}>
          <ChevronLeft size={16} />
          Back to Doctors
        </button>

        {loading ? (
          <div className="detail-loading">
            <div className="loading-spinner" />
            <p>Loading profile…</p>
          </div>
        ) : error ? (
          <div className="detail-loading"><p className="error-text">{error}</p></div>
        ) : doctor ? (
          <>
            {/* ── Hero card ── */}
            <div className="detail-hero">
              <div
                className="detail-avatar"
                style={{ background: avatarColor(doctor.username) }}
              >
                {doctor.username.replace(/^dr\./i, '').slice(0, 2).toUpperCase()}
              </div>

              <div className="detail-hero-info">
                <h1 className="detail-name">{formatName(doctor.username)}</h1>

                <div className="detail-specialties">
                  {doctor.specialty && (
                    <span className="specialty-badge">{doctor.specialty}</span>
                  )}
                </div>

                <StarRating value={doctor.averageRating} />

                <div className="detail-meta-row">
                  {doctor.location && (
                    <span className="detail-meta-chip">
                      <MapPin size={13} /> {doctor.location}
                    </span>
                  )}
                  {doctor.phoneNumber && (
                    <span className="detail-meta-chip">
                      <Phone size={13} /> {doctor.phoneNumber}
                    </span>
                  )}
                  {doctor.licenseNumber && (
                    <span className="detail-meta-chip">
                      <BadgeCheck size={13} /> License: {doctor.licenseNumber}
                    </span>
                  )}
                </div>
              </div>

              {isPatient && (
                <button className="detail-book-btn" onClick={() => setShowModal(true)}>
                  <CalendarPlus size={18} />
                  Book Appointment
                </button>
              )}
            </div>

            {/* ── Body ── */}
            <div className="detail-body">
              {/* About */}
              <section className="detail-section detail-section--bio">
                <h2 className="detail-section-title">
                  <Info size={16} /> About the Doctor
                </h2>
                {doctor.bio ? (
                  <p className="detail-bio">{doctor.bio}</p>
                ) : (
                  <p className="detail-bio detail-bio--empty">No biography provided.</p>
                )}
              </section>

              {/* Schedule */}
              <section className="detail-section detail-section--schedule">
                <h2 className="detail-section-title">
                  <Clock size={16} /> Weekly Schedule
                </h2>

                {workingDays.length === 0 ? (
                  <p className="detail-no-schedule">No schedule information available.</p>
                ) : (
                  <div className="schedule-grid">
                    {ALL_DAYS.map((day) => {
                      const slots = scheduleByDay[day];
                      const active = Boolean(slots?.length);
                      return (
                        <div key={day} className={`schedule-row ${active ? 'schedule-row--active' : 'schedule-row--off'}`}>
                          <span className="schedule-day">{DAY_LABEL[day]}</span>
                          <div className="schedule-slots">
                            {active
                              ? slots.map((s, i) => (
                                  <span key={i} className="schedule-slot">
                                    {s.startTime} – {s.endTime}
                                  </span>
                                ))
                              : <span className="schedule-unavailable">Not available</span>}
                          </div>
                        </div>
                      );
                    })}
                  </div>
                )}

                {isPatient && workingDays.length > 0 && (
                  <button className="detail-book-secondary" onClick={() => setShowModal(true)}>
                    <CalendarPlus size={15} />
                    Schedule an Appointment
                  </button>
                )}
              </section>
            </div>
          </>
        ) : null}
      </main>

      {showModal && doctor && (
        <BookAppointmentModal
          doctor={doctor}
          onClose={() => setShowModal(false)}
          onBooked={() => setShowModal(false)}
        />
      )}
    </div>
  );
}
