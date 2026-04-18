import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Auth from './pages/Auth';
import Dashboard from './pages/Dashboard';
import MyProfile from './pages/myProfile';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Auth />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/myProfile" element={<MyProfile />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;