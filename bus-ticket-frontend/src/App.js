import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Navbar from './components/Navbar';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import Dasboard from './pages/Dasboard';
import BusSearch from './pages/BusSearch';
import AdminPanel from './pages/AdminPanel';
import Booking from './pages/Booking';
import BookingDetails from './pages/BookingDetails';
import PrivateRoute from './components/PrivateRoute';

function App() {
  return (
    <AuthProvider>
      <Router>
        <div className="App">
          <Navbar />
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/search" element={<BusSearch />} />
            
            <Route path="/dashboard" element={
              <PrivateRoute>
                <Dasboard />
              </PrivateRoute>
            } />
            
            <Route path="/admin" element={
              <PrivateRoute requireAdmin={true}>
                <AdminPanel />
              </PrivateRoute>
            } />

            <Route path="/booking/:tripId" element={
              <PrivateRoute>
                <Booking />
              </PrivateRoute>
            } />

            <Route path="/booking-details/:bookingId" element={
              <PrivateRoute>
                <BookingDetails />
              </PrivateRoute>
            } />
          </Routes>
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;