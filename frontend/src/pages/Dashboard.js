import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

function Dashboard() {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [flights, setFlights] = useState([]);
  const [selectedFlights, setSelectedFlights] = useState([]);

  useEffect(() => {
    const token = localStorage.getItem('token');
    const savedData = localStorage.getItem('user');
    
    if (!token || !savedData) {
      handleLogout();
      return;
    }
    
    const parsedUser = JSON.parse(savedData);

    // 1. Pobieranie zaktualizowanego czasu lotu (FTL) z backendu
    fetch(`http://localhost:8080/users/${parsedUser.id}`, {
      method: 'GET',
      headers: { 
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}` 
      }
    })
      .then(res => {
        if (!res.ok) throw new Error("Błąd pobierania usera");
        return res.json();
      })
      .then(freshUserData => {
        setUser(freshUserData);
      })
      .catch(err => console.error("Nie udało się pobrać statystyk:", err));

    // 2. Pobieranie listy lotów (Tego brakowało!)
    fetchFlights(token);

  }, [navigate]);

  const fetchFlights = async (token) => {
    try {
      const res = await fetch('http://localhost:8080/flights', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      if (res.status === 401 || res.status === 403) {
        console.error("Token wygasł lub brak uprawnień.");
        handleLogout();
        return;
      }
      
      if (res.ok) {
        const data = await res.json();
        setFlights(data);
      }
    } catch (err) {
      console.error("Błąd pobierania lotów:", err);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('user');
    localStorage.removeItem('token');
    navigate('/');
  };

  const toggleFlightSelection = (id) => {
    setSelectedFlights(prev => 
      prev.includes(id) ? prev.filter(fid => fid !== id) : [...prev, id]
    );
  };

  if (!user) return null;

  const styles = {
    container: { padding: '40px', fontFamily: '"Inter", sans-serif', backgroundColor: '#f8f9fa', minHeight: '100vh' },
    card: { backgroundColor: '#fff', padding: '24px', borderRadius: '12px', boxShadow: '0 2px 12px rgba(0,0,0,0.04)', marginBottom: '20px' },
    table: { width: '100%', borderCollapse: 'collapse', marginTop: '10px' },
    th: { textAlign: 'left', padding: '12px', borderBottom: '2px solid #edf2f7', color: '#718096', fontSize: '13px', textTransform: 'uppercase' },
    td: { padding: '12px', borderBottom: '1px solid #edf2f7', color: '#2d3748', fontSize: '14px' },
    status: { padding: '4px 8px', borderRadius: '6px', fontSize: '12px', fontWeight: '600', backgroundColor: '#e3f9e5', color: '#1f7a28' }
  };

  return (
    <div style={styles.container}>
      <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '32px' }}>
        <h1 style={{ fontSize: '24px', color: '#1a1f36' }}>Panel Zarządzania Lotami</h1>
        <div>
          {/* Menu Nawigacyjne w Dashboardzie */}
          {user.userRole === 'ADMIN' && (
            <button onClick={() => navigate('/userList')} style={{ padding: '8px 16px', borderRadius: '8px', border: 'none', cursor: 'pointer', background: '#fed7d7', color: '#9b2c2c', fontWeight: '600', marginRight: '10px' }}>Użytkownicy</button>
          )}
          <button onClick={() => navigate('/myProfile')} style={{ padding: '8px 16px', borderRadius: '8px', border: 'none', cursor: 'pointer', background: '#5469d4', color: '#fff', fontWeight: '600', marginRight: '10px' }}>Mój Profil</button>
          <button onClick={handleLogout} style={{ padding: '8px 16px', borderRadius: '8px', border: '1px solid #dcdfe4', cursor: 'pointer', background: '#fff' }}>Wyloguj</button>
        </div>
      </header>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 300px', gap: '24px' }}>
        {/* LISTA LOTÓW */}
        <section style={styles.card}>
          <h2 style={{ fontSize: '18px', marginBottom: '20px' }}>Aktualne Loty</h2>
          <table style={styles.table}>
            <thead>
              <tr>
                {user.userRole === 'SCHEDULER' && <th style={styles.th}></th>}
                <th style={styles.th}>Nr Lotu</th>
                <th style={styles.th}>Trasa</th>
                <th style={styles.th}>Wylot</th>
                <th style={styles.th}>Czas</th>
              </tr>
            </thead>
            <tbody>
              {flights.map(f => (
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
          
          {user.userRole === 'SCHEDULER' && selectedFlights.length > 0 && (
            <button style={{ marginTop: '20px', padding: '12px 24px', backgroundColor: '#5469d4', color: '#fff', border: 'none', borderRadius: '8px', cursor: 'pointer', fontWeight: '600' }}>
              Utwórz służbę z wybranych lotów ({selectedFlights.length})
            </button>
          )}
        </section>

        {/* PROFIL I STATYSTYKI */}
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
              <p style={{ fontWeight: '700' }}>{Math.floor(user.twentyDaysAirTime / 60)}h / 90h</p>
            </div>
            <div>
              <p style={{ fontSize: '12px', color: '#718096' }}>Rok kalendarzowy</p>
              <p style={{ fontWeight: '700' }}>{Math.floor(user.annualAirTime / 60)}h / 900h</p>
            </div>
          </div>
        </aside>
      </div>
    </div>
  );
}

export default Dashboard;