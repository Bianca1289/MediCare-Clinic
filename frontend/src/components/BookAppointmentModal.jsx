import { useState } from 'react';
import { X, CalendarDays, Clock, FileText, CircleAlert, CircleCheck } from 'lucide-react';
import { bookAppointment } from '../api/appointments';
import './BookAppointmentModal.css';

function pad(n) { return String(n).padStart(2, '0'); }

function toISO(dateStr, timeStr) {
  return `${dateStr}T${timeStr}:00`;
}

function minDate() {
  const d = new Date();
  d.setDate(d.getDate() + 1);
  return d.toISOString().slice(0, 10);
}

export default function BookAppointmentModal({ doctor, onClose, onBooked }) {
  const [date, setDate] = useState('');
  const [time, setTime] = useState('09:00');
  const [notes, setNotes] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [done, setDone] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    if (!date) { setError('Please select a date.'); return; }
    setLoading(true);
    try {
      await bookAppointment(doctor.username, toISO(date, time), notes.trim() || null);
      setDone(true);
      onBooked?.();
    } catch (err) {
      const msg = err?.response?.data?.message || err?.response?.data?.error || 'Could not book appointment. Please try again.';
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
            <h2 className="modal-title">Book Appointment</h2>
            <p className="modal-sub">
              with <strong>Dr. {doctor.username.charAt(0).toUpperCase() + doctor.username.slice(1)}</strong>
              {doctor.location ? ` · ${doctor.location}` : ''}
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
            <h3>Appointment Booked!</h3>
            <p>Your appointment has been scheduled for <strong>{date}</strong> at <strong>{time}</strong>.</p>
            <button className="modal-btn-primary" onClick={onClose}>Close</button>
          </div>
        ) : (
          <form className="modal-form" onSubmit={handleSubmit}>
            {doctor.specialties?.length > 0 && (
              <div className="modal-spec-row">
                {doctor.specialties.map((s) => (
                  <span key={s} className="specialty-badge">{s}</span>
                ))}
              </div>
            )}

            {error && (
              <div className="modal-alert modal-alert--error">
                <CircleAlert size={15} />
                <span>{error}</span>
              </div>
            )}

            <div className="modal-row">
              <div className="modal-group">
                <label className="modal-label" htmlFor="appt-date">
                  <CalendarDays size={14} /> Date
                </label>
                <input
                  id="appt-date"
                  className="modal-input"
                  type="date"
                  value={date}
                  min={minDate()}
                  onChange={(e) => setDate(e.target.value)}
                  required
                />
              </div>

              <div className="modal-group">
                <label className="modal-label" htmlFor="appt-time">
                  <Clock size={14} /> Time
                </label>
                <select
                  id="appt-time"
                  className="modal-input modal-select"
                  value={time}
                  onChange={(e) => setTime(e.target.value)}
                >
                  {['08:00','08:30','09:00','09:30','10:00','10:30','11:00','11:30',
                    '12:00','12:30','13:00','13:30','14:00','14:30','15:00','15:30',
                    '16:00','16:30','17:00'].map((t) => (
                    <option key={t} value={t}>{t}</option>
                  ))}
                </select>
              </div>
            </div>

            <div className="modal-group">
              <label className="modal-label" htmlFor="appt-notes">
                <FileText size={14} /> Notes <span className="modal-optional">(optional)</span>
              </label>
              <textarea
                id="appt-notes"
                className="modal-input modal-textarea"
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                placeholder="Describe your symptoms or reason for visit..."
                rows={3}
                maxLength={500}
              />
            </div>

            <div className="modal-actions">
              <button type="button" className="modal-btn-cancel" onClick={onClose}>
                Cancel
              </button>
              <button type="submit" className="modal-btn-primary" disabled={loading}>
                {loading ? <span className="modal-spinner" /> : 'Confirm Booking'}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}
