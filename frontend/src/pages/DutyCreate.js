import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { createDutyFromFlights } from '../api/duties';
import { useToast } from '../components/ToastProvider';
import Button from '../components/Button';
import Card from '../components/Card';
import { ApiError } from '../api/client';

function formatMinutes(total) {
  if (total == null) return '–';
  const h = Math.floor(total / 60);
  const m = total % 60;
  return h > 0 ? `${h}h ${m}min` : `${m}min`;
}

function formatDateTime(value) {
  if (!value) return '–';
  return new Date(value).toLocaleString('pl-PL');
}

function DutyCreate() {
  const navigate = useNavigate();
  const location = useLocation();
  const toast = useToast();

  const flightIds = location.state?.flightIds ?? [];
  const flightsPreview = location.state?.flights ?? [];

  const [duty, setDuty] = useState(null);
  const [creating, setCreating] = useState(false);

  useEffect(() => {
    if (flightIds.length === 0) {
      navigate('/dashboard', { replace: true });
    }
  }, [flightIds.length, navigate]);

  const handleCreate = async () => {
    setCreating(true);
    try {
      const created = await createDutyFromFlights(flightIds);
      setDuty(created);
      toast.success('Służba utworzona');
    } catch (err) {
      const msg = err instanceof ApiError ? err.message : 'Nie udało się utworzyć służby';
      toast.error(msg || 'Nie udało się utworzyć służby');
    } finally {
      setCreating(false);
    }
  };

  const styles = {
    container: { padding: '40px', fontFamily: '"Inter", sans-serif', backgroundColor: '#f8f9fa', minHeight: '100vh' },
    backLink: { background: 'none', border: 'none', color: '#5469d4', cursor: 'pointer', fontSize: '14px', padding: 0, marginBottom: '16px', fontWeight: 600 },
    title: { fontSize: '24px', color: '#1a1f36', marginBottom: '24px' },
    sectionTitle: { fontSize: '16px', color: '#1a1f36', marginBottom: '16px' },
    table: { width: '100%', borderCollapse: 'collapse' },
    th: { textAlign: 'left', padding: '10px 12px', borderBottom: '2px solid #edf2f7', color: '#718096', fontSize: '12px', textTransform: 'uppercase' },
    td: { padding: '10px 12px', borderBottom: '1px solid #edf2f7', color: '#2d3748', fontSize: '14px' },
    statRow: { display: 'flex', justifyContent: 'space-between', padding: '8px 0', borderBottom: '1px solid #edf2f7', fontSize: '14px' },
    statLabel: { color: '#718096' },
    statValue: { fontWeight: 600, color: '#1a1f36' },
    helperText: { fontSize: '13px', color: '#718096', marginTop: '16px', fontStyle: 'italic' },
  };

  return (
    <div style={styles.container}>
      <button type="button" style={styles.backLink} onClick={() => navigate('/dashboard')}>
        ← Wróć do dashboardu
      </button>
      <h1 style={styles.title}>Nowa służba</h1>

      <Card style={{ marginBottom: '20px' }}>
        <h2 style={styles.sectionTitle}>Wybrane loty</h2>
        {flightsPreview.length > 0 ? (
          <table style={styles.table}>
            <thead>
              <tr>
                <th style={styles.th}>Nr lotu</th>
                <th style={styles.th}>Trasa</th>
                <th style={styles.th}>Wylot</th>
                <th style={styles.th}>Przylot</th>
                <th style={styles.th}>Czas</th>
              </tr>
            </thead>
            <tbody>
              {flightsPreview.map((f) => (
                <tr key={f.id}>
                  <td style={styles.td}><strong>{f.flightNumber}</strong></td>
                  <td style={styles.td}>{f.departureAirport} → {f.arrivalAirport}</td>
                  <td style={styles.td}>{formatDateTime(f.departureTime)}</td>
                  <td style={styles.td}>{formatDateTime(f.arrivalTime)}</td>
                  <td style={styles.td}>{f.durationMinutes} min</td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <p style={{ color: '#718096', fontSize: '14px' }}>
            Wybrano {flightIds.length} lot(ów) (ID: {flightIds.join(', ')}).
          </p>
        )}

        {!duty && (
          <Button
            variant="primary"
            onClick={handleCreate}
            loading={creating}
            style={{ marginTop: '20px' }}
          >
            {creating ? 'Tworzenie…' : 'Utwórz służbę'}
          </Button>
        )}
      </Card>

      {duty && (
        <Card>
          <h2 style={styles.sectionTitle}>Utworzona służba #{duty.id}</h2>

          <div style={styles.statRow}>
            <span style={styles.statLabel}>Początek służby</span>
            <span style={styles.statValue}>{formatDateTime(duty.dutyStartTime)}</span>
          </div>
          <div style={styles.statRow}>
            <span style={styles.statLabel}>Koniec służby</span>
            <span style={styles.statValue}>{formatDateTime(duty.dutyEndTime)}</span>
          </div>
          <div style={styles.statRow}>
            <span style={styles.statLabel}>Czas pracy</span>
            <span style={styles.statValue}>{formatMinutes(duty.workTimeMinutes)}</span>
          </div>
          <div style={styles.statRow}>
            <span style={styles.statLabel}>Czas lotu</span>
            <span style={styles.statValue}>{formatMinutes(duty.airTimeMinutes)}</span>
          </div>

          {Array.isArray(duty.flights) && duty.flights.length > 0 && (
            <>
              <h3 style={{ ...styles.sectionTitle, marginTop: '24px' }}>Loty w służbie</h3>
              <table style={styles.table}>
                <thead>
                  <tr>
                    <th style={styles.th}>Nr lotu</th>
                    <th style={styles.th}>Trasa</th>
                    <th style={styles.th}>Wylot</th>
                    <th style={styles.th}>Czas</th>
                  </tr>
                </thead>
                <tbody>
                  {duty.flights.map((f) => (
                    <tr key={f.id}>
                      <td style={styles.td}><strong>{f.flightNumber}</strong></td>
                      <td style={styles.td}>{f.departureAirport} → {f.arrivalAirport}</td>
                      <td style={styles.td}>{formatDateTime(f.departureTime)}</td>
                      <td style={styles.td}>{f.durationMinutes} min</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </>
          )}

          <p style={styles.helperText}>
            Przypisywanie załogi do służby będzie dostępne wkrótce.
          </p>

          <div style={{ marginTop: '20px', display: 'flex', gap: '10px' }}>
            <Button variant="primary" onClick={() => navigate('/dashboard')}>
              Wróć do dashboardu
            </Button>
          </div>
        </Card>
      )}
    </div>
  );
}

export default DutyCreate;
