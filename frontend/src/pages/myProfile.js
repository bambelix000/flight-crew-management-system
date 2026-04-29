import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

function MyProfile() {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [newPhone, setNewPhone] = useState('');
  const [message, setMessage] = useState({ text: '', type: '' });

  useEffect(() => {
    const savedData = localStorage.getItem('user');
    const token = localStorage.getItem('token');
    
    if (!savedData || !token) {
      navigate('/');
      return;
    }
    
    const parsedUser = JSON.parse(savedData);

    // Aby pobrać statystyki, uderzamy do Twojego bezpiecznego endpointu:
    fetch(`http://localhost:8080/users/my-stats`, {
      method: 'GET',
      headers: { 
        'Authorization': `Bearer ${token}` 
      }
    })
      .then(res => {
        if (!res.ok) throw new Error("Błąd pobierania");
        return res.json();
      })
      .then(statsData => {
        setUser({ ...parsedUser, ...statsData });
        // Uwaga: Jeśli endpoint my-stats nie zwraca phoneNumber,
        // nowe uaktualnienie telefonu będzie puste na start.
        setNewPhone(statsData.phoneNumber || ''); 
      })
      .catch(err => {
        console.error(err);
        setMessage({ text: 'Nie udało się pobrać statystyk z serwera.', type: 'error' });
      });

  }, [navigate]);

  const handleUpdate = async () => {
    const token = localStorage.getItem('token'); 

    if (!token) {
      setMessage({ text: 'Błąd sesji: Brak tokenu. Zaloguj się ponownie.', type: 'error' });
      return;
    }

    try {
      // UWAGA: Nowy, bezpieczny endpoint stworzony przez Ciebie! Brak przekazywania ID.
      const res = await fetch(`http://localhost:8080/users/update-phone`, {
        method: 'PUT',
        headers: { 
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}` 
        },
        body: JSON.stringify({ phoneNumber: newPhone }) // Przekazujemy w formacie JSON dla wygody Springa
      });

      if (res.ok) {
        const updatedUser = { ...user, phoneNumber: newPhone };
        setUser(updatedUser);
        setMessage({ text: 'Numer telefonu zaktualizowany pomyślnie!', type: 'success' });
      } else {
        setMessage({ text: `Serwer odrzucił zmianę (Kod błędu: ${res.status})`, type: 'error' });
      }
    } catch (err) {
      setMessage({ text: 'Błąd połączenia z backendem.', type: 'error' });
    }
  };

  if (!user) return null;

  const styles = {
    page: { display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', backgroundColor: '#f0f2f5', fontFamily: '"Inter", sans-serif', padding: '20px' },
    card: { backgroundColor: '#ffffff', padding: '40px', borderRadius: '16px', boxShadow: '0 10px 25px rgba(0,0,0,0.05)', width: '100%', maxWidth: '500px' },
    header: { textAlign: 'center', marginBottom: '32px' },
    title: { margin: '0 0 8px 0', color: '#1a1f36', fontSize: '28px', fontWeight: '700' },
    badge: { display: 'inline-block', padding: '6px 12px', borderRadius: '20px', fontSize: '12px', fontWeight: '600', backgroundColor: '#ebf4ff', color: '#3182ce', textTransform: 'uppercase' },
    section: { marginBottom: '24px', borderBottom: '1px solid #edf2f7', paddingBottom: '16px' },
    sectionTitle: { fontSize: '12px', color: '#718096', fontWeight: '700', marginBottom: '16px', textTransform: 'uppercase', letterSpacing: '0.05em' },
    row: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' },
    label: { color: '#4f566b', fontSize: '14px' },
    value: { color: '#1a1f36', fontSize: '14px', fontWeight: '500' },
    input: { padding: '8px 12px', border: '1px solid #dcdfe4', borderRadius: '6px', fontSize: '14px', width: '200px', outline: 'none', transition: 'border-color 0.2s' },
    button: { width: '100%', padding: '12px', backgroundColor: '#5469d4', color: '#ffffff', border: 'none', borderRadius: '8px', fontSize: '15px', fontWeight: '600', cursor: 'pointer', marginBottom: '12px', transition: 'all 0.2s' },
    backLink: { display: 'block', textAlign: 'center', color: '#5469d4', textDecoration: 'none', fontSize: '14px', fontWeight: '500', cursor: 'pointer' },
    message: { padding: '12px', borderRadius: '8px', marginBottom: '24px', fontSize: '14px', textAlign: 'center', 
               backgroundColor: message.type === 'success' ? '#e3f9e5' : '#fff1f0', 
               color: message.type === 'success' ? '#1f7a28' : '#cf1322',
               display: message.text ? 'block' : 'none' }
  };

return (
    <div style={styles.page}>
      <div style={styles.card}>
        <div style={styles.header}>
          <h1 style={styles.title}>Mój Profil</h1>
          <span style={styles.badge}>{user.userRole}</span>
        </div>

        <div style={styles.message}>{message.text}</div>

        <div style={styles.section}>
          <h2 style={styles.sectionTitle}>Dane stałe</h2>
          <div style={styles.row}>
            <span style={styles.label}>Imię i Nazwisko</span>
            <span style={styles.value}>{user.name} {user.surname}</span>
          </div>
          <div style={styles.row}>
            <span style={styles.label}>Login</span>
            <span style={styles.value}>{user.login}</span>
          </div>
        </div>

        <div style={styles.section}>
          <h2 style={styles.sectionTitle}>Dane kontaktowe</h2>
          <div style={styles.row}>
            <span style={styles.label}>Numer telefonu</span>
            <input 
              style={styles.input} 
              type="text" 
              value={newPhone} 
              onChange={(e) => setNewPhone(e.target.value)} 
            />
          </div>
        </div>

        <div style={{ ...styles.section, borderBottom: 'none' }}>
          <h2 style={styles.sectionTitle}>Statystyki Nalotu</h2>
          <div style={styles.row}>
            <span style={styles.label}>Ostatnie 20 dni</span>
            <span style={styles.value}>{Math.floor((user.twentyDaysAirTime || 0) / 60)}h / 90h</span>
          </div>
          <div style={styles.row}>
            <span style={styles.label}>Rok kalendarzowy</span>
            <span style={styles.value}>{Math.floor((user.annualAirTime || 0) / 60)}h / 900h</span>
          </div>
        </div>

        <button style={styles.button} onClick={handleUpdate}>Zapisz zmiany</button>
        <div style={styles.backLink} onClick={() => navigate('/dashboard')}>Wróć do Dashboardu</div>
      </div>
    </div>
  );
}

export default MyProfile;