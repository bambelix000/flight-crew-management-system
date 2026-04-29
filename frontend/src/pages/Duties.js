import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

const Duties = ({ user, token }) => {
  const navigate = useNavigate();
  const isScheduler = user.userRole === 'SCHEDULER' || user.userRole === 'ADMIN';
  
  const [activeTab, setActiveTab] = useState(isScheduler ? 'unassigned' : 'myDuties');
  const [duties, setDuties] = useState([]);
  const [flights, setFlights] = useState([]);
  const [selectedFlightIds, setSelectedFlightIds] = useState([]);
  const [expandedDutyId, setExpandedDutyId] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);

  useEffect(() => {
    fetchData();
  }, [activeTab]);

  const fetchData = async () => {
    try {
      if (activeTab === 'myDuties') {
        const res = await fetch('http://localhost:8080/duties/my-duties', {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        if (res.ok) setDuties(await res.json());
      } else if (activeTab === 'createdDuties') {
        const res = await fetch('http://localhost:8080/duties', {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        if (res.ok) setDuties(await res.json());
      } else if (activeTab === 'unassigned') {
        const res = await fetch('http://localhost:8080/flights', {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        if (res.ok) {
          const allFlights = await res.json();
          setFlights(allFlights);
        }
      }
    } catch (err) {
      console.error(err);
    }
  };

  const toggleDutyExpand = (dutyId) => {
    setExpandedDutyId(expandedDutyId === dutyId ? null : dutyId);
  };

  const handleCheckboxToggle = (flightId) => {
    setSelectedFlightIds(prev => 
      prev.includes(flightId) 
        ? prev.filter(id => id !== flightId)
        : [...prev, flightId]
    );
  };

  const handleCreateDutySubmit = async () => {
    try {
      const res = await fetch('http://localhost:8080/duties/create', {
        method: 'POST',
        headers: { 
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}` 
        },
        body: JSON.stringify({ flightIds: selectedFlightIds })
      });
      if (res.ok) {
        setIsModalOpen(false);
        setSelectedFlightIds([]);
        setActiveTab('createdDuties');
      }
    } catch (err) {
      console.error(err);
    }
  };

  const selectedFlightsData = flights.filter(f => selectedFlightIds.includes(f.id));

  return (
    <div style={styles.page}>
      <div style={styles.card}>
        <div style={styles.header}>
          <h1 style={styles.title}>Zarządzanie Służbami</h1>
          <div style={styles.backLink} onClick={() => navigate('/dashboard')}>Powrót</div>
        </div>

        {isScheduler && (
          <div style={styles.tabsContainer}>
            <button 
              style={activeTab === 'unassigned' ? styles.activeTab : styles.tab} 
              onClick={() => setActiveTab('unassigned')}
            >
              Nieprzypisane Loty
            </button>
            <button 
              style={activeTab === 'createdDuties' ? styles.activeTab : styles.tab} 
              onClick={() => setActiveTab('createdDuties')}
            >
              Utworzone Służby
            </button>
          </div>
        )}

        {!isScheduler && (
          <div style={styles.tabsContainer}>
            <button style={styles.activeTab}>Moje Służby</button>
          </div>
        )}

        {(activeTab === 'myDuties' || activeTab === 'createdDuties') && (
          <div style={styles.listContainer}>
            {duties.map(duty => (
              <div key={duty.id} style={styles.dutyBlock}>
                <div style={styles.dutyHeader} onClick={() => toggleDutyExpand(duty.id)}>
                  <span>Służba #{duty.id}</span>
                  <span>Czas: {Math.floor(duty.workTimeMinutes / 60)}h {duty.workTimeMinutes % 60}m</span>
                  <span>{expandedDutyId === duty.id ? '▲' : '▼'}</span>
                </div>
                
                {expandedDutyId === duty.id && (
                  <div style={styles.dutyDetails}>
                    <h4 style={styles.detailsTitle}>Loty w tej służbie:</h4>
                    {duty.flights && duty.flights.map(flight => (
                      <div key={flight.id} style={styles.flightRow}>
                        <span>{flight.flightNumber}</span>
                        <span>{flight.route}</span>
                      </div>
                    ))}
                    
                    <h4 style={styles.detailsTitle}>Załoga:</h4>
                    {duty.assignedCrew && duty.assignedCrew.map(crew => (
                      <div key={crew.userId} style={styles.crewRow}>
                        <span>{crew.name} {crew.surname}</span>
                        <span style={styles.badge}>{crew.roleOnDuty}</span>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            ))}
          </div>
        )}

        {activeTab === 'unassigned' && (
          <div style={styles.listContainer}>
            <table style={styles.table}>
              <thead>
                <tr>
                  <th>Wybierz</th>
                  <th>Nr Lotu</th>
                  <th>Wylot</th>
                  <th>Przylot</th>
                </tr>
              </thead>
              <tbody>
                {flights.map(flight => (
                  <tr key={flight.id}>
                    <td>
                      <input 
                        type="checkbox" 
                        checked={selectedFlightIds.includes(flight.id)}
                        onChange={() => handleCheckboxToggle(flight.id)}
                        style={styles.checkbox}
                      />
                    </td>
                    <td>{flight.flightNumber}</td>
                    <td>{flight.departureAirport?.airportCode}</td>
                    <td>{flight.arrivalAirport?.airportCode}</td>
                  </tr>
                ))}
              </tbody>
            </table>

            {selectedFlightIds.length > 0 && (
              <div style={styles.actionBar}>
                <span>Wybrano lotów: {selectedFlightIds.length}</span>
                <button style={styles.actionButton} onClick={() => setIsModalOpen(true)}>
                  Utwórz Służbę
                </button>
              </div>
            )}
          </div>
        )}

      </div>

      {isModalOpen && (
        <div style={styles.modalOverlay}>
          <div style={styles.modalContent}>
            <h2 style={styles.modalTitle}>Podsumowanie nowej służby</h2>
            <div style={styles.modalList}>
              {selectedFlightsData.map(f => (
                <div key={f.id} style={styles.flightRow}>
                  <span>{f.flightNumber}</span>
                  <span>{f.departureAirport?.airportCode} - {f.arrivalAirport?.airportCode}</span>
                </div>
              ))}
            </div>
            <div style={styles.modalActions}>
              <button style={styles.cancelButton} onClick={() => setIsModalOpen(false)}>Anuluj</button>
              <button style={styles.confirmButton} onClick={handleCreateDutySubmit}>Potwierdź i Utwórz</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

const styles = {
  page: { minHeight: '100vh', display: 'flex', justifyContent: 'center', alignItems: 'center', backgroundColor: '#f0f2f5', padding: '20px' },
  card: { backgroundColor: 'white', borderRadius: '8px', padding: '30px', width: '100%', maxWidth: '800px', boxShadow: '0 4px 6px rgba(0,0,0,0.1)' },
  header: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid #eee', paddingBottom: '20px', marginBottom: '20px' },
  title: { margin: 0, color: '#333' },
  backLink: { color: '#0066cc', cursor: 'pointer', textDecoration: 'underline' },
  tabsContainer: { display: 'flex', gap: '10px', marginBottom: '20px' },
  tab: { padding: '10px 20px', border: 'none', backgroundColor: '#eee', borderRadius: '4px', cursor: 'pointer' },
  activeTab: { padding: '10px 20px', border: 'none', backgroundColor: '#0066cc', color: 'white', borderRadius: '4px', cursor: 'pointer' },
  listContainer: { display: 'flex', flexDirection: 'column', gap: '10px' },
  dutyBlock: { border: '1px solid #ddd', borderRadius: '6px', overflow: 'hidden' },
  dutyHeader: { display: 'flex', justifyContent: 'space-between', padding: '15px', backgroundColor: '#f9f9f9', cursor: 'pointer', fontWeight: 'bold' },
  dutyDetails: { padding: '15px', borderTop: '1px solid #ddd', backgroundColor: '#fff' },
  detailsTitle: { margin: '10px 0 5px 0', fontSize: '14px', color: '#666' },
  flightRow: { display: 'flex', justifyContent: 'space-between', padding: '8px 0', borderBottom: '1px solid #eee' },
  crewRow: { display: 'flex', justifyContent: 'space-between', padding: '8px 0', alignItems: 'center' },
  badge: { backgroundColor: '#e6f2ff', color: '#0066cc', padding: '4px 8px', borderRadius: '12px', fontSize: '12px' },
  table: { width: '100%', borderCollapse: 'collapse', textAlign: 'left' },
  checkbox: { transform: 'scale(1.2)', cursor: 'pointer' },
  actionBar: { marginTop: '20px', padding: '15px', backgroundColor: '#e6f2ff', borderRadius: '6px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' },
  actionButton: { padding: '10px 20px', backgroundColor: '#0066cc', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' },
  modalOverlay: { position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', justifyContent: 'center', alignItems: 'center' },
  modalContent: { backgroundColor: 'white', padding: '30px', borderRadius: '8px', width: '90%', maxWidth: '500px' },
  modalTitle: { marginTop: 0 },
  modalList: { margin: '20px 0', maxHeight: '300px', overflowY: 'auto' },
  modalActions: { display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '20px' },
  cancelButton: { padding: '10px 20px', backgroundColor: '#eee', border: 'none', borderRadius: '4px', cursor: 'pointer' },
  confirmButton: { padding: '10px 20px', backgroundColor: '#28a745', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }
};

export default Duties;