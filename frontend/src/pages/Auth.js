import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import * as authApi from '../api/auth';
import { useToast } from '../components/ToastProvider';
import { ApiError } from '../api/client';

function Auth() {
  const navigate = useNavigate();
  const toast = useToast();
  const [isLoginView, setIsLoginView] = useState(true);
  const [showPassword, setShowPassword] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const [loginData, setLoginData] = useState({ login: '', password: '' });
  const [regData, setRegData] = useState({ login: '', password: '', name: '', surname: '', phoneNumber: '', userRole: 'CREWMEMBER' });

  const switchView = (toLogin) => {
    setIsLoginView(toLogin);
    setShowPassword(false);
    setLoginData({ login: '', password: '' });
    setRegData({ login: '', password: '', name: '', surname: '', phoneNumber: '', userRole: 'CREWMEMBER' });
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await authApi.login(loginData);
      navigate('/dashboard');
    } catch (err) {
      if (err instanceof ApiError && err.status === 0) {
        toast.error('Błąd połączenia z serwerem');
      } else {
        toast.error('Błędny login lub hasło');
      }
    } finally {
      setSubmitting(false);
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      const user = await authApi.register(regData);
      toast.success(`Zarejestrowano pomyślnie: ${user.name} ${user.surname}! Możesz się zalogować.`);
      setIsLoginView(true);
      setShowPassword(false);
      setLoginData({ login: regData.login, password: '' });
      setRegData({ login: '', password: '', name: '', surname: '', phoneNumber: '', userRole: 'CREWMEMBER' });
    } catch (err) {
      if (err instanceof ApiError && err.status === 0) {
        toast.error('Błąd połączenia z serwerem');
      } else {
        toast.error('Błąd rejestracji. Login może być zajęty.');
      }
    } finally {
      setSubmitting(false);
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
    buttonDisabled: { opacity: 0.65, cursor: 'not-allowed' },
    footer: { marginTop: '24px', textAlign: 'center', fontSize: '14px', color: '#4f566b' },
    link: { color: '#5469d4', fontWeight: '600', cursor: 'pointer', border: 'none', background: 'none', padding: '0', fontSize: '14px', textDecoration: 'none' },
  };

  const EyeIcon = () => (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
    </svg>
  );

  const EyeOffIcon = () => (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/>
    </svg>
  );

  const submitColor = isLoginView ? '#5469d4' : '#2ecc71';
  const submitStyle = {
    ...styles.button,
    backgroundColor: submitColor,
    ...(submitting ? styles.buttonDisabled : null),
  };

  return (
    <div style={styles.page}>
      <div style={styles.card}>
        <h1 style={styles.title}>Flight Crew</h1>
        <p style={styles.subtitle}>{isLoginView ? 'Zaloguj się do panelu' : 'Utwórz nowe konto'}</p>

        <form onSubmit={isLoginView ? handleLogin : handleRegister}>
          {!isLoginView && (
            <div style={styles.inputGroup}>
              <label style={styles.label}>Imię i Nazwisko</label>
              <div style={{ display: 'flex', gap: '10px' }}>
                <input style={styles.input} type="text" placeholder="np. Jan" required
                       value={regData.name} onChange={e => setRegData({...regData, name: e.target.value})} />
                <input style={styles.input} type="text" placeholder="np. Kowalski" required
                       value={regData.surname} onChange={e => setRegData({...regData, surname: e.target.value})} />
              </div>
            </div>
          )}

          <div style={styles.inputGroup}>
            <label style={styles.label}>Login</label>
            <input style={styles.input} type="text" placeholder="np. jkowalski" required
                   value={isLoginView ? loginData.login : regData.login}
                   onChange={e => isLoginView ? setLoginData({...loginData, login: e.target.value}) : setRegData({...regData, login: e.target.value})} />
          </div>

          <div style={styles.inputGroup}>
            <label style={styles.label}>Hasło</label>
            <div style={styles.passwordContainer}>
              <input style={styles.passwordInput} type={showPassword ? "text" : "password"} placeholder="" required
                     value={isLoginView ? loginData.password : regData.password}
                     onChange={e => isLoginView ? setLoginData({...loginData, password: e.target.value}) : setRegData({...regData, password: e.target.value})} />
              <button type="button" style={styles.iconButton} aria-label={showPassword ? 'Ukryj hasło' : 'Pokaż hasło'} onClick={() => setShowPassword(!showPassword)}>
                {showPassword ? <EyeOffIcon /> : <EyeIcon />}
              </button>
            </div>
          </div>

          {!isLoginView && (
            <>
              <div style={styles.inputGroup}>
                <label style={styles.label}>Numer telefonu</label>
                <input style={styles.input} type="text" placeholder="+48 123 456 789" required
                       value={regData.phoneNumber} onChange={e => setRegData({...regData, phoneNumber: e.target.value})} />
              </div>
              <div style={styles.inputGroup}>
                <label style={styles.label}>Rola w systemie</label>
                <select style={styles.input} value={regData.userRole} onChange={e => setRegData({...regData, userRole: e.target.value})}>
                  <option value="CREWMEMBER">Członek Załogi</option>
                  <option value="SCHEDULER">Planista</option>
                  <option value="ADMIN">Administrator</option>
                </select>
              </div>
            </>
          )}

          <button type="submit" disabled={submitting} style={submitStyle}>
            {submitting ? 'Proszę czekać…' : (isLoginView ? 'Zaloguj się' : 'Utwórz konto')}
          </button>
        </form>

        <div style={styles.footer}>
          {isLoginView ? (
            <>Nie masz konta? <button style={styles.link} onClick={() => switchView(false)}>Zarejestruj się</button></>
          ) : (
            <>Masz już konto? <button style={styles.link} onClick={() => switchView(true)}>Zaloguj się</button></>
          )}
        </div>
      </div>
    </div>
  );
}

export default Auth;
