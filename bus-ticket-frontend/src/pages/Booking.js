import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { bookingAPI, tripAPI } from '../services/api';

const Booking = () => {
  const { tripId } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  
  const [loading, setLoading] = useState(true);
  const [trip, setTrip] = useState(null);
  const [seats, setSeats] = useState([]);
  const [selectedSeats, setSelectedSeats] = useState([]);
  const [passengerDetails, setPassengerDetails] = useState({});
  const [step, setStep] = useState(1);
  const [paymentMethod, setPaymentMethod] = useState('card');
  const [paymentDetails, setPaymentDetails] = useState({
    cardNumber: '',
    expiryDate: '',
    cvv: '',
    cardholderName: '',
    upiId: '',
    walletType: ''
  });
  const [bookingComplete, setBookingComplete] = useState(false);
  const [bookingData, setBookingData] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchTripAndSeats = async () => {
      try {
        setLoading(true);
        setError('');
        
        // Fetch trip details
        const tripResponse = await tripAPI.getById(tripId);
        setTrip(tripResponse.data);
        
        // Fetch available seats
        const seatsResponse = await tripAPI.getSeats(tripId);
        setSeats(seatsResponse.data);
        
      } catch (error) {
        console.error('Error fetching trip details:', error);
        setError('Failed to load trip details. Please try again.');
      } finally {
        setLoading(false);
      }
    };

    if (tripId) {
      fetchTripAndSeats();
    }
  }, [tripId]);

  useEffect(() => {
    if (selectedSeats.length > 0) {
      const newDetails = {...passengerDetails};
      let needsUpdate = false;
      
      selectedSeats.forEach(seatId => {
        if (!newDetails[seatId]) {
          newDetails[seatId] = {
            name: user?.name || '',
            age: '',
            gender: 'male'
          };
          needsUpdate = true;
        }
      });
      
      Object.keys(newDetails).forEach(seatId => {
        if (!selectedSeats.includes(seatId)) {
          delete newDetails[seatId];
          needsUpdate = true;
        }
      });
      
      if (needsUpdate) {
        setPassengerDetails(newDetails);
      }
    }
  }, [selectedSeats, user?.name]);

  const handleSeatSelect = (seatId) => {
    const seat = seats.find(s => s.id === seatId);
    if (!seat || seat.isBooked) return;
    
    setSelectedSeats(prev => {
      if (prev.includes(seatId)) {
        return prev.filter(id => id !== seatId);
      } else {
        return [...prev, seatId];
      }
    });
  };

  const handlePassengerDetailChange = (seatId, field, value) => {
    setPassengerDetails(prev => ({
      ...prev,
      [seatId]: {
        ...prev[seatId],
        [field]: value
      }
    }));
  };

  const handlePaymentDetailChange = (field, value) => {
    setPaymentDetails(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const validatePassengerDetails = () => {
    return Object.values(passengerDetails).every(
      detail => detail?.name?.trim() && detail?.age && detail.age >= 1 && detail.age <= 100
    );
  };

  const validatePaymentDetails = () => {
    if (paymentMethod === 'card') {
      return paymentDetails.cardNumber && paymentDetails.expiryDate && 
             paymentDetails.cvv && paymentDetails.cardholderName;
    } else if (paymentMethod === 'upi') {
      return paymentDetails.upiId;
    } else if (paymentMethod === 'wallet') {
      return paymentDetails.walletType;
    }
    return false;
  };

  const handleNextStep = () => {
    if (step === 1 && selectedSeats.length === 0) {
      setError('Please select at least one seat');
      return;
    }
    if (step === 2 && !validatePassengerDetails()) {
      setError('Please fill all passenger details correctly');
      return;
    }
    setError('');
    setStep(step + 1);
  };

  const handlePrevStep = () => {
    setError('');
    setStep(step - 1);
  };

  const handlePayment = async () => {
    try {
      if (!validatePaymentDetails()) {
        setError('Please fill in all payment details');
        return;
      }

      setLoading(true);
      setError('');
      
      const bookingPayload = {
        tripId: parseInt(tripId),
        seatIds: selectedSeats.map(id => parseInt(id)),
        paymentMethod: paymentMethod
      };

      const response = await bookingAPI.create(bookingPayload);
      
      setBookingData(response.data);
      setBookingComplete(true);
      setStep(4);
      
    } catch (error) {
      console.error('Booking error:', error);
      const errorMessage = error.response?.data?.error || 
                          'Booking failed. Please try again.';
      setError(errorMessage);
    } finally {
      setLoading(false);
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

  if (error && !trip) {
    return (
      <div className="container mt-5">
        <div className="alert alert-danger">{error}</div>
        <button className="btn btn-primary" onClick={() => navigate('/search')}>
          Back to Search
        </button>
      </div>
    );
  }

  if (!trip) {
    return (
      <div className="container mt-5">
        <div className="alert alert-danger">Trip not found</div>
        <button className="btn btn-primary" onClick={() => navigate('/search')}>
          Back to Search
        </button>
      </div>
    );
  }

  const totalAmount = selectedSeats.length * (trip.fare || 50);

  return (
    <div className="container mt-4">
      {error && (
        <div className="alert alert-danger alert-dismissible fade show" role="alert">
          {error}
          <button type="button" className="btn-close" onClick={() => setError('')}></button>
        </div>
      )}

      <div className="row mb-4">
        <div className="col-md-12">
          <ul className="progress-bar">
            <li className={step >= 1 ? 'active' : ''}>Select Seats</li>
            <li className={step >= 2 ? 'active' : ''}>Passenger Details</li>
            <li className={step >= 3 ? 'active' : ''}>Payment</li>
            <li className={step >= 4 ? 'active' : ''}>Confirmation</li>
          </ul>
        </div>
      </div>

      <div className="row">
        <div className="col-md-8">
          {/* Step 1: Seat Selection */}
          {step === 1 && (
            <div className="card">
              <div className="card-header">
                <h5>Select Seats</h5>
              </div>
              <div className="card-body">
                <div className="row">
                  {seats.map(seat => (
                    <div key={seat.id} className="col-3 col-md-2 mb-3">
                      <button
                        className={`btn w-100 ${
                          seat.isBooked ? 'btn-secondary disabled' :
                          selectedSeats.includes(seat.id) ? 'btn-success' : 'btn-outline-primary'
                        } seat`}
                        onClick={() => handleSeatSelect(seat.id)}
                        disabled={seat.isBooked}
                        title={seat.isBooked ? 'Already booked' : `Seat ${seat.seatNumber}`}
                      >
                        {seat.seatNumber}
                      </button>
                    </div>
                  ))}
                </div>
                <div className="mt-3">
                  <button className="btn btn-primary" onClick={handleNextStep}>
                    Next: Passenger Details
                  </button>
                </div>
              </div>
            </div>
          )}

          {/* Step 2: Passenger Details */}
          {step === 2 && (
            <div className="card">
              <div className="card-header">
                <h5>Passenger Details</h5>
              </div>
              <div className="card-body">
                {selectedSeats.map((seatId, index) => {
                  const seat = seats.find(s => s.id === seatId);
                  const details = passengerDetails[seatId] || {};
                  return (
                    <div key={seatId} className="passenger-form p-3 mb-3">
                      <h6>Passenger {index + 1} (Seat {seat?.seatNumber})</h6>
                      <div className="row">
                        <div className="col-md-6 mb-2">
                          <input
                            type="text"
                            className="form-control"
                            placeholder="Full Name"
                            value={details.name || ''}
                            onChange={(e) => handlePassengerDetailChange(seatId, 'name', e.target.value)}
                          />
                        </div>
                        <div className="col-md-3 mb-2">
                          <input
                            type="number"
                            className="form-control"
                            placeholder="Age"
                            value={details.age || ''}
                            onChange={(e) => handlePassengerDetailChange(seatId, 'age', e.target.value)}
                          />
                        </div>
                        <div className="col-md-3 mb-2">
                          <select
                            className="form-select"
                            value={details.gender || 'male'}
                            onChange={(e) => handlePassengerDetailChange(seatId, 'gender', e.target.value)}
                          >
                            <option value="male">Male</option>
                            <option value="female">Female</option>
                            <option value="other">Other</option>
                          </select>
                        </div>
                      </div>
                    </div>
                  );
                })}
                <div className="d-flex gap-2">
                  <button className="btn btn-outline-secondary" onClick={handlePrevStep}>
                    Back
                  </button>
                  <button className="btn btn-primary" onClick={handleNextStep}>
                    Next: Payment
                  </button>
                </div>
              </div>
            </div>
          )}

          {/* Step 3: Payment */}
          {step === 3 && (
            <div className="card">
              <div className="card-header">
                <h5>Payment</h5>
              </div>
              <div className="card-body">
                <div className="mb-3">
                  <label className="form-label">Payment Method</label>
                  <select
                    className="form-select"
                    value={paymentMethod}
                    onChange={(e) => setPaymentMethod(e.target.value)}
                  >
                    <option value="card">Credit/Debit Card</option>
                    <option value="upi">UPI</option>
                    <option value="wallet">Wallet</option>
                  </select>
                </div>

                {paymentMethod === 'card' && (
                  <div className="card-payment-form">
                    <div className="row">
                      <div className="col-md-12 mb-3">
                        <label className="form-label">Card Number</label>
                        <input 
                          type="text" 
                          className="form-control" 
                          placeholder="1234 5678 9012 3456"
                          value={paymentDetails.cardNumber}
                          onChange={(e) => handlePaymentDetailChange('cardNumber', e.target.value)}
                        />
                      </div>
                      <div className="col-md-6 mb-3">
                        <label className="form-label">Expiry Date</label>
                        <input 
                          type="text" 
                          className="form-control" 
                          placeholder="MM/YY"
                          value={paymentDetails.expiryDate}
                          onChange={(e) => handlePaymentDetailChange('expiryDate', e.target.value)}
                        />
                      </div>
                      <div className="col-md-6 mb-3">
                        <label className="form-label">CVV</label>
                        <input 
                          type="text" 
                          className="form-control" 
                          placeholder="123"
                          value={paymentDetails.cvv}
                          onChange={(e) => handlePaymentDetailChange('cvv', e.target.value)}
                        />
                      </div>
                      <div className="col-md-12 mb-3">
                        <label className="form-label">Cardholder Name</label>
                        <input 
                          type="text" 
                          className="form-control" 
                          placeholder="John Doe"
                          value={paymentDetails.cardholderName}
                          onChange={(e) => handlePaymentDetailChange('cardholderName', e.target.value)}
                        />
                      </div>
                    </div>
                  </div>
                )}

                {paymentMethod === 'upi' && (
                  <div className="upi-payment-form">
                    <div className="mb-3">
                      <label className="form-label">UPI ID</label>
                      <input 
                        type="text" 
                        className="form-control" 
                        placeholder="username@upi"
                        value={paymentDetails.upiId}
                        onChange={(e) => handlePaymentDetailChange('upiId', e.target.value)}
                      />
                    </div>
                  </div>
                )}

                {paymentMethod === 'wallet' && (
                  <div className="wallet-payment-form">
                    <div className="mb-3">
                      <label className="form-label">Select Wallet</label>
                      <select 
                        className="form-select"
                        value={paymentDetails.walletType}
                        onChange={(e) => handlePaymentDetailChange('walletType', e.target.value)}
                      >
                        <option value="">Select Wallet</option>
                        <option value="paytm">Paytm</option>
                        <option value="phonepe">PhonePe</option>
                        <option value="amazonpay">Amazon Pay</option>
                      </select>
                    </div>
                  </div>
                )}

                <div className="d-flex gap-2">
                  <button className="btn btn-outline-secondary" onClick={handlePrevStep}>
                    Back
                  </button>
                  <button className="btn btn-primary" onClick={handlePayment} disabled={loading}>
                    {loading ? 'Processing...' : `Pay $${totalAmount}`}
                  </button>
                </div>
              </div>
            </div>
          )}

          {/* Step 4: Confirmation */}
          {step === 4 && bookingComplete && (
            <div className="card">
              <div className="card-body text-center">
                <div className="checkmark">✓</div>
                <h4 className="text-success mb-3">Booking Confirmed!</h4>
                <p>Your booking has been successfully completed.</p>
                <p><strong>Booking ID:</strong> #{bookingData?.id}</p>
                <p><strong>Total Amount:</strong> ${totalAmount}</p>
                <div className="d-flex gap-2 justify-content-center">
                  <button className="btn btn-primary" onClick={() => navigate('/dashboard')}>
                    View Dashboard
                  </button>
                  <button className="btn btn-outline-primary">
                    Download Ticket
                  </button>
                </div>
              </div>
            </div>
          )}
        </div>

        <div className="col-md-4">
          <div className="card booking-summary">
            <div className="card-header">
              <h5>Booking Summary</h5>
            </div>
            <div className="card-body">
              <p><strong>Route:</strong> {trip.route?.source} → {trip.route?.destination}</p>
              <p><strong>Departure:</strong> {new Date(trip.departureTime).toLocaleString()}</p>
              <p><strong>Bus:</strong> {trip.bus?.busNumber} ({trip.bus?.busType})</p>
              <p><strong>Selected Seats:</strong> {selectedSeats.map(id => {
                const seat = seats.find(s => s.id === id);
                return seat?.seatNumber;
              }).join(', ')}</p>
              <hr />
              <h6>Price Breakdown:</h6>
              <p>Seats: {selectedSeats.length} x ${trip.fare || 50}</p>
              <h5 className="text-primary">Total: ${totalAmount}</h5>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Booking;