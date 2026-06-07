import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

function MyProfile() {
  const navigate = useNavigate();
  const [userStats, setUserStats] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    const token = localStorage.getItem('token');
    const savedData = localStorage.getItem('user');
    
    if (!token || !savedData) {
      navigate('/');
      return;
    }

    fetch('http://localhost:8080/users/my-stats', {
      method: 'GET',
      headers: { 'Authorization': `Bearer ${token}` }
    })
      .then(res => {
        if (!res.ok) throw new Error("Nie udało się pobrać statystyk.");
        return res.json();
      })
      .then(data => {
        const parsedUser = JSON.parse(savedData);
        setUserStats({ ...parsedUser, ...data });
      })
      .catch(err => setError(err.message));
  }, [navigate]);

  if (error) {
    return <div style={{ padding: '40px', textAlign: 'center', color: '#c53030' }}>{error}</div>;
  }

  if (!userStats) return <div style={{ padding: '40px', textAlign: 'center' }}>Ładowanie profilu...</div>;

  const isScheduler = userStats.userRole === 'SCHEDULER' || userStats.userRole === 'ADMIN';

  const limitNearing20 = (userStats.twentyDaysAirTime || 0) >= 5100;
  const limitNearing365 = (userStats.annualAirTime || 0) >= 53700;
  const showWarning = !isScheduler && (limitNearing20 || limitNearing365);

  const styles = {
    container: { padding: '40px', fontFamily: '"Inter", sans-serif', backgroundColor: '#f8f9fa', minHeight: '100vh', display: 'flex', flexDirection: 'column', alignItems: 'center' },
    card: { backgroundColor: '#fff', padding: '32px', borderRadius: '12px', boxShadow: '0 4px 12px rgba(0,0,0,0.05)', width: '100%', maxWidth: '900px', marginBottom: '24px' },
    header: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '32px', width: '100%', maxWidth: '900px' },
    title: { fontSize: '24px', color: '#1a1f36', margin: 0 },
    backButton: { padding: '8px 16px', borderRadius: '8px', border: '1px solid #dcdfe4', cursor: 'pointer', background: '#fff', fontWeight: '600', color: '#4a5568' },
    profileInfo: { display: 'flex', alignItems: 'center', gap: '20px', borderBottom: isScheduler ? 'none' : '1px solid #edf2f7', paddingBottom: isScheduler ? '0' : '24px', marginBottom: isScheduler ? '0' : '24px' },
    avatar: { width: '80px', height: '80px', backgroundColor: '#ebf4ff', color: '#3182ce', borderRadius: '50%', display: 'flex', justifyContent: 'center', alignItems: 'center', fontSize: '32px', fontWeight: 'bold' },
    userName: { fontSize: '24px', fontWeight: 'bold', margin: '0 0 8px 0', color: '#2d3748' },
    roleBadge: { backgroundColor: '#e6fffa', color: '#285e61', padding: '6px 12px', borderRadius: '20px', fontSize: '12px', fontWeight: 'bold' },
    statsGrid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '20px' },
    statBox: { padding: '24px', borderRadius: '12px', textAlign: 'center' },
    statTitle: { fontSize: '14px', margin: '0 0 12px 0', textTransform: 'uppercase', fontWeight: 'bold' },
    statValue: { fontSize: '32px', fontWeight: 'bold', margin: 0 },
    warningBanner: { backgroundColor: '#fed7d7', color: '#9b2c2c', padding: '16px', borderRadius: '8px', fontWeight: 'bold', marginBottom: '24px', textAlign: 'center' }
  };

  return (
    <div style={styles.container}>
      <header style={styles.header}>
        <h1 style={styles.title}>Mój Profil</h1>
        <button onClick={() => navigate('/dashboard')} style={styles.backButton}>Powrót do Dashboardu</button>
      </header>

      <div style={styles.card}>
        {showWarning && (
          <div style={styles.warningBanner}>
            ⚠️ UWAGA FTL: Zbliżasz się do nieprzekraczalnych limitów czasu lotu. Zostało Ci mniej niż 5 godzin zaplanowanego lotu.
          </div>
        )}

        <div style={styles.profileInfo}>
          <div style={styles.avatar}>
            {userStats.login ? userStats.login.charAt(0).toUpperCase() : '?'}
          </div>
          <div>
            <h2 style={styles.userName}>{userStats.login}</h2>
            <span style={styles.roleBadge}>{userStats.userRole}</span>
          </div>
        </div>

        {!isScheduler && (
          <>
            <h3 style={{ fontSize: '18px', color: '#4a5568', marginBottom: '20px' }}>Całkowite Podsumowanie (All-time)</h3>
            <div style={styles.statsGrid}>
              <div style={{ ...styles.statBox, backgroundColor: '#ebf4ff' }}>
                <h4 style={{ ...styles.statTitle, color: '#2b6cb0' }}>Czas Służb (Duty)</h4>
                <p style={{ ...styles.statValue, color: '#1a365d' }}>
                  {Math.floor((userStats.totalDutyTimeMinutes || 0) / 60)}h {(userStats.totalDutyTimeMinutes || 0) % 60}m
                </p>
              </div>

              <div style={{ ...styles.statBox, backgroundColor: '#e6fffa' }}>
                <h4 style={{ ...styles.statTitle, color: '#285e61' }}>Czas Pracy (Work)</h4>
                <p style={{ ...styles.statValue, color: '#234e52' }}>
                  {Math.floor((userStats.totalWorkTimeMinutes || 0) / 60)}h {(userStats.totalWorkTimeMinutes || 0) % 60}m
                </p>
              </div>

              <div style={{ ...styles.statBox, backgroundColor: '#f0f4ff' }}>
                <h4 style={{ ...styles.statTitle, color: '#4c51bf' }}>W Powietrzu (Flight)</h4>
                <p style={{ ...styles.statValue, color: '#2b6cb0' }}>
                  {Math.floor((userStats.totalAirBorneTimeMinutes || 0) / 60)}h {(userStats.totalAirBorneTimeMinutes || 0) % 60}m
                </p>
              </div>

              <div style={{ ...styles.statBox, backgroundColor: '#fff5f5' }}>
                <h4 style={{ ...styles.statTitle, color: '#c53030' }}>Zgłoszone Incapacity</h4>
                <p style={{ ...styles.statValue, color: '#742a2a' }}>{userStats.incapacityCounter || 0}</p>
              </div>
              
              <div style={{ ...styles.statBox, backgroundColor: '#fefeb6', gridColumn: 'span 2' }}>
                <h4 style={{ ...styles.statTitle, color: '#744210' }}>Najczęstsza Rola</h4>
                <p style={{ ...styles.statValue, color: '#744210', fontSize: '24px' }}>
                  {userStats.mostFrequentRole || 'Brak lotów'}
                </p>
              </div>
            </div>

            <h3 style={{ fontSize: '18px', color: '#4a5568', marginTop: '40px', marginBottom: '20px' }}>Limity Czasu Lotu (FTL) - Kroczące</h3>
            <div style={styles.statsGrid}>
              <div style={{ ...styles.statBox, border: limitNearing20 ? '2px solid #feb2b2' : '1px solid #edf2f7' }}>
                <h4 style={{ ...styles.statTitle, color: '#718096' }}>Ostatnie 20 dni</h4>
                <p style={{ ...styles.statValue, color: limitNearing20 ? '#c53030' : '#2d3748' }}>
                  {Math.floor((userStats.twentyDaysAirTime || 0) / 60)}h / 90h
                </p>
              </div>

              <div style={{ ...styles.statBox, border: limitNearing365 ? '2px solid #feb2b2' : '1px solid #edf2f7' }}>
                <h4 style={{ ...styles.statTitle, color: '#718096' }}>Rok kalendarzowy</h4>
                <p style={{ ...styles.statValue, color: limitNearing365 ? '#c53030' : '#2d3748' }}>
                  {Math.floor((userStats.annualAirTime || 0) / 60)}h / 900h
                </p>
              </div>
            </div>
          </>
        )}

      </div>
    </div>
  );
}

export default MyProfile;