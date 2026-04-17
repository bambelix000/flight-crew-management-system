import React from 'react';
import { useNavigate } from 'react-router-dom';

function Dashboard() {
  const navigate = useNavigate();

  const handleLogout = () => {
    // Tymczasowe wylogowanie
    navigate('/');
  };

  return (
    <div style={{ padding: '20px', fontFamily: 'Arial' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2>Panel Główny</h2>
        <button onClick={handleLogout} style={{ padding: '8px 16px', cursor: 'pointer' }}>Wyloguj</button>
      </div>
      <p>Witaj w systemie zarządzania załogą!</p>
    </div>
  );
}

export default Dashboard;