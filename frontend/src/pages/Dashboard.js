import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { listFlights } from '../api/flights';
import { getUser, logout } from '../api/auth';
import { useToast } from '../components/ToastProvider';
import Button from '../components/Button';
import Spinner from '../components/Spinner';

function Dashboard() {
  const navigate = useNavigate();
  const toast = useToast();
  const [user] = useState(() => getUser());
  const [flights, setFlights] = useState([]);
  const [selectedFlights, setSelectedFlights] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    listFlights()
      .then((data) => { if (!cancelled) setFlights(data || []); })
      .catch((err) => {
        if (cancelled) return;
        if (err?.status !== 401 && err?.status !== 403) {
          toast.error('Nie udało się pobrać lotów');
        }
      })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [toast]);

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  const toggleFlightSelection = (id) => {
    setSelectedFlights((prev) =>
      prev.includes(id) ? prev.filter((fid) => fid !== id) : [...prev, id]
    );
  };

  const handleCreateDuty = () => {
    const picked = flights.filter((f) => selectedFlights.includes(f.id));
    navigate('/duty/new', { state: { flightIds: selectedFlights, flights: picked } });
  };

  if (!user) return null;

  const styles = {
    container: { padding: '40px', fontFamily: '"Inter", sans-serif', backgroundColor: '#f8f9fa', minHeight: '100vh' },
    card: { backgroundColor: '#fff', padding: '24px', borderRadius: '12px', boxShadow: '0 2px 12px rgba(0,0,0,0.04)', marginBottom: '20px' },
    table: { width: '100%', borderCollapse: 'collapse', marginTop: '10px' },
    th: { textAlign: 'left', padding: '12px', borderBottom: '2px solid #edf2f7', color: '#718096', fontSize: '13px', textTransform: 'uppercase' },
    td: { padding: '12px', borderBottom: '1px solid #edf2f7', color: '#2d3748', fontSize: '14px' },
    status: { padding: '4px 8px', borderRadius: '6px', fontSize: '12px', fontWeight: '600', backgroundColor: '#e3f9e5', color: '#1f7a28' },
    emptyState: { padding: '32px', textAlign: 'center', color: '#718096', fontSize: '14px' },
    loadingWrap: { display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '40px' },
  };

  return (
    <div style={styles.container}>
      <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '32px' }}>
        <h1 style={{ fontSize: '24px', color: '#1a1f36' }}>Panel Zarządzania Lotami</h1>
        <button onClick={handleLogout} style={{ padding: '8px 16px', borderRadius: '8px', border: '1px solid #dcdfe4', cursor: 'pointer', background: '#fff' }}>Wyloguj</button>
      </header>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 300px', gap: '24px' }}>
        <section style={styles.card}>
          <h2 style={{ fontSize: '18px', marginBottom: '20px' }}>Aktualne Loty</h2>

          {loading ? (
            <div style={styles.loadingWrap}><Spinner size={28} /></div>
          ) : flights.length === 0 ? (
            <div style={styles.emptyState}>Brak lotów do wyświetlenia.</div>
          ) : (
            <table style={styles.table}>
              <thead>
                <tr>
                  {user.userRole === 'SCHEDULER' && <th></th>}
                  <th style={styles.th}>Nr Lotu</th>
                  <th style={styles.th}>Trasa</th>
                  <th style={styles.th}>Wylot</th>
                  <th style={styles.th}>Czas</th>
                </tr>
              </thead>
              <tbody>
                {flights.map((f) => (
                  <tr key={f.id}>
                    {user.userRole === 'SCHEDULER' && (
                      <td style={styles.td}>
                        <input type="checkbox" onChange={() => toggleFlightSelection(f.id)} checked={selectedFlights.includes(f.id)} />
                      </td>
                    )}
                    <td style={styles.td}><strong>{f.flightNumber}</strong></td>
                    <td style={styles.td}>{f.departureAirport} → {f.arrivalAirport}</td>
                    <td style={styles.td}>{new Date(f.departureTime).toLocaleString('pl-PL')}</td>
                    <td style={styles.td}>{f.durationMinutes} min</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}

          {user.userRole === 'SCHEDULER' && (
            <Button
              variant="primary"
              onClick={handleCreateDuty}
              disabled={selectedFlights.length === 0}
              style={{ marginTop: '20px' }}
            >
              Utwórz służbę z wybranych lotów ({selectedFlights.length})
            </Button>
          )}
        </section>

        <aside>
          <div style={styles.card}>
            <p style={{ fontSize: '14px', color: '#718096', marginBottom: '4px' }}>Zalogowany jako:</p>
            <p style={{ fontWeight: '700', fontSize: '18px' }}>{user.name} {user.surname}</p>
            <span style={{ ...styles.status, backgroundColor: '#ebf4ff', color: '#3182ce' }}>{user.userRole}</span>
          </div>

          <div style={styles.card}>
            <h3 style={{ fontSize: '16px', marginBottom: '16px' }}>Czas lotu (FTL)</h3>
            <div style={{ marginBottom: '12px' }}>
              <p style={{ fontSize: '12px', color: '#718096' }}>Ostatnie 20 dni</p>
              <p style={{ fontWeight: '700' }}>{Math.floor((user.twentyDaysAirTime || 0) / 60)}h / 90h</p>
            </div>
            <div>
              <p style={{ fontSize: '12px', color: '#718096' }}>Rok kalendarzowy</p>
              <p style={{ fontWeight: '700' }}>{Math.floor((user.annualAirTime || 0) / 60)}h / 900h</p>
            </div>
            <p style={{ fontSize: '11px', color: '#a0aec0', marginTop: '12px', fontStyle: 'italic' }}>
              {/* TODO: replace when GET /users/me ships */}
              Dane orientacyjne — brak endpointu do pobrania aktualnych godzin.
            </p>
          </div>
        </aside>
      </div>
    </div>
  );
}

export default Dashboard;
