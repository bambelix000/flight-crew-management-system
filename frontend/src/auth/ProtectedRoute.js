import React, { useEffect } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { getToken, logout } from '../api/auth';

function ProtectedRoute({ children }) {
  const navigate = useNavigate();

  useEffect(() => {
    const onExpired = () => {
      logout();
      navigate('/', { replace: true });
    };
    window.addEventListener('auth:expired', onExpired);
    return () => window.removeEventListener('auth:expired', onExpired);
  }, [navigate]);

  if (!getToken()) {
    return <Navigate to="/" replace />;
  }
  return children;
}

export default ProtectedRoute;
