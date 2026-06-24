import { Link, useLocation } from 'react-router-dom';
import {
  Cross,
  LayoutDashboard,
  User,
  CalendarDays,
  FileText,
  Users,
  Activity,
  BarChart2,
  LogOut,
} from 'lucide-react';
import './Sidebar.css';

const BRAND_ICON_MAP = {
  admin: <BarChart2 size={20} />,
  user: <LayoutDashboard size={20} />,
};

export default function Sidebar({ navItems, onLogout }) {
  const { pathname } = useLocation();

  return (
    <aside className="sidebar">
      <div className="sidebar-brand">
        <span className="sidebar-brand-icon">
          <Cross size={18} strokeWidth={2.5} />
        </span>
        <span className="sidebar-brand-name">MediCare</span>
      </div>

      <nav className="sidebar-nav">
        {navItems.map(({ to, icon: Icon, label, disabled }) =>
          disabled ? (
            <span key={label} className="nav-item nav-item--disabled">
              <span className="nav-icon"><Icon size={18} /></span>
              {label}
            </span>
          ) : (
            <Link
              key={to}
              className={`nav-item${pathname === to ? ' active' : ''}`}
              to={to}
            >
              <span className="nav-icon"><Icon size={18} /></span>
              {label}
            </Link>
          )
        )}
      </nav>

      <button className="btn-logout" onClick={onLogout}>
        <LogOut size={16} />
        <span>Log out</span>
      </button>
    </aside>
  );
}

export {
  LayoutDashboard,
  User,
  CalendarDays,
  FileText,
  Users,
  Activity,
  BarChart2,
};
