import { useState } from 'react';
import { X, CalendarDays, Clock, CircleAlert, CircleCheck } from 'lucide-react';
import { rescheduleAppointment } from '../api/appointments';
import './BookAppointmentModal.css';

function minDate() {
  const d = new Date();
  d.setDate(d.getDate() + 1);
  return d.toISOString().slice(0, 10);
}

function formatDoctorName(username = '') {
  const base = username.replace(/^dr\./i, '');
  return 'Dr. ' + base.charAt(0).toUpperCase() + base.slice(1);
}

export default function RescheduleModal({ appointment, onClose, onRescheduled }) {
  const [date, setDate] = useState('');
  const [time, setTime] = useState('09:00');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [done, setDone] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    if (!date) { setError('Please select a new date.'); return; }
    setLoading(true);
    try {
      await rescheduleAppointment(appointment.id, `${date}T${time}:00`);
      setDone(true);
      onRescheduled?.();
    } catch (err) {
      const msg =
        err?.response?.data?.message ||
        err?.response?.data?.error ||
        'Could not reschedule. Please try again.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="modal-overlay" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="modal-box" role="dialog" aria-modal="true">
        <div className="modal-header">
          <div>
            <h2 className="modal-title">Reschedule Appointment</h2>
            <p className="modal-sub">
              with <strong>{formatDoctorName(appointment.doctorUsername)}</strong>
            </p>
          </div>
          <button className="modal-close" onClick={onClose} aria-label="Close">
            <X size={18} />
          </button>
        </div>

        {done ? (
          <div className="modal-success">
            <div className="modal-success-icon">
              <CircleCheck size={40} strokeWidth={1.5} />
            </div>
            <h3>Appointment Rescheduled!</h3>
            <p>
              Your appointment has been moved to <strong>{date}</strong> at{' '}
              <strong>{time}</strong>.
            </p>
            <button className="modal-btn-primary" onClick={onClose}>Close</button>
          </div>
        ) : (
          <form className="modal-form" onSubmit={handleSubmit}>
            {error && (
              <div className="modal-alert modal-alert--error">
                <CircleAlert size={15} />
                <span>{error}</span>
              </div>
            )}

            <div className="modal-row">
              <div className="modal-group">
                <label className="modal-label" htmlFor="rs-date">
                  <CalendarDays size={14} /> New Date
                </label>
                <input
                  id="rs-date"
                  className="modal-input"
                  type="date"
                  value={date}
                  min={minDate()}
                  onChange={(e) => setDate(e.target.value)}
                  required
                />
              </div>

              <div className="modal-group">
                <label className="modal-label" htmlFor="rs-time">
                  <Clock size={14} /> New Time
                </label>
                <select
                  id="rs-time"
                  className="modal-input modal-select"
                  value={time}
                  onChange={(e) => setTime(e.target.value)}
                >
                  {[
                    '08:00','08:30','09:00','09:30','10:00','10:30','11:00','11:30',
                    '12:00','12:30','13:00','13:30','14:00','14:30','15:00','15:30',
                    '16:00','16:30','17:00',
                  ].map((t) => (
                    <option key={t} value={t}>{t}</option>
                  ))}
                </select>
              </div>
            </div>

            <div className="modal-actions">
              <button type="button" className="modal-btn-cancel" onClick={onClose}>
                Cancel
              </button>
              <button type="submit" className="modal-btn-primary" disabled={loading}>
                {loading ? <span className="modal-spinner" /> : 'Confirm Reschedule'}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}
