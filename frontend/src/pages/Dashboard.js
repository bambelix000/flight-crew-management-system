import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

function Dashboard() {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);

  useEffect(() => {
    const savedUser = localStorage.getItem('user');
    if (!savedUser) {
      navigate('/'); // Jeśli nie ma sesji, wróć do logowania
    } else {
      setUser(JSON.parse(savedUser));
    }
  }, [navigate]);

  const handleLogout = () => {
    localStorage.removeItem('user');
    navigate('/');
  };

  if (!user) return null;

  const styles = {
    container: { padding: '40px', fontFamily: '"Inter", sans-serif', backgroundColor: '#f0f2f5', minHeight: '100vh' },
    header: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '30px' },
    welcome: { fontSize: '24px', fontWeight: '700', color: '#1a1f36' },
    roleBadge: { backgroundColor: '#e2e8f0', padding: '4px 12px', borderRadius: '12px', fontSize: '12px', fontWeight: '600', color: '#475569', marginLeft: '10px' },
    logoutBtn: { padding: '10px 20px', backgroundColor: '#ffffff', border: '1px solid #dcdfe4', borderRadius: '8px', cursor: 'pointer', fontWeight: '600' },
    grid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '20px' },
    card: { backgroundColor: '#ffffff', padding: '24px', borderRadius: '12px', boxShadow: '0 4px 6px rgba(0,0,0,0.05)' },
    cardTitle: { fontSize: '16px', fontWeight: '600', color: '#4f566b', marginBottom: '16px' },
    statValue: { fontSize: '32px', fontWeight: '700', color: '#5469d4' },
    statLabel: { fontSize: '14px', color: '#7f8c8d', marginTop: '4px' }
  };

  return (
    <div style={styles.container}>
      <header style={styles.header}>
        <div>
          <span style={styles.welcome}>Witaj, {user.name} {user.surname}</span>
          <span style={styles.roleBadge}>{user.userRole}</span>
        </div>
        <button onClick={handleLogout} style={styles.logoutBtn}>Wyloguj się</button>
      </header>

      <div style={styles.grid}>
        {/* Sekcja statystyk - ważna dla CREWMEMBER */}
        <div style={styles.card}>
          <h3 style={styles.cardTitle}>Twoje Limity FTL</h3>
          <div>
            <div style={styles.statValue}>{user.twentyDaysAirTime / 60}h / 90h</div>
            <div style={styles.statLabel}>Czas lotu (ostatnie 20 dni)</div>
          </div>
          <div style={{ marginTop: '20px' }}>
            <div style={styles.statValue}>{user.annualAirTime / 60}h / 900h</div>
            <div style={styles.statLabel}>Czas lotu (rok kalendarzowy)</div>
          </div>
        </div>

        {/* Sekcja tylko dla Planisty / Administracji */}
        {(user.userRole === 'SCHEDULER' || user.userRole === 'ADMIN') && (
          <div style={styles.card}>
            <h3 style={styles.cardTitle}>Narzędzia Planowania</h3>
            <p style={{ color: '#4f566b', fontSize: '14px' }}>Masz dostęp do zarządzania lotami i przydzielania załogi.</p>
            <button style={{ 
              marginTop: '15px', padding: '10px', width: '100%', backgroundColor: '#5469d4', 
              color: '#fff', border: 'none', borderRadius: '6px', cursor: 'pointer' 
            }}>
              Zarządzaj Lotami
            </button>
          </div>
        )}

        {/* Dane kontaktowe */}
        <div style={styles.card}>
          <h3 style={styles.cardTitle}>Twoje Dane</h3>
          <p><strong>Telefon:</strong> {user.phoneNumber}</p>
          <p><strong>Login:</strong> {user.login}</p>
        </div>
      </div>
    </div>
  );
}

export default Dashboard;