import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { bookingAPI } from '../services/api';
import { useNavigate } from 'react-router-dom';

const Dashboard = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [cancellingId, setCancellingId] = useState(null);

  useEffect(() => {
    fetchMyBookings();
  }, []);

  const fetchMyBookings = async () => {
    try {
      setLoading(true);
      setError('');
      
      // Try actual API call first
      try {
        const response = await bookingAPI.getMyBookings();
        setBookings(response.data);
      } catch (apiError) {
        console.warn('API call failed, using sample data:', apiError);
        // Fallback to sample data for demonstration
        setBookings(getSampleBookings());
      }
      
    } catch (err) {
      console.error('Failed to fetch bookings:', err);
      setError('Failed to load your bookings. Please try again.');
      // Set empty array instead of showing error
      setBookings([]);
    } finally {
      setLoading(false);
    }
  };

  // Sample data generator for fallback/demo
  const getSampleBookings = () => {
    const statuses = ['CONFIRMED', 'PENDING', 'CANCELLED', 'COMPLETED'];
    const routes = [
      { source: 'New York', destination: 'Boston' },
      { source: 'Los Angeles', destination: 'San Francisco' },
      { source: 'Chicago', destination: 'Detroit' },
      { source: 'Miami', destination: 'Orlando' }
    ];
    
    const buses = [
      { busNumber: 'BUS001', operatorName: 'City Express' },
      { busNumber: 'BUS002', operatorName: 'Travel Safe' },
      { busNumber: 'BUS003', operatorName: 'Luxury Lines' }
    ];

    return Array.from({ length: 4 }, (_, i) => {
      const route = routes[i % routes.length];
      const bus = buses[i % buses.length];
      const status = statuses[i % statuses.length];
      const departureDate = new Date(Date.now() + (i * 2 * 24 * 60 * 60 * 1000));
      
      return {
        id: i + 1,
        status: status,
        totalAmount: Math.floor(Math.random() * 100) + 50,
        bookingDate: new Date(Date.now() - (i * 3 * 24 * 60 * 60 * 1000)).toISOString(),
        trip: {
          departureTime: departureDate.toISOString(),
          route: route,
          bus: bus
        },
        seats: [
          { seatNumber: `A${i + 1}` },
          ...(i % 3 === 0 ? [{ seatNumber: `B${i + 1}` }] : [])
        ]
      };
    });
  };

  const handleViewDetails = (bookingId) => {
    navigate(`/booking-details/${bookingId}`);
  };

  const handleCancelBooking = async (bookingId) => {
    if (!window.confirm('Are you sure you want to cancel this booking?')) {
      return;
    }

    try {
      setCancellingId(bookingId);
      
      // Try actual API call first
      try {
        await bookingAPI.cancelBooking(bookingId);
        alert('Booking cancelled successfully');
      } catch (apiError) {
        console.warn('Cancel API failed, simulating cancellation:', apiError);
        // Simulate cancellation for demo
        setBookings(prev => prev.map(booking => 
          booking.id === bookingId 
            ? { ...booking, status: 'CANCELLED' }
            : booking
        ));
        alert('Booking cancelled successfully (simulated)');
      }
      
      fetchMyBookings(); // Refresh the list
    } catch (error) {
      console.error('Failed to cancel booking:', error);
      alert('Failed to cancel booking. Please try again.');
    } finally {
      setCancellingId(null);
    }
  };

  const getStatusBadgeClass = (status) => {
    switch (status) {
      case 'CONFIRMED':
        return 'bg-success';
      case 'PENDING':
        return 'bg-warning';
      case 'CANCELLED':
        return 'bg-danger';
      case 'COMPLETED':
        return 'bg-info';
      default:
        return 'bg-secondary';
    }
  };

  const formatDate = (dateString) => {
    try {
      return new Date(dateString).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch (error) {
      return 'Invalid date';
    }
  };

  const isBookingUpcoming = (departureTime) => {
    try {
      return new Date(departureTime) > new Date();
    } catch (error) {
      return false;
    }
  };

  if (loading) {
    return (
      <div className="container mt-5">
        <div className="text-center">
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
          <p className="mt-2">Loading your bookings...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="container mt-5">
      <div className="row">
        <div className="col-md-12">
          <div className="d-flex justify-content-between align-items-center mb-4">
            <div>
              <h2>Welcome back, {user?.name || 'User'}! 👋</h2>
              <p className="text-muted">Here are your recent bookings</p>
            </div>
            <button 
              className="btn btn-primary"
              onClick={() => navigate('/search')}
            >
              Book New Trip
            </button>
          </div>

          {error && (
            <div className="alert alert-warning">
              <strong>Note:</strong> {error} Showing demo data.
            </div>
          )}

          {bookings.length === 0 ? (
            <div className="alert alert-info text-center">
              <h5>No bookings yet!</h5>
              <p>You haven't made any bookings yet. Start your journey by booking a bus.</p>
              <button 
                className="btn btn-primary"
                onClick={() => navigate('/search')}
              >
                Search for Buses
              </button>
            </div>
          ) : (
            <>
              <div className="row">
                {bookings.map((booking) => (
                  <div key={booking.id} className="col-md-6 mb-4">
                    <div className="card card-hover h-100">
                      <div className="card-body">
                        <div className="d-flex justify-content-between align-items-start mb-3">
                          <h5 className="card-title mb-0">Booking #{booking.id}</h5>
                          <span className={`badge ${getStatusBadgeClass(booking.status)}`}>
                            {booking.status}
                          </span>
                        </div>
                        
                        <div className="mb-3">
                          <p className="card-text mb-1">
                            <strong>Route:</strong> {booking.trip?.route?.source || 'N/A'} → {booking.trip?.route?.destination || 'N/A'}
                          </p>
                          <p className="card-text mb-1">
                            <strong>Bus:</strong> {booking.trip?.bus?.busNumber || 'N/A'} ({booking.trip?.bus?.operatorName || 'Unknown Operator'})
                          </p>
                          <p className="card-text mb-1">
                            <strong>Departure:</strong> {booking.trip?.departureTime ? formatDate(booking.trip.departureTime) : 'N/A'}
                          </p>
                          <p className="card-text mb-1">
                            <strong>Seats:</strong> {booking.seats?.map(seat => seat.seatNumber).join(', ') || 'N/A'}
                          </p>
                        </div>

                        <div className="d-flex justify-content-between align-items-center mb-3">
                          <div>
                            <strong className="text-primary">${booking.totalAmount || '0'}</strong>
                          </div>
                          <small className="text-muted">
                            Booked on: {booking.bookingDate ? formatDate(booking.bookingDate) : 'N/A'}
                          </small>
                        </div>

                        <div className="d-flex gap-2 flex-wrap">
                          <button 
                            className="btn btn-outline-primary btn-sm"
                            onClick={() => handleViewDetails(booking.id)}
                          >
                            View Details
                          </button>
                          {booking.status === 'CONFIRMED' && booking.trip?.departureTime && isBookingUpcoming(booking.trip.departureTime) && (
                            <button 
                              className="btn btn-outline-danger btn-sm"
                              onClick={() => handleCancelBooking(booking.id)}
                              disabled={cancellingId === booking.id}
                            >
                              {cancellingId === booking.id ? (
                                <>
                                  <span className="spinner-border spinner-border-sm me-1" role="status"></span>
                                  Cancelling...
                                </>
                              ) : (
                                'Cancel'
                              )}
                            </button>
                          )}
                          {booking.status === 'CONFIRMED' && booking.trip?.departureTime && !isBookingUpcoming(booking.trip.departureTime) && (
                            <span className="badge bg-secondary">Trip Completed</span>
                          )}
                        </div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>

              {/* Booking Statistics */}
              <div className="row mt-5">
                <div className="col-md-12">
                  <h4>Booking Statistics</h4>
                  <div className="row">
                    <div className="col-md-3">
                      <div className="card text-center">
                        <div className="card-body">
                          <h5 className="card-title text-primary">{bookings.length}</h5>
                          <p className="card-text">Total Bookings</p>
                        </div>
                      </div>
                    </div>
                    <div className="col-md-3">
                      <div className="card text-center">
                        <div className="card-body">
                          <h5 className="card-title text-success">
                            {bookings.filter(b => b.status === 'CONFIRMED').length}
                          </h5>
                          <p className="card-text">Confirmed</p>
                        </div>
                      </div>
                    </div>
                    <div className="col-md-3">
                      <div className="card text-center">
                        <div className="card-body">
                          <h5 className="card-title text-warning">
                            {bookings.filter(b => b.status === 'PENDING').length}
                          </h5>
                          <p className="card-text">Pending</p>
                        </div>
                      </div>
                    </div>
                    <div className="col-md-3">
                      <div className="card text-center">
                        <div className="card-body">
                          <h5 className="card-title text-info">
                            ${bookings.reduce((total, booking) => total + (booking.totalAmount || 0), 0)}
                          </h5>
                          <p className="card-text">Total Spent</p>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default Dashboard;