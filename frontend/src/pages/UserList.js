import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

function UserList() {
  const navigate = useNavigate();
  const [users, setUsers] = useState([]);
  const [error, setError] = useState('');

  // Stan odpowiadający za okienko (Modal)
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingUser, setEditingUser] = useState(null);

  const token = localStorage.getItem('token');

  const fetchData = () => {
    fetch('http://localhost:8080/users', {
      headers: { 'Authorization': `Bearer ${token}` }
    })
      .then(res => {
        if (!res.ok) throw new Error("Błąd pobierania danych");
        return res.json();
      })
      .then(data => setUsers(data))
      .catch(err => setError(err.message));
  };

  useEffect(() => {
    const savedUser = localStorage.getItem('user');
    if (!savedUser) {
      navigate('/');
      return;
    }
    
    const parsedUser = JSON.parse(savedUser);
    if (parsedUser.userRole !== 'ADMIN') {
      navigate('/dashboard');
      return;
    }

    fetchData();
  }, [navigate, token]);

  // Otwieranie okienka
  const handleEditClick = (user) => {
    setEditingUser({ ...user }); // Tworzymy kopię danych klikniętego użytkownika
    setIsModalOpen(true);
  };

  // Zamykanie okienka
  const handleCloseModal = () => {
    setIsModalOpen(false);
    setEditingUser(null);
  };

  // Zapisywanie zmian w okienku
  const handleSave = async (e) => {
    e.preventDefault(); // Zapobiega przeładowaniu strony
    try {
      const res = await fetch(`http://localhost:8080/users/${editingUser.id}`, {
        method: 'PUT',
        headers: { 
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}` 
        },
        body: JSON.stringify(editingUser)
      });
      
      if (res.ok) {
        handleCloseModal(); // Zamknij modal
        fetchData(); // Odśwież tabelę, żeby pokazać nowe dane
      } else {
        alert('Wystąpił błąd podczas zapisywania zmian na serwerze.');
      }
    } catch (err) {
      alert('Błąd połączenia z serwerem.');
    }
  };

  const styles = {
    page: { display: 'flex', justifyContent: 'center', alignItems: 'flex-start', minHeight: '100vh', backgroundColor: '#f0f2f5', fontFamily: '"Inter", sans-serif', padding: '40px 20px' },
    card: { backgroundColor: '#ffffff', padding: '40px', borderRadius: '16px', boxShadow: '0 10px 25px rgba(0,0,0,0.05)', width: '100%', maxWidth: '900px' },
    title: { margin: '0 0 24px 0', color: '#1a1f36', fontSize: '24px', fontWeight: '700', textAlign: 'center' },
    table: { width: '100%', borderCollapse: 'collapse', marginTop: '20px' },
    th: { backgroundColor: '#f7fafc', color: '#4a5568', padding: '12px', textAlign: 'left', borderBottom: '2px solid #e2e8f0', fontSize: '14px', textTransform: 'uppercase' },
    td: { padding: '16px 12px', borderBottom: '1px solid #edf2f7', color: '#1a1f36', fontSize: '14px', verticalAlign: 'middle' },
    badge: { padding: '4px 8px', borderRadius: '12px', fontSize: '11px', fontWeight: 'bold', textTransform: 'uppercase' },
    backLink: { display: 'block', textAlign: 'center', color: '#5469d4', textDecoration: 'none', fontSize: '14px', fontWeight: '500', cursor: 'pointer', marginTop: '24px' },
    error: { color: '#e53e3e', textAlign: 'center', marginBottom: '16px' },
    btnEdit: { backgroundColor: '#edf2f7', color: '#4a5568', border: '1px solid #cbd5e0', padding: '6px 12px', borderRadius: '6px', fontSize: '13px', fontWeight: '600', cursor: 'pointer', transition: 'all 0.2s' },
    
    // Strefa styli dla Okienka (Modala)
    modalOverlay: { position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.4)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000 },
    modalCard: { backgroundColor: '#ffffff', padding: '32px', borderRadius: '12px', width: '100%', maxWidth: '400px', boxShadow: '0 20px 25px -5px rgba(0,0,0,0.1)' },
    modalTitle: { margin: '0 0 24px 0', fontSize: '20px', color: '#1a1f36', fontWeight: '700' },
    formGroup: { marginBottom: '16px' },
    label: { display: 'block', marginBottom: '6px', fontSize: '13px', color: '#4a5568', fontWeight: '600' },
    input: { width: '100%', boxSizing: 'border-box', padding: '10px 12px', border: '1px solid #dcdfe4', borderRadius: '6px', fontSize: '14px', outline: 'none' },
    select: { width: '100%', boxSizing: 'border-box', padding: '10px 12px', border: '1px solid #dcdfe4', borderRadius: '6px', fontSize: '14px', outline: 'none', backgroundColor: '#fff' },
    modalActions: { display: 'flex', justifyContent: 'flex-end', gap: '12px', marginTop: '32px' },
    btnCancel: { padding: '10px 16px', backgroundColor: '#fff', border: '1px solid #dcdfe4', borderRadius: '6px', cursor: 'pointer', fontWeight: '600', color: '#4a5568' },
    btnSave: { padding: '10px 16px', backgroundColor: '#5469d4', border: 'none', borderRadius: '6px', cursor: 'pointer', fontWeight: '600', color: '#fff' }
  };

  const getRoleBadgeStyle = (role) => {
    switch(role) {
      case 'ADMIN': return { ...styles.badge, backgroundColor: '#fed7d7', color: '#9b2c2c' };
      case 'SCHEDULER': return { ...styles.badge, backgroundColor: '#feebc8', color: '#c05621' };
      default: return { ...styles.badge, backgroundColor: '#e2e8f0', color: '#4a5568' }; // CREWMEMBER
    }
  };

  return (
    <div style={styles.page}>
      <div style={styles.card}>
        <h1 style={styles.title}>Baza Użytkowników</h1>
        
        {error && <div style={styles.error}>{error}</div>}

        <table style={styles.table}>
          <thead>
            <tr>
              <th style={styles.th}>ID</th>
              <th style={styles.th}>Imię i Nazwisko</th>
              <th style={styles.th}>Login</th>
              <th style={styles.th}>Telefon</th>
              <th style={styles.th}>Rola</th>
              <th style={styles.th}>Akcje</th>
            </tr>
          </thead>
          <tbody>
            {users.map(u => (
              <tr key={u.id}>
                <td style={styles.td}>{u.id}</td>
                <td style={styles.td}><strong>{u.name} {u.surname}</strong></td>
                <td style={styles.td}>{u.login}</td>
                <td style={styles.td}>{u.phoneNumber}</td>
                <td style={styles.td}>
                  <span style={getRoleBadgeStyle(u.userRole)}>{u.userRole}</span>
                </td>
                <td style={styles.td}>
                  <button style={styles.btnEdit} onClick={() => handleEditClick(u)}>Edytuj</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        <div style={styles.backLink} onClick={() => navigate('/dashboard')}>Wróć do Dashboardu</div>
      </div>

      {/* OKIENKO (MODAL) DO EDYCJI */}
      {isModalOpen && editingUser && (
        <div style={styles.modalOverlay}>
          <div style={styles.modalCard}>
            <h2 style={styles.modalTitle}>Edytuj Użytkownika</h2>
            <form onSubmit={handleSave}>
              <div style={styles.formGroup}>
                <label style={styles.label}>Imię</label>
                <input style={styles.input} type="text" value={editingUser.name} onChange={e => setEditingUser({...editingUser, name: e.target.value})} required />
              </div>
              <div style={styles.formGroup}>
                <label style={styles.label}>Nazwisko</label>
                <input style={styles.input} type="text" value={editingUser.surname} onChange={e => setEditingUser({...editingUser, surname: e.target.value})} required />
              </div>
              <div style={styles.formGroup}>
                <label style={styles.label}>Login</label>
                <input style={styles.input} type="text" value={editingUser.login} onChange={e => setEditingUser({...editingUser, login: e.target.value})} required />
              </div>
              <div style={styles.formGroup}>
                <label style={styles.label}>Telefon</label>
                <input style={styles.input} type="text" value={editingUser.phoneNumber} onChange={e => setEditingUser({...editingUser, phoneNumber: e.target.value})} required />
              </div>
              <div style={styles.formGroup}>
                <label style={styles.label}>Rola</label>
                <select style={styles.select} value={editingUser.userRole} onChange={e => setEditingUser({...editingUser, userRole: e.target.value})}>
                  <option value="CREWMEMBER">CREWMEMBER</option>
                  <option value="SCHEDULER">SCHEDULER</option>
                  <option value="ADMIN">ADMIN</option>
                </select>
              </div>
              <div style={styles.modalActions}>
                <button type="button" style={styles.btnCancel} onClick={handleCloseModal}>Anuluj</button>
                <button type="submit" style={styles.btnSave}>Zapisz zmiany</button>
              </div>
            </form>
          </div>
        </div>
      )}

    </div>
  );
}

export default UserList;