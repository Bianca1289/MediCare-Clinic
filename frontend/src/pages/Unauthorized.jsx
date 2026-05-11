import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Unauthorized() {
  const navigate = useNavigate();
  const { user } = useAuth();

  function goBack() {
    const isAdmin = user?.roles?.includes('ROLE_ADMIN');
    navigate(isAdmin ? '/admin/dashboard' : '/dashboard', { replace: true });
  }

  return (
    <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#f3f4f6' }}>
      <div style={{ background: '#fff', borderRadius: '16px', padding: '3rem', textAlign: 'center', boxShadow: '0 4px 24px rgba(0,0,0,0.1)', maxWidth: '400px' }}>
        <div style={{ fontSize: '4rem', marginBottom: '1rem' }}>🚫</div>
        <h1 style={{ fontSize: '1.5rem', fontWeight: 700, color: '#111827', margin: '0 0 0.5rem' }}>
          Access Denied
        </h1>
        <p style={{ color: '#6b7280', margin: '0 0 1.5rem', fontSize: '0.95rem' }}>
          You don&apos;t have permission to view this page.
        </p>
        <button
          onClick={goBack}
          style={{ padding: '0.6rem 1.5rem', background: '#1d6fc4', color: '#fff', border: 'none', borderRadius: '8px', fontSize: '0.95rem', fontWeight: 600, cursor: 'pointer' }}
        >
          Go to Dashboard
        </button>
      </div>
    </div>
  );
}
