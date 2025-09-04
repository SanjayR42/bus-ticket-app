import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { bookingAPI, paymentAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';

const BookingDetails = () => {
  const { bookingId } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [booking, setBooking] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchBookingDetails = async () => {
    try {
      setLoading(true);
      const response = await bookingAPI.getBooking(bookingId);
      setBooking(response.data);
    } catch (error) {
      console.error('Error fetching booking details:', error);
      setError('Failed to load booking details');
    } finally {
      setLoading(false);
    }
  };

  fetchBookingDetails();
}, [bookingId]);

  const handleDownloadTicket = async () => {
    try {
      // This would typically download a PDF
      alert('Ticket download functionality would be implemented here');
    } catch (error) {
      console.error('Error downloading ticket:', error);
      alert('Failed to download ticket');
    }
  };

  const handleCancelBooking = async () => {
    if (!window.confirm('Are you sure you want to cancel this booking?')) {
      return;
    }

    try {
      await bookingAPI.cancelBooking(bookingId);
      alert('Booking cancelled successfully');
      navigate('/dashboard');
    } catch (error) {
      console.error('Error cancelling booking:', error);
      setError('Failed to cancel booking');
    }
  };

  if (loading) {
    return (
      <div className="container mt-5">
        <div className="text-center">
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
          <p className="mt-2">Loading booking details...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container mt-5">
        <div className="alert alert-danger">{error}</div>
        <button className="btn btn-primary" onClick={() => navigate('/dashboard')}>
          Back to Dashboard
        </button>
      </div>
    );
  }

  if (!booking) {
    return (
      <div className="container mt-5">
        <div className="alert alert-danger">Booking not found</div>
        <button className="btn btn-primary" onClick={() => navigate('/dashboard')}>
          Back to Dashboard
        </button>
      </div>
    );
  }

  return (
    <div className="container mt-4">
      <div className="row">
        <div className="col-md-12">
          <div className="d-flex justify-content-between align-items-center mb-4">
            <h2>Booking Details #{booking.id}</h2>
            <button className="btn btn-outline-secondary" onClick={() => navigate('/dashboard')}>
              Back to Dashboard
            </button>
          </div>

          <div className="row">
            <div className="col-md-8">
              <div className="card mb-4">
                <div className="card-header">
                  <h5>Trip Information</h5>
                </div>
                <div className="card-body">
                  <div className="row">
                    <div className="col-md-6">
                      <p><strong>Route:</strong> {booking.trip?.route?.source} → {booking.trip?.route?.destination}</p>
                      <p><strong>Departure:</strong> {new Date(booking.trip?.departureTime).toLocaleString()}</p>
                      <p><strong>Arrival:</strong> {new Date(booking.trip?.arrivalTime).toLocaleString()}</p>
                    </div>
                    <div className="col-md-6">
                      <p><strong>Bus:</strong> {booking.trip?.bus?.busNumber} ({booking.trip?.bus?.busType})</p>
                      <p><strong>Operator:</strong> {booking.trip?.bus?.operatorName}</p>
                      <p><strong>Seats:</strong> {booking.seats?.map(seat => seat.seatNumber).join(', ')}</p>
                    </div>
                  </div>
                </div>
              </div>

              <div className="card mb-4">
                <div className="card-header">
                  <h5>Passenger Information</h5>
                </div>
                <div className="card-body">
                  <p><strong>Name:</strong> {user?.name}</p>
                  <p><strong>Email:</strong> {user?.email}</p>
                  <p><strong>Phone:</strong> {user?.phone}</p>
                </div>
              </div>
            </div>

            <div className="col-md-4">
              <div className="card mb-4">
                <div className="card-header">
                  <h5>Payment Details</h5>
                </div>
                <div className="card-body">
                  <p><strong>Total Amount:</strong> ${booking.totalAmount}</p>
                  <p><strong>Status:</strong> 
                    <span className={`badge ${booking.status === 'CONFIRMED' ? 'bg-success' : 'bg-warning'}`}>
                      {booking.status}
                    </span>
                  </p>
                  {booking.payment && (
                    <>
                      <p><strong>Payment Method:</strong> {booking.payment.paymentMethod}</p>
                      <p><strong>Payment Status:</strong> {booking.payment.status}</p>
                    </>
                  )}
                </div>
              </div>

              <div className="card">
                <div className="card-header">
                  <h5>Actions</h5>
                </div>
                <div className="card-body">
                  <div className="d-grid gap-2">
                    <button className="btn btn-primary" onClick={handleDownloadTicket}>
                      Download Ticket
                    </button>
                    {booking.status === 'CONFIRMED' && (
                      <button className="btn btn-outline-danger" onClick={handleCancelBooking}>
                        Cancel Booking
                      </button>
                    )}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default BookingDetails;