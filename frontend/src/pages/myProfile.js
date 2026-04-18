import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

function MyProfile() {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [newPhone, setNewPhone] = useState('');
  const [message, setMessage] = useState({ text: '', type: '' });

  useEffect(() => {
    const savedUser = localStorage.getItem('user');
    if (!savedUser) {
      navigate('/');
    } else {
      const parsedUser = JSON.parse(savedUser);
      setUser(parsedUser);
      setNewPhone(parsedUser.phoneNumber || '');
    }
  }, [navigate]);

  const handleUpdate = async () => {
    try {
      const res = await fetch(`http://localhost:8080/users/${user.id}/phone`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ phoneNumber: newPhone })
      });

      if (res.ok) {
        const updatedUser = { ...user, phoneNumber: newPhone };
        setUser(updatedUser);
        localStorage.setItem('user', JSON.stringify(updatedUser)); // Aktualizacja sesji
        setMessage({ text: 'Numer telefonu został zaktualizowany!', type: 'success' });
      } else {
        setMessage({ text: 'Błąd podczas aktualizacji danych.', type: 'error' });
      }
    } catch (err) {
      setMessage({ text: 'Błąd połączenia z serwerem.', type: 'error' });
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
    button: { width: '100%', padding: '12px', backgroundColor: '#5469d4', color: '#ffffff', border: 'none', borderRadius: '8px', fontSize: '15px', fontWeight: '600', cursor: 'pointer', marginBottom: '12px' },
    backLink: { display: 'block', textAlign: 'center', color: '#5469d4', textDecoration: 'none', fontSize: '14px', fontWeight: '500', cursor: 'pointer' },
    message: { 
      padding: '10px', 
      borderRadius: '6px', 
      marginBottom: '20px', 
      fontSize: '13px', 
      textAlign: 'center', 
      backgroundColor: message.type === 'success' ? '#e3f9e5' : '#fff1f0', 
      color: message.type === 'success' ? '#1f7a28' : '#cf1322',
      display: message.text ? 'block' : 'none' 
    }
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

        {/* Naprawiony podwójny atrybut style w tym divie */}
        <div style={{ ...styles.section, borderBottom: 'none' }}>
          <h2 style={styles.sectionTitle}>Statystyki Nalotu</h2>
          <div style={styles.row}>
            <span style={styles.label}>Ostatnie 20 dni</span>
            <span style={styles.value}>{Math.floor(user.twentyDaysAirTime / 60)}h / 90h</span>
          </div>
          <div style={styles.row}>
            <span style={styles.label}>Rok kalendarzowy</span>
            <span style={styles.value}>{Math.floor(user.annualAirTime / 60)}h / 900h</span>
          </div>
        </div>

        <button style={styles.button} onClick={handleUpdate}>Zapisz zmiany</button>
        <div style={styles.backLink} onClick={() => navigate('/dashboard')}>Wróć do Dashboardu</div>
      </div>
    </div>
  );
}

export default MyProfile;