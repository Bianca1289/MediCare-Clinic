import { useState } from 'react';
import { X, TriangleAlert, CircleCheck, CalendarX } from 'lucide-react';
import { cancelAppointment } from '../api/appointments';
import './ConfirmCancelModal.css';

function formatDoctorName(username = '') {
  const base = username.replace(/^dr\./i, '');
  return 'Dr. ' + base.charAt(0).toUpperCase() + base.slice(1);
}

function formatDateTime(iso) {
  if (!iso) return '—';
  const d = new Date(iso);
  return (
    d.toLocaleDateString('en-GB', { weekday: 'long', day: '2-digit', month: 'long', year: 'numeric' }) +
    ' at ' +
    d.toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' })
  );
}

export default function ConfirmCancelModal({ appointment, onClose, onCancelled }) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [done, setDone] = useState(false);

  async function handleConfirm() {
    setError('');
    setLoading(true);
    try {
      await cancelAppointment(appointment.id);
      setDone(true);
      onCancelled?.();
    } catch (err) {
      setError(
        err?.response?.data?.message ||
        err?.response?.data?.error ||
        'Could not cancel the appointment. Please try again.'
      );
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="cc-overlay" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="cc-box" role="dialog" aria-modal="true">
        {/* Close */}
        <button className="cc-close" onClick={onClose} aria-label="Close">
          <X size={18} />
        </button>

        {done ? (
          /* ── Success ── */
          <div className="cc-success">
            <div className="cc-success-icon">
              <CircleCheck size={44} strokeWidth={1.5} />
            </div>
            <h3>Appointment Cancelled</h3>
            <p>Your appointment with <strong>{formatDoctorName(appointment.doctorUsername)}</strong> has been cancelled.</p>
            <button className="cc-btn cc-btn--primary" onClick={onClose}>Close</button>
          </div>
        ) : (
          /* ── Confirm ── */
          <>
            <div className="cc-icon-wrap">
              <CalendarX size={32} strokeWidth={1.5} />
            </div>

            <h2 className="cc-title">Cancel Appointment?</h2>
            <p className="cc-sub">You are about to cancel the following appointment:</p>

            <div className="cc-detail-card">
              <div className="cc-detail-row">
                <span className="cc-detail-label">Doctor</span>
                <span className="cc-detail-value">{formatDoctorName(appointment.doctorUsername)}</span>
              </div>
              <div className="cc-detail-row">
                <span className="cc-detail-label">Date &amp; Time</span>
                <span className="cc-detail-value">{formatDateTime(appointment.startTime)}</span>
              </div>
              {appointment.notes && (
                <div className="cc-detail-row">
                  <span className="cc-detail-label">Notes</span>
                  <span className="cc-detail-value">{appointment.notes}</span>
                </div>
              )}
            </div>

            <div className="cc-warning">
              <TriangleAlert size={15} />
              <span>This action cannot be undone.</span>
            </div>

            {error && <p className="cc-error">{error}</p>}

            <div className="cc-actions">
              <button className="cc-btn cc-btn--ghost" onClick={onClose} disabled={loading}>
                Keep Appointment
              </button>
              <button className="cc-btn cc-btn--danger" onClick={handleConfirm} disabled={loading}>
                {loading ? <span className="cc-spinner" /> : <><CalendarX size={15} /> Yes, Cancel</>}
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
