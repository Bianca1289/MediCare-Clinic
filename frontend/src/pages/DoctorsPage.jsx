import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  LayoutDashboard,
  User,
  CalendarDays,
  FileText,
  Activity,
  Search,
  Phone,
  BadgeCheck,
  MapPin,
  Star,
  CalendarPlus,
  ArrowRight,
  X,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import Sidebar from '../components/Sidebar';
import BookAppointmentModal from '../components/BookAppointmentModal';
import { getDoctors, getSpecialties } from '../api/doctors';
import './Dashboard.css';
import './DoctorsPage.css';

const AVATAR_COLORS = [
  '#2563EB', '#7C3AED', '#059669', '#D97706', '#DC2626', '#0891B2', '#9333EA', '#0D9488',
];

const SORT_OPTIONS = [
  { value: '', label: 'Sort by…' },
  { value: 'name_asc', label: 'Name A → Z' },
  { value: 'name_desc', label: 'Name Z → A' },
  { value: 'rating_desc', label: 'Rating: High → Low' },
  { value: 'rating_asc', label: 'Rating: Low → High' },
];

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

function avatarColor(idx) { return AVATAR_COLORS[idx % AVATAR_COLORS.length]; }
function initials(username = '') { return username.slice(0, 2).toUpperCase(); }
function formatName(username = '') {
  const base = username.replace(/^dr\./i, '');
  return 'Dr. ' + base.charAt(0).toUpperCase() + base.slice(1);
}

function StarRating({ value }) {
  if (value == null) return <span className="rating-na">No rating yet</span>;
  const full = Math.floor(value);
  const half = value - full >= 0.5;
  return (
    <span className="star-rating" title={`${value.toFixed(1)} / 5`}>
      {[1, 2, 3, 4, 5].map((i) => (
        <Star
          key={i}
          size={13}
          className={i <= full ? 'star-filled' : half && i === full + 1 ? 'star-half' : 'star-empty'}
          fill={i <= full ? 'currentColor' : 'none'}
        />
      ))}
      <span className="star-value">{value.toFixed(1)}</span>
    </span>
  );
}

export default function DoctorsPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const [doctors, setDoctors] = useState([]);
  const [specialties, setSpecialties] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [search, setSearch] = useState('');
  const [filterSpecialty, setFilterSpecialty] = useState('');
  const [filterLocation, setFilterLocation] = useState('');
  const [sortBy, setSortBy] = useState('');

  const [bookTarget, setBookTarget] = useState(null);

  const isAdmin = user?.roles?.includes('ROLE_ADMIN');
  const isPatient = user?.roles?.includes('ROLE_PATIENT');
  const navItems = isAdmin ? ADMIN_NAV : USER_NAV;

  useEffect(() => {
    let active = true;
    setLoading(true);
    Promise.all([getDoctors(), getSpecialties()])
      .then(([docRes, specRes]) => {
        if (!active) return;
        setDoctors(docRes.data?.content ?? []);
        setSpecialties(specRes.data ?? []);
      })
      .catch(() => { if (active) setError('Could not load doctors. Please try again.'); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, []);

  const locations = useMemo(() => {
    const set = new Set(doctors.map((d) => d.location).filter(Boolean));
    return Array.from(set).sort();
  }, [doctors]);

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();
    let list = doctors.filter((doc) => {
      const matchSearch = !q || doc.username.toLowerCase().includes(q);
      const matchSpec = !filterSpecialty || doc.specialty === filterSpecialty;
      const matchLoc = !filterLocation || doc.location === filterLocation;
      return matchSearch && matchSpec && matchLoc;
    });

    if (sortBy === 'name_asc') list = [...list].sort((a, b) => a.username.localeCompare(b.username));
    else if (sortBy === 'name_desc') list = [...list].sort((a, b) => b.username.localeCompare(a.username));
    else if (sortBy === 'rating_desc') list = [...list].sort((a, b) => (b.averageRating ?? -1) - (a.averageRating ?? -1));
    else if (sortBy === 'rating_asc') list = [...list].sort((a, b) => (a.averageRating ?? 99) - (b.averageRating ?? 99));

    return list;
  }, [doctors, search, filterSpecialty, filterLocation, sortBy]);

  async function handleLogout() {
    await logout();
    navigate('/login', { replace: true });
  }

  function clearFilters() {
    setSearch('');
    setFilterSpecialty('');
    setFilterLocation('');
    setSortBy('');
  }

  const hasFilters = search || filterSpecialty || filterLocation || sortBy;

  return (
    <div className="layout">
      <Sidebar navItems={navItems} onLogout={handleLogout} />

      <main className="main">
        <header className="page-header">
          <div>
            <h1 className="page-title">Our Doctors</h1>
            <p className="page-sub">Find and learn about our medical specialists</p>
          </div>
          <div className="user-badge user-badge--blue">{user?.username}</div>
        </header>

        {/* ── Toolbar ── */}
        <div className="doctors-toolbar">
          {/* Search */}
          <div className="search-box">
            <Search size={15} className="search-icon" />
            <input
              type="text"
              placeholder="Search by name…"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
            {search && (
              <button className="search-clear" onClick={() => setSearch('')} aria-label="Clear search">
                <X size={13} />
              </button>
            )}
          </div>

          {/* Specialty dropdown */}
          <div className="filter-select-wrap">
            <select
              className="filter-select"
              value={filterSpecialty}
              onChange={(e) => setFilterSpecialty(e.target.value)}
            >
              <option value="">All Specialties</option>
              {specialties.map((s) => (
                <option key={s.id} value={s.name}>{s.name}</option>
              ))}
            </select>
          </div>

          {/* Location dropdown */}
          <div className="filter-select-wrap">
            <select
              className="filter-select"
              value={filterLocation}
              onChange={(e) => setFilterLocation(e.target.value)}
            >
              <option value="">All Locations</option>
              {locations.map((loc) => (
                <option key={loc} value={loc}>{loc}</option>
              ))}
            </select>
          </div>

          {/* Sort dropdown */}
          <div className="filter-select-wrap">
            <select
              className="filter-select"
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value)}
            >
              {SORT_OPTIONS.map((o) => (
                <option key={o.value} value={o.value}>{o.label}</option>
              ))}
            </select>
          </div>

          {hasFilters && (
            <button className="filter-clear-btn" onClick={clearFilters}>
              <X size={13} /> Clear filters
            </button>
          )}
        </div>

        {/* ── Results ── */}
        {loading ? (
          <div className="doctors-empty">
            <div className="loading-spinner" />
            <p>Loading doctors…</p>
          </div>
        ) : error ? (
          <div className="doctors-empty"><p className="error-text">{error}</p></div>
        ) : filtered.length === 0 ? (
          <div className="doctors-empty">
            <Activity size={40} strokeWidth={1.5} className="empty-icon" />
            <p>No doctors match your filters.</p>
            {hasFilters && (
              <button className="filter-clear-btn" onClick={clearFilters}>
                Clear filters
              </button>
            )}
          </div>
        ) : (
          <>
            <p className="doctors-count">
              {filtered.length} doctor{filtered.length !== 1 ? 's' : ''} found
            </p>
            <div className="doctors-grid">
              {filtered.map((doc, idx) => (
                <article key={doc.id} className="doctor-card">
                  <div className="doctor-card-header">
                    <div className="doctor-avatar" style={{ background: avatarColor(idx) }}>
                      {initials(doc.username)}
                    </div>
                    <div className="doctor-identity">
                      <h2 className="doctor-name">{formatName(doc.username)}</h2>
                      <div className="doctor-specialties">
                        {doc.specialty && (
                          <span className="specialty-badge">{doc.specialty}</span>
                        )}
                      </div>
                      <StarRating value={doc.averageRating} />
                    </div>
                  </div>

                  {doc.bio && <p className="doctor-bio">{doc.bio}</p>}

                  <div className="doctor-meta">
                    {doc.location && (
                      <div className="doctor-meta-row">
                        <MapPin size={13} className="meta-icon" />
                        <span>{doc.location}</span>
                      </div>
                    )}
                    {doc.phoneNumber && (
                      <div className="doctor-meta-row">
                        <Phone size={13} className="meta-icon" />
                        <span>{doc.phoneNumber}</span>
                      </div>
                    )}
                    {doc.licenseNumber && (
                      <div className="doctor-meta-row">
                        <BadgeCheck size={13} className="meta-icon" />
                        <span>License: {doc.licenseNumber}</span>
                      </div>
                    )}
                  </div>

                  <div className="doctor-card-actions">
                    <button
                      className="doctor-profile-btn"
                      onClick={() => navigate(`/doctors/${doc.username}`)}
                    >
                      <ArrowRight size={14} />
                      View Profile
                    </button>
                    {isPatient && (
                      <button
                        className="doctor-book-btn"
                        onClick={() => setBookTarget(doc)}
                      >
                        <CalendarPlus size={15} />
                        Book
                      </button>
                    )}
                  </div>
                </article>
              ))}
            </div>
          </>
        )}
      </main>

      {bookTarget && (
        <BookAppointmentModal
          doctor={bookTarget}
          onClose={() => setBookTarget(null)}
          onBooked={() => setBookTarget(null)}
        />
      )}
    </div>
  );
}
