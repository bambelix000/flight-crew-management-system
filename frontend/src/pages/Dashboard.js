import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

function Dashboard() {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [activeTab, setActiveTab] = useState(null);
  const [flights, setFlights] = useState([]);
  const [duties, setDuties] = useState([]);
  const [selectedFlights, setSelectedFlights] = useState([]);
  const [expandedDutyId, setExpandedDutyId] = useState(null);
  
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [refreshKey, setRefreshKey] = useState(0);

  const [isAssignModalOpen, setIsAssignModalOpen] = useState(false);
  const [assignDutyId, setAssignDutyId] = useState(null);
  const [usersList, setUsersList] = useState([]);
  const [assignUserId, setAssignUserId] = useState('');
  const [assignRole, setAssignRole] = useState('CABIN_CREW');
  const [assignError, setAssignError] = useState('');

  useEffect(() => {
    const token = localStorage.getItem('token');
    const savedData = localStorage.getItem('user'); 
    
    if (!token || !savedData) {
      handleLogout();
      return;
    }
    
    const parsedUser = JSON.parse(savedData);
    
    if (!activeTab) {
      const isScheduler = parsedUser.userRole === 'SCHEDULER' || parsedUser.userRole === 'ADMIN';
      setActiveTab(isScheduler ? 'unassigned' : 'myDuties');
    }

    fetch(`http://localhost:8080/users/my-stats`, {
      method: 'GET',
      headers: { 'Authorization': `Bearer ${token}` }
    })
      .then(res => {
        if (!res.ok) throw new Error();
        return res.json();
      })
      .then(statsData => {
        setUser({ ...parsedUser, ...statsData });
      })
      .catch(() => {});
  }, [navigate, activeTab]);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token || !activeTab || !user) return;

    const isScheduler = user.userRole === 'SCHEDULER' || user.userRole === 'ADMIN';

    const fetchTabData = async () => {
      try {
        if (isScheduler) {
          const [resFlights, resDuties] = await Promise.all([
            // Zmiana na nowy endpoint get
            fetch('http://localhost:8080/flights/get', { headers: { 'Authorization': `Bearer ${token}` } }),
            fetch('http://localhost:8080/duties', { headers: { 'Authorization': `Bearer ${token}` } })
          ]);
          if (resFlights.ok) setFlights(await resFlights.json());
          if (resDuties.ok) setDuties(await resDuties.json());
        } else {
          const res = await fetch('http://localhost:8080/duties/my-duties', { headers: { 'Authorization': `Bearer ${token}` } });
          if (res.ok) setDuties(await res.json());
        }
      } catch (err) {}
    };

    fetchTabData();
  }, [activeTab, user, refreshKey]);

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

  const toggleDutyExpand = (id) => {
    setExpandedDutyId(expandedDutyId === id ? null : id);
  };

  const handleCreateDutySubmit = async () => {
    const token = localStorage.getItem('token');
    try {
      const res = await fetch('http://localhost:8080/duties/create-from-flights', {
        method: 'POST',
        headers: { 
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}` 
        },
        body: JSON.stringify(selectedFlights)
      });
      if (res.ok) {
        setIsModalOpen(false);
        setSelectedFlights([]);
        setActiveTab('createdDuties');
        setRefreshKey(prev => prev + 1);
      } else {
        alert("Błąd podczas tworzenia służby");
      }
    } catch (err) {}
  };

  const openAssignModal = async (dutyId) => {
    const token = localStorage.getItem('token');
    try {
      const res = await fetch('http://localhost:8080/users', {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (res.ok) {
        const allUsers = await res.json();
        
        const currentDuty = duties.find(d => d.id === dutyId);
        const assignedIds = currentDuty?.assignedCrew?.map(crew => crew.userId) || [];
        
        const availableCrew = allUsers.filter(u => 
          u.userRole === 'CREWMEMBER' && !assignedIds.includes(u.id)
        );

        setUsersList(availableCrew);
        setAssignDutyId(dutyId);
        setAssignUserId('');
        setAssignRole('CABIN_CREW');
        setAssignError('');
        setIsAssignModalOpen(true);
      }
    } catch (err) {
      alert("Nie udało się pobrać listy użytkowników.");
    }
  };

  const handleAssignSubmit = async () => {
    const token = localStorage.getItem('token');
    try {
      const res = await fetch(`http://localhost:8080/duties/${assignDutyId}/assign`, {
        method: 'POST',
        headers: { 
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}` 
        },
        body: JSON.stringify({
          userId: parseInt(assignUserId),
          role: assignRole
        })
      });
      
      if (res.ok) {
        setIsAssignModalOpen(false);
        setRefreshKey(prev => prev + 1);
      } else {
        // Wszechstronne wyciąganie treści błędu
        const errorText = await res.text();
        let finalErrorMsg = "Nie udało się przypisać użytkownika. Sprawdź logi na serwerze.";
        
        if (errorText) {
          try {
            // Próba odczytania JSON (np. domyślny format błędu Springa)
            const errorJson = JSON.parse(errorText);
            finalErrorMsg = errorJson.message || errorText;
          } catch (e) {
            // Jeśli to nie JSON, wyświetl surowy tekst zwrócony przez Springa
            finalErrorMsg = errorText;
          }
        }
        setAssignError(finalErrorMsg);
      }
    } catch (err) {
      setAssignError("Wystąpił błąd sieci podczas komunikacji z serwerem.");
    }
  };

  if (!user) return null;

  const isScheduler = user.userRole === 'SCHEDULER' || user.userRole === 'ADMIN';
  
  const assignedFlightIds = duties.flatMap(d => (d.flights || []).map(f => f.id));
  const unassignedFlights = flights.filter(f => !assignedFlightIds.includes(f.id));
  
  const targetFlightsList = activeTab === 'unassigned' ? unassignedFlights : flights;
  const selectedFlightsData = flights.filter(f => selectedFlights.includes(f.id));

  let previewStartTime = '';
  let previewEndTime = '';
  
  if (isModalOpen && selectedFlightsData.length > 0) {
    const sorted = [...selectedFlightsData].sort((a, b) => new Date(a.departureTime) - new Date(b.departureTime));
    const firstFlight = sorted[0];
    const lastFlight = sorted[sorted.length - 1];

    const startObj = new Date(firstFlight.departureTime);
    startObj.setHours(startObj.getHours() - 1);
    previewStartTime = startObj.toLocaleString('pl-PL');

    const endObj = new Date(lastFlight.arrivalTime);
    previewEndTime = endObj.toLocaleString('pl-PL');
  }

  const styles = {
    container: { padding: '40px', fontFamily: '"Inter", sans-serif', backgroundColor: '#f8f9fa', minHeight: '100vh' },
    card: { backgroundColor: '#fff', padding: '24px', borderRadius: '12px', boxShadow: '0 2px 12px rgba(0,0,0,0.04)', marginBottom: '20px' },
    table: { width: '100%', borderCollapse: 'collapse', marginTop: '10px' },
    th: { textAlign: 'left', padding: '12px', borderBottom: '2px solid #edf2f7', color: '#718096', fontSize: '13px', textTransform: 'uppercase' },
    td: { padding: '12px', borderBottom: '1px solid #edf2f7', color: '#2d3748', fontSize: '14px' },
    status: { padding: '4px 8px', borderRadius: '6px', fontSize: '12px', fontWeight: '600', backgroundColor: '#e3f9e5', color: '#1f7a28' },
    tabsContainer: { display: 'flex', gap: '10px', marginBottom: '20px', borderBottom: '2px solid #edf2f7', paddingBottom: '10px' },
    tab: { padding: '10px 20px', border: 'none', backgroundColor: 'transparent', color: '#718096', cursor: 'pointer', fontSize: '16px', fontWeight: '600' },
    activeTab: { padding: '10px 20px', border: 'none', backgroundColor: '#ebf4ff', color: '#3182ce', borderRadius: '8px', cursor: 'pointer', fontSize: '16px', fontWeight: '700' },
    dutyBlock: { border: '1px solid #edf2f7', borderRadius: '8px', overflow: 'hidden', marginBottom: '10px' },
    dutyHeader: { display: 'flex', justifyContent: 'space-between', padding: '16px', backgroundColor: '#f8f9fa', cursor: 'pointer', fontWeight: '600', color: '#2d3748' },
    dutyDetails: { padding: '16px', borderTop: '1px solid #edf2f7', backgroundColor: '#fff' },
    detailsTitle: { margin: 0, fontSize: '14px', color: '#718096', textTransform: 'uppercase' },
    innerRow: { display: 'flex', justifyContent: 'space-between', padding: '8px 0', borderBottom: '1px solid #edf2f7', fontSize: '14px' },
    badge: { backgroundColor: '#ebf4ff', color: '#3182ce', padding: '4px 8px', borderRadius: '6px', fontSize: '12px', fontWeight: '600' },
    modalOverlay: { position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.6)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000 },
    modalContent: { backgroundColor: 'white', padding: '32px', borderRadius: '12px', width: '90%', maxWidth: '500px', boxShadow: '0 10px 25px rgba(0,0,0,0.1)' },
    selectInput: { width: '100%', padding: '10px', borderRadius: '8px', border: '1px solid #e2e8f0', fontSize: '14px', marginTop: '8px', backgroundColor: '#fff' },
    assignButton: { padding: '6px 12px', fontSize: '12px', backgroundColor: '#fff', color: '#3182ce', border: '1px solid #3182ce', borderRadius: '6px', cursor: 'pointer', fontWeight: '600' },
    errorMessage: { padding: '12px', backgroundColor: '#fed7d7', color: '#c53030', borderRadius: '8px', marginBottom: '16px', fontSize: '14px', fontWeight: '500' }
  };

  return (
    <div style={styles.container}>
      <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '32px' }}>
        <h1 style={{ fontSize: '24px', color: '#1a1f36' }}>Panel Zarządzania</h1>
        <div>
          {user.userRole === 'ADMIN' && (
            <button onClick={() => navigate('/userList')} style={{ padding: '8px 16px', borderRadius: '8px', border: 'none', cursor: 'pointer', background: '#fed7d7', color: '#9b2c2c', fontWeight: '600', marginRight: '10px' }}>Użytkownicy</button>
          )}
          <button onClick={() => navigate('/myProfile')} style={{ padding: '8px 16px', borderRadius: '8px', border: 'none', cursor: 'pointer', background: '#5469d4', color: '#fff', fontWeight: '600', marginRight: '10px' }}>Mój Profil</button>
          <button onClick={handleLogout} style={{ padding: '8px 16px', borderRadius: '8px', border: '1px solid #dcdfe4', cursor: 'pointer', background: '#fff' }}>Wyloguj</button>
        </div>
      </header>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 300px', gap: '24px' }}>
        
        <section style={styles.card}>
          
          <div style={styles.tabsContainer}>
            {isScheduler ? (
              <>
                <button style={activeTab === 'unassigned' ? styles.activeTab : styles.tab} onClick={() => setActiveTab('unassigned')}>
                  Nieprzypisane Loty
                </button>
                <button style={activeTab === 'allFlights' ? styles.activeTab : styles.tab} onClick={() => setActiveTab('allFlights')}>
                  Wszystkie Loty
                </button>
                <button style={activeTab === 'createdDuties' ? styles.activeTab : styles.tab} onClick={() => setActiveTab('createdDuties')}>
                  Utworzone Służby
                </button>
              </>
            ) : (
              <button style={styles.activeTab}>Moje Służby</button>
            )}
          </div>

          {(activeTab === 'unassigned' || activeTab === 'allFlights') && (
            <>
              <table style={styles.table}>
                <thead>
                  <tr>
                    {activeTab === 'unassigned' && <th style={styles.th}>Wybierz</th>}
                    <th style={styles.th}>Nr Lotu</th>
                    <th style={styles.th}>Trasa</th>
                    <th style={styles.th}>Wylot</th>
                    <th style={styles.th}>Przylot</th>
                    <th style={styles.th}>Czas</th>
                  </tr>
                </thead>
                <tbody>
                  {targetFlightsList.map(f => (
                    <tr key={f.id}>
                      {activeTab === 'unassigned' && (
                        <td style={styles.td}>
                          <input type="checkbox" style={{ transform: 'scale(1.2)' }} onChange={() => toggleFlightSelection(f.id)} checked={selectedFlights.includes(f.id)} />
                        </td>
                      )}
                      <td style={styles.td}><strong>{f.flightNumber}</strong></td>
                      <td style={styles.td}>{f.departureAirport?.airportCode} → {f.arrivalAirport?.airportCode}</td>
                      <td style={styles.td}>{new Date(f.departureTime).toLocaleString('pl-PL')}</td>
                      <td style={styles.td}>{new Date(f.arrivalTime).toLocaleString('pl-PL')}</td>
                      <td style={styles.td}>{f.durationMinutes} min</td>
                    </tr>
                  ))}
                </tbody>
              </table>

              {activeTab === 'unassigned' && selectedFlights.length > 0 && (
                <div style={{ marginTop: '20px', padding: '16px', backgroundColor: '#ebf4ff', borderRadius: '8px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span style={{ color: '#2b6cb0', fontWeight: '600' }}>Wybrano lotów: {selectedFlights.length}</span>
                  <button onClick={() => setIsModalOpen(true)} style={{ padding: '12px 24px', backgroundColor: '#3182ce', color: '#fff', border: 'none', borderRadius: '8px', cursor: 'pointer', fontWeight: '600' }}>
                    Utwórz Służbę
                  </button>
                </div>
              )}
            </>
          )}

          {(activeTab === 'createdDuties' || activeTab === 'myDuties') && (
            <div>
              {duties.length === 0 ? (
                <p style={{ color: '#718096', padding: '20px 0' }}>Brak przypisanych służb.</p>
              ) : (
                duties.map(duty => (
                  <div key={duty.id} style={styles.dutyBlock}>
                    <div style={styles.dutyHeader} onClick={() => toggleDutyExpand(duty.id)}>
                      <span>Służba #{duty.id}</span>
                      <span>Czas pracy: {Math.floor(duty.workTimeMinutes / 60)}h {duty.workTimeMinutes % 60}m</span>
                      <span>{expandedDutyId === duty.id ? '▲ Zwiń' : '▼ Rozwiń'}</span>
                    </div>
                    
                    {expandedDutyId === duty.id && (
                      <div style={styles.dutyDetails}>
                        <div style={{ backgroundColor: '#ebf4ff', padding: '16px', borderRadius: '8px', marginBottom: '20px', border: '1px solid #bee3f8' }}>
                          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
                            <span style={{ color: '#2b6cb0', fontWeight: '600', fontSize: '14px' }}>Rozpoczęcie (Check-in):</span>
                            <span style={{ fontWeight: '700', color: '#2c5282' }}>{duty.dutyStartTime ? new Date(duty.dutyStartTime).toLocaleString('pl-PL') : 'Brak'}</span>
                          </div>
                          <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                            <span style={{ color: '#2b6cb0', fontWeight: '600', fontSize: '14px' }}>Zakończenie (Check-out):</span>
                            <span style={{ fontWeight: '700', color: '#2c5282' }}>{duty.dutyEndTime ? new Date(duty.dutyEndTime).toLocaleString('pl-PL') : 'Brak'}</span>
                          </div>
                        </div>

                        <h4 style={{ ...styles.detailsTitle, marginBottom: '10px' }}>Loty w tej służbie:</h4>
                        {duty.flights && duty.flights.map(flight => {
                          const fullFlight = flights.find(f => f.id === flight.id);
                          const depTime = fullFlight ? new Date(fullFlight.departureTime).toLocaleString('pl-PL') : '';
                          const arrTime = fullFlight ? new Date(fullFlight.arrivalTime).toLocaleString('pl-PL') : '';

                          return (
                            <div key={flight.id} style={{ ...styles.innerRow, flexDirection: 'column', alignItems: 'flex-start' }}>
                              <div style={{ display: 'flex', justifyContent: 'space-between', width: '100%', marginBottom: '4px' }}>
                                <strong>{flight.flightNumber}</strong>
                                <span>{flight.route}</span>
                              </div>
                              {fullFlight && (
                                <div style={{ display: 'flex', justifyContent: 'space-between', width: '100%', fontSize: '12px', color: '#718096' }}>
                                  <span>Wylot: {depTime}</span>
                                  <span>Przylot: {arrTime}</span>
                                </div>
                              )}
                            </div>
                          );
                        })}
                        
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '24px', marginBottom: '10px' }}>
                          <h4 style={styles.detailsTitle}>Przypisana Załoga:</h4>
                          {isScheduler && (
                            <button onClick={() => openAssignModal(duty.id)} style={styles.assignButton}>
                              + Przypisz pracownika
                            </button>
                          )}
                        </div>

                        {(!duty.assignedCrew || duty.assignedCrew.length === 0) ? (
                          <p style={{ fontSize: '14px', color: '#a0aec0', margin: '5px 0' }}>Brak załogi</p>
                        ) : (
                          duty.assignedCrew.map(crew => (
                            <div key={crew.userId} style={styles.innerRow}>
                              <span>{crew.name} {crew.surname}</span>
                              <span style={styles.badge}>{crew.roleOnDuty}</span>
                            </div>
                          ))
                        )}
                      </div>
                    )}
                  </div>
                ))
              )}
            </div>
          )}
        </section>

        <aside>
          <div style={styles.card}>
            <p style={{ fontSize: '14px', color: '#718096', marginBottom: '4px' }}>Zalogowany jako:</p>
            <p style={{ fontWeight: '700', fontSize: '18px', marginBottom: '8px' }}>{user.login}</p>
            <span style={{ ...styles.status, backgroundColor: '#ebf4ff', color: '#3182ce' }}>{user.userRole}</span>
          </div>

          <div style={styles.card}>
            <h3 style={{ fontSize: '16px', marginBottom: '16px' }}>Czas lotu (FTL)</h3>
            <div style={{ marginBottom: '12px' }}>
              <p style={{ fontSize: '12px', color: '#718096' }}>Ostatnie 20 dni</p>
              <p style={{ fontWeight: '700' }}>{Math.floor((user.twentyDaysAirTime || 0) / 60)}h / 90h</p>
            </div>
            <div>
              <p style={{ fontSize: '12px', color: '#718096' }}>Rok kalendarzowy</p>
              <p style={{ fontWeight: '700' }}>{Math.floor((user.annualAirTime || 0) / 60)}h / 900h</p>
            </div>
          </div>
        </aside>
      </div>

      {isModalOpen && (
        <div style={styles.modalOverlay}>
          <div style={styles.modalContent}>
            <h2 style={{ marginTop: 0, color: '#1a1f36', fontSize: '20px' }}>Podsumowanie nowej służby</h2>
            
            <div style={{ backgroundColor: '#ebf4ff', padding: '16px', borderRadius: '8px', marginBottom: '20px', border: '1px solid #bee3f8' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
                <span style={{ color: '#2b6cb0', fontWeight: '600', fontSize: '14px' }}>Rozpoczęcie (Check-in):</span>
                <span style={{ fontWeight: '700', color: '#2c5282' }}>{previewStartTime}</span>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ color: '#2b6cb0', fontWeight: '600', fontSize: '14px' }}>Zakończenie (Check-out):</span>
                <span style={{ fontWeight: '700', color: '#2c5282' }}>{previewEndTime}</span>
              </div>
            </div>

            <p style={{ color: '#718096', fontSize: '14px', marginBottom: '12px', fontWeight: '600', textTransform: 'uppercase' }}>Lista lotów:</p>
            
            <div style={{ maxHeight: '200px', overflowY: 'auto', marginBottom: '24px', paddingRight: '5px' }}>
              {selectedFlightsData.map(f => (
                <div key={f.id} style={{ display: 'flex', flexDirection: 'column', padding: '10px 0', borderBottom: '1px solid #edf2f7' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <strong>{f.flightNumber}</strong>
                    <span>{f.departureAirport?.airportCode} → {f.arrivalAirport?.airportCode}</span>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', color: '#718096', marginTop: '4px' }}>
                    <span>Wylot: {new Date(f.departureTime).toLocaleString('pl-PL')}</span>
                    <span>Przylot: {new Date(f.arrivalTime).toLocaleString('pl-PL')}</span>
                  </div>
                </div>
              ))}
            </div>
            
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px' }}>
              <button onClick={() => setIsModalOpen(false)} style={{ padding: '10px 20px', backgroundColor: '#edf2f7', color: '#4a5568', border: 'none', borderRadius: '8px', cursor: 'pointer', fontWeight: '600' }}>Anuluj</button>
              <button onClick={handleCreateDutySubmit} style={{ padding: '10px 20px', backgroundColor: '#38a169', color: '#fff', border: 'none', borderRadius: '8px', cursor: 'pointer', fontWeight: '600' }}>Zatwierdź służbę</button>
            </div>
          </div>
        </div>
      )}

      {isAssignModalOpen && (
        <div style={styles.modalOverlay}>
          <div style={styles.modalContent}>
            <h2 style={{ marginTop: 0, color: '#1a1f36', fontSize: '20px', marginBottom: '24px' }}>Przypisz do służby #{assignDutyId}</h2>
            
            {assignError && (
              <div style={styles.errorMessage}>
                {assignError}
              </div>
            )}

            <div style={{ marginBottom: '16px' }}>
              <label style={{ display: 'block', color: '#4a5568', fontSize: '14px', fontWeight: '600' }}>Wybierz pracownika:</label>
              <select 
                value={assignUserId} 
                onChange={(e) => setAssignUserId(e.target.value)}
                style={styles.selectInput}
              >
                <option value="" disabled>-- Wybierz pracownika --</option>
                {usersList.length === 0 && (
                  <option value="" disabled>Brak dostępnych pracowników</option>
                )}
                {usersList.map(u => (
                  <option key={u.id} value={u.id}>
                    {u.name} {u.surname}
                  </option>
                ))}
              </select>
            </div>

            <div style={{ marginBottom: '32px' }}>
              <label style={{ display: 'block', color: '#4a5568', fontSize: '14px', fontWeight: '600' }}>Wybierz rolę na służbie:</label>
              <select 
                value={assignRole} 
                onChange={(e) => setAssignRole(e.target.value)}
                style={styles.selectInput}
              >
                <option value="CAPTAIN">Kapitan (CAPTAIN)</option>
                <option value="FIRST_OFFICER">Pierwszy Oficer (FIRST_OFFICER)</option>
                <option value="CABIN_CREW">Personel Pokładowy (CABIN_CREW)</option>
                <option value="INSTRUCTOR">Instruktor (INSTRUCTOR)</option>
              </select>
            </div>
            
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px' }}>
              <button onClick={() => setIsAssignModalOpen(false)} style={{ padding: '10px 20px', backgroundColor: '#edf2f7', color: '#4a5568', border: 'none', borderRadius: '8px', cursor: 'pointer', fontWeight: '600' }}>Anuluj</button>
              <button 
                onClick={handleAssignSubmit} 
                disabled={!assignUserId} 
                style={{ padding: '10px 20px', backgroundColor: '#3182ce', color: '#fff', border: 'none', borderRadius: '8px', cursor: 'pointer', fontWeight: '600', opacity: assignUserId ? 1 : 0.5 }}
              >
                Przypisz
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default Dashboard;