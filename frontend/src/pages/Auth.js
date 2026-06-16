import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

function Auth() {
  const navigate = useNavigate();
  const [message, setMessage] = useState({ text: '', type: '' });
  const [showPassword, setShowPassword] = useState(false);
  const [loginData, setLoginData] = useState({ login: '', password: '' });

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const res = await fetch('http://localhost:8080/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(loginData)
      });

      if (res.ok) {
        const authResponse = await res.json();

        localStorage.setItem('token', authResponse.token);
        localStorage.setItem('user', JSON.stringify({
          login: authResponse.login,
          userRole: authResponse.role
        }));

        navigate('/dashboard');
      } else {
        setMessage({ text: 'Błędny login lub hasło', type: 'error' });
      }
    } catch (err) {
      setMessage({ text: 'Błąd połączenia z serwerem', type: 'error' });
    }
  };

  const styles = {
    page: { display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', backgroundColor: '#f0f2f5', fontFamily: '"Inter", "Segoe UI", sans-serif' },
    card: { backgroundColor: '#ffffff', padding: '40px', borderRadius: '16px', boxShadow: '0 10px 25px rgba(0,0,0,0.05)', width: '100%', maxWidth: '400px' },
    title: { margin: '0 0 8px 0', color: '#1a1f36', fontSize: '28px', fontWeight: '700', textAlign: 'center' },
    subtitle: { margin: '0 0 32px 0', color: '#4f566b', fontSize: '16px', textAlign: 'center' },
    inputGroup: { marginBottom: '20px' },
    label: { display: 'block', marginBottom: '8px', fontSize: '14px', fontWeight: '500', color: '#1a1f36', textAlign: 'left' },
    input: { width: '100%', padding: '12px 16px', border: '1px solid #dcdfe4', borderRadius: '8px', fontSize: '15px', color: '#1a1f36', transition: 'border-color 0.2s', outline: 'none', boxSizing: 'border-box' },
    passwordContainer: { position: 'relative' },
    passwordInput: { width: '100%', padding: '12px 16px', paddingRight: '46px', border: '1px solid #dcdfe4', borderRadius: '8px', fontSize: '15px', outline: 'none', boxSizing: 'border-box' },
    iconButton: { position: 'absolute', right: '12px', top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', padding: '4px', cursor: 'pointer', display: 'flex', alignItems: 'center', color: '#a3acb9' },
    button: { width: '100%', padding: '12px', backgroundColor: '#5469d4', color: '#ffffff', border: 'none', borderRadius: '8px', fontSize: '16px', fontWeight: '600', cursor: 'pointer', transition: 'all 0.2s', boxShadow: '0 2px 4px rgba(0,0,0,0.08)' },
    footer: { marginTop: '24px', textAlign: 'center', fontSize: '13px', color: '#718096', lineHeight: 1.5 },
    message: {
      padding: '12px',
      borderRadius: '8px',
      marginBottom: '24px',
      fontSize: '14px',
      textAlign: 'center',
      backgroundColor: '#fff1f0',
      color: '#cf1322',
      border: '1px solid #ffa39e',
      display: message.text ? 'block' : 'none'
    }
  };

  const EyeIcon = () => (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" /><circle cx="12" cy="12" r="3" />
    </svg>
  );

  const EyeOffIcon = () => (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" /><line x1="1" y1="1" x2="23" y2="23" />
    </svg>
  );

  return (
    <div style={styles.page}>
      <div style={styles.card}>
        <h1 style={styles.title}>Flight Crew</h1>
        <p style={styles.subtitle}>Zaloguj się do panelu</p>

        <div style={styles.message}>{message.text}</div>

        <form onSubmit={handleLogin}>
          <div style={styles.inputGroup}>
            <label style={styles.label}>Login</label>
            <input
              style={styles.input}
              type="text"
              placeholder="np. jkowalski"
              required
              value={loginData.login}
              onChange={e => setLoginData({ ...loginData, login: e.target.value })}
            />
          </div>

          <div style={styles.inputGroup}>
            <label style={styles.label}>Hasło</label>
            <div style={styles.passwordContainer}>
              <input
                style={styles.passwordInput}
                type={showPassword ? 'text' : 'password'}
                required
                value={loginData.password}
                onChange={e => setLoginData({ ...loginData, password: e.target.value })}
              />
              <button type="button" style={styles.iconButton} onClick={() => setShowPassword(!showPassword)} aria-label={showPassword ? 'Ukryj hasło' : 'Pokaż hasło'}>
                {showPassword ? <EyeOffIcon /> : <EyeIcon />}
              </button>
            </div>
          </div>

          <button type="submit" style={styles.button}>
            Zaloguj się
          </button>
        </form>

        <div style={styles.footer}>
          Nowe konta tworzy administrator w panelu użytkowników.
        </div>
      </div>
    </div>
  );
}

export default Auth;
