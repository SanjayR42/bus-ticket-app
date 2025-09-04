import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { Navigate, useLocation } from 'react-router-dom';

const PrivateRoute = ({ children, requireAdmin = false }) => {
  const { isAuthenticated, isAdmin, validateToken, token, loading } = useAuth();
  const [isValidating, setIsValidating] = useState(true);
  const location = useLocation();
  
  useEffect(() => {
    const checkTokenValidity = async () => {
      if (token) {
        try {
          const isValid = await validateToken(token);
          if (!isValid) {
            console.warn('Token is invalid, redirecting to login');
          }
        } catch (error) {
          console.error('Token validation error:', error);
        }
      }
      setIsValidating(false);
    };

    checkTokenValidity();
  }, [token, validateToken]);

  if (loading || isValidating) {
    return (
      <div className="container mt-5">
        <div className="text-center">
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
          <p className="mt-2">Verifying authentication...</p>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (requireAdmin && !isAdmin) {
    return <Navigate to="/dashboard" replace />;
  }

  
  return children;
};

export default PrivateRoute;