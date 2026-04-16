import React, { useState } from 'react';

function App() {
  const [isLoginView, setIsLoginView] = useState(true);
  const [message, setMessage] = useState({ text: '', type: '' });
  
  const [loginData, setLoginData] = useState({ login: '', password: '' });
  const [regData, setRegData] = useState({ login: '', password: '', fullName: '', phoneNumber: '', userRole: 'CREWMEMBER' });

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const res = await fetch('http://localhost:8080/users/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(loginData)
      });
      const text = await res.text();
      if (res.ok) {
        setMessage({ text: text, type: 'success' });
      } else {
        setMessage({ text: 'Błędny login lub hasło', type: 'error' });
      }
    } catch (err) { 
      setMessage({ text: 'Błąd połączenia z serwerem', type: 'error' }); 
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    try {
      const res = await fetch('http://localhost:8080/users/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(regData)
      });
      if (res.ok) {
        const data = await res.json();
        setMessage({ text: `Zarejestrowano pomyślnie: ${data.fullName}! Możesz się zalogować.`, type: 'success' });
        setIsLoginView(true);
      } else {
        setMessage({ text: 'Błąd rejestracji. Login może być zajęty.', type: 'error' });
      }
    } catch (err) { 
      setMessage({ text: 'Błąd połączenia z serwerem', type: 'error' }); 
    }
  };

  const styles = {
    page: { display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', backgroundColor: '#f4f7f6', fontFamily: '"Segoe UI", Tahoma, Geneva, Verdana, sans-serif' },
    card: { backgroundColor: 'white', padding: '40px', borderRadius: '12px', boxShadow: '0 8px 24px rgba(0,0,0,0.1)', width: '100%', maxWidth: '400px', textAlign: 'center' },
    title: { margin: '0 0 20px 0', color: '#2c3e50', fontSize: '24px' },
    input: { width: '100%', padding: '12px', margin: '8px 0', border: '1px solid #ccc', borderRadius: '6px', boxSizing: 'border-box', fontSize: '14px' },
    select: { width: '100%', padding: '12px', margin: '8px 0', border: '1px solid #ccc', borderRadius: '6px', boxSizing: 'border-box', fontSize: '14px', backgroundColor: 'white' },
    button: { width: '100%', padding: '14px', margin: '16px 0 8px 0', backgroundColor: '#3498db', color: 'white', border: 'none', borderRadius: '6px', fontSize: '16px', fontWeight: 'bold', cursor: 'pointer', transition: 'background 0.3s' },
    switchText: { fontSize: '14px', color: '#7f8c8d', marginTop: '15px' },
    link: { color: '#3498db', cursor: 'pointer', fontWeight: 'bold', textDecoration: 'underline', background: 'none', border: 'none' },
    message: { padding: '10px', borderRadius: '6px', marginBottom: '20px', fontSize: '14px', 
               backgroundColor: message.type === 'success' ? '#d4edda' : '#f8d7da', 
               color: message.type === 'success' ? '#155724' : '#721c24', 
               display: message.text ? 'block' : 'none' }
  };

  return (
    <div style={styles.page}>
      <div style={styles.card}>
        <h1 style={styles.title}>Flight Crew Manager</h1>
        
        {}
        <div style={styles.message}>{message.text}</div>

        {isLoginView ? (
          // login
          <form onSubmit={handleLogin}>
            <input style={styles.input} type="text" placeholder="Login" required
                   onChange={e => setLoginData({...loginData, login: e.target.value})} />
            <input style={styles.input} type="password" placeholder="Hasło" required
                   onChange={e => setLoginData({...loginData, password: e.target.value})} />
            
            <button type="submit" style={styles.button}>Zaloguj się</button>
            
            <p style={styles.switchText}>
              Nie masz konta? <button type="button" style={styles.link} onClick={() => { setIsLoginView(false); setMessage({text: '', type: ''}); }}>Zarejestruj się</button>
            </p>
          </form>
        ) : (
          // register
          <form onSubmit={handleRegister}>
            <input style={styles.input} type="text" placeholder="Imię i Nazwisko" required
                   onChange={e => setRegData({...regData, fullName: e.target.value})} />
            <input style={styles.input} type="text" placeholder="Login" required
                   onChange={e => setRegData({...regData, login: e.target.value})} />
            <input style={styles.input} type="password" placeholder="Hasło" required
                   onChange={e => setRegData({...regData, password: e.target.value})} />
            <input style={styles.input} type="text" placeholder="Numer telefonu" required
                   onChange={e => setRegData({...regData, phoneNumber: e.target.value})} />
            
            <select style={styles.select} value={regData.userRole} onChange={e => setRegData({...regData, userRole: e.target.value})}>
              <option value="CREWMEMBER">Członek Załogi</option>
              <option value="SCHEDULER">Planista (Scheduler)</option>
              <option value="ADMIN">Administrator</option>
            </select>

            <button type="submit" style={{...styles.button, backgroundColor: '#2ecc71'}}>Utwórz konto</button>
            
            <p style={styles.switchText}>
              Masz już konto? <button type="button" style={styles.link} onClick={() => { setIsLoginView(true); setMessage({text: '', type: ''}); }}>Zaloguj się</button>
            </p>
          </form>
        )}
      </div>
    </div>
  );
}

export default App;