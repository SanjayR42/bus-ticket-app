// components/APITest.js
import React, { useState } from 'react';
import api from '../services/api';

const APITest = () => {
  const [result, setResult] = useState('');
  const [loading, setLoading] = useState(false);

  const testPublicEndpoint = async () => {
    setLoading(true);
    try {
      const response = await api.get('/buses');
      setResult(`✅ Success: ${response.status} - ${response.data.length} buses found`);
    } catch (error) {
      setResult(`❌ Error: ${error.response?.status || 'No response'} - ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  const testAuthEndpoint = async () => {
    setLoading(true);
    try {
      const response = await api.get('/bookings/me');
      setResult(`✅ Authenticated: ${response.status} - ${response.data.length} bookings found`);
    } catch (error) {
      setResult(`❌ Auth Error: ${error.response?.status || 'No response'} - ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container mt-4">
      <h4>API Connection Test</h4>
      <div className="d-flex gap-2 mb-3">
        <button className="btn btn-primary" onClick={testPublicEndpoint} disabled={loading}>
          Test Public API
        </button>
        <button className="btn btn-secondary" onClick={testAuthEndpoint} disabled={loading}>
          Test Auth API
        </button>
      </div>
      {loading && <div className="spinner-border text-primary"></div>}
      <div className={`alert ${result.includes('✅') ? 'alert-success' : 'alert-danger'} mt-3`}>
        {result || 'Click a button to test API connection'}
      </div>
    </div>
  );
};

export default APITest;