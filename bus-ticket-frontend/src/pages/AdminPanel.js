import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { busAPI, routeAPI, tripAPI } from '../services/api';

const AdminPanel = () => {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState('buses');
  const [message, setMessage] = useState('');
  
  // State for forms
  const [busForm, setBusForm] = useState({
    busNumber: '',
    busType: 'AC',
    totalSeats: '',
    operatorName: ''
  });
  
  const [routeForm, setRouteForm] = useState({
    source: '',
    destination: '',
    distance: '',
    duration: ''
  });
  
  const [tripForm, setTripForm] = useState({
    busId: '',
    routeId: '',
    departureTime: '',
    arrivalTime: '',
    fare: ''
  });

  // State for listings and editing
  const [buses, setBuses] = useState([]);
  const [routes, setRoutes] = useState([]);
  const [trips, setTrips] = useState([]);
  const [editingId, setEditingId] = useState(null);
  const [loading, setLoading] = useState(false);

  // Fetch data when tab changes
  useEffect(() => {
    fetchData();
  }, [activeTab]);

  const fetchData = async () => {
    setLoading(true);
    try {
      switch (activeTab) {
        case 'buses':
          const busesResponse = await busAPI.getAll();
          setBuses(busesResponse.data);
          break;
        case 'routes':
          const routesResponse = await routeAPI.getAll();
          setRoutes(routesResponse.data);
          break;
        case 'trips':
          const tripsResponse = await tripAPI.getAll();
          setTrips(tripsResponse.data);
          break;
      }
    } catch (error) {
      setMessage('Error fetching data: ' + (error.response?.data?.error || 'Unknown error'));
    } finally {
      setLoading(false);
    }
  };

  // Bus operations
  const handleBusSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingId) {
        await busAPI.update(editingId, {
          ...busForm,
          totalSeats: parseInt(busForm.totalSeats)
        });
        setMessage('Bus updated successfully!');
      } else {
        await busAPI.create({
          ...busForm,
          totalSeats: parseInt(busForm.totalSeats)
        });
        setMessage('Bus created successfully!');
      }
      resetForm();
      fetchData();
    } catch (error) {
      setMessage('Error: ' + (error.response?.data?.error || 'Unknown error'));
    }
  };

  const editBus = (bus) => {
    setBusForm({
      busNumber: bus.busNumber,
      busType: bus.busType,
      totalSeats: bus.totalSeats.toString(),
      operatorName: bus.operatorName
    });
    setEditingId(bus.id);
  };

  const deleteBus = async (id) => {
    if (!window.confirm('Are you sure you want to delete this bus?')) return;
    
    try {
      await busAPI.delete(id);
      setMessage('Bus deleted successfully!');
      fetchData();
    } catch (error) {
      setMessage('Error deleting bus: ' + (error.response?.data?.error || 'Unknown error'));
    }
  };

  // Route operations
  const handleRouteSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingId) {
        await routeAPI.update(editingId, routeForm);
        setMessage('Route updated successfully!');
      } else {
        await routeAPI.create(routeForm);
        setMessage('Route created successfully!');
      }
      resetForm();
      fetchData();
    } catch (error) {
      setMessage('Error: ' + (error.response?.data?.error || 'Unknown error'));
    }
  };

  const editRoute = (route) => {
    setRouteForm({
      source: route.source,
      destination: route.destination,
      distance: route.distance.toString(),
      duration: route.duration
    });
    setEditingId(route.id);
  };

  const deleteRoute = async (id) => {
    if (!window.confirm('Are you sure you want to delete this route?')) return;
    
    try {
      await routeAPI.delete(id);
      setMessage('Route deleted successfully!');
      fetchData();
    } catch (error) {
      setMessage('Error deleting route: ' + (error.response?.data?.error || 'Unknown error'));
    }
  };

  // Trip operations
  const handleTripSubmit = async (e) => {
    e.preventDefault();
    try {
      const tripData = {
        ...tripForm,
        fare: parseFloat(tripForm.fare)
      };

      if (editingId) {
        await tripAPI.update(editingId, tripData);
        setMessage('Trip updated successfully!');
      } else {
        await tripAPI.create(tripData);
        setMessage('Trip created successfully!');
      }
      resetForm();
      fetchData();
    } catch (error) {
      setMessage('Error: ' + (error.response?.data?.error || 'Unknown error'));
    }
  };

  const editTrip = (trip) => {
    setTripForm({
      busId: trip.bus?.id?.toString() || '',
      routeId: trip.route?.id?.toString() || '',
      departureTime: trip.departureTime?.split('.')[0] || '',
      arrivalTime: trip.arrivalTime?.split('.')[0] || '',
      fare: trip.fare?.toString() || ''
    });
    setEditingId(trip.id);
  };

  const deleteTrip = async (id) => {
    if (!window.confirm('Are you sure you want to delete this trip?')) return;
    
    try {
      await tripAPI.delete(id);
      setMessage('Trip deleted successfully!');
      fetchData();
    } catch (error) {
      setMessage('Error deleting trip: ' + (error.response?.data?.error || 'Unknown error'));
    }
  };

  const resetForm = () => {
    setBusForm({ busNumber: '', busType: 'AC', totalSeats: '', operatorName: '' });
    setRouteForm({ source: '', destination: '', distance: '', duration: '' });
    setTripForm({ busId: '', routeId: '', departureTime: '', arrivalTime: '', fare: '' });
    setEditingId(null);
  };

  const cancelEdit = () => {
    resetForm();
    setMessage('Edit cancelled');
  };

  return (
    <div className="container mt-5">
      <div className="row">
        <div className="col-md-12">
          <h2>Admin Panel 👑</h2>
          <p className="text-muted">Welcome, {user?.name}. Manage your bus reservation system.</p>

          {/* Navigation Tabs */}
          <ul className="nav nav-tabs mb-4">
            <li className="nav-item">
              <button 
                className={`nav-link ${activeTab === 'buses' ? 'active' : ''}`}
                onClick={() => { setActiveTab('buses'); resetForm(); }}
              >
                Buses
              </button>
            </li>
            <li className="nav-item">
              <button 
                className={`nav-link ${activeTab === 'routes' ? 'active' : ''}`}
                onClick={() => { setActiveTab('routes'); resetForm(); }}
              >
                Routes
              </button>
            </li>
            <li className="nav-item">
              <button 
                className={`nav-link ${activeTab === 'trips' ? 'active' : ''}`}
                onClick={() => { setActiveTab('trips'); resetForm(); }}
              >
                Trips
              </button>
            </li>
          </ul>

          {message && (
            <div className={`alert ${message.includes('Error') ? 'alert-danger' : 'alert-success'}`}>
              {message}
            </div>
          )}

          {/* Buses Management */}
          {activeTab === 'buses' && (
            <div>
              <div className="d-flex justify-content-between align-items-center mb-4">
                <h4>{editingId ? 'Edit Bus' : 'Add New Bus'}</h4>
                {editingId && (
                  <button className="btn btn-outline-secondary btn-sm" onClick={cancelEdit}>
                    Cancel Edit
                  </button>
                )}
              </div>
              
              <form onSubmit={handleBusSubmit} className="row g-3 mb-5">
                <div className="col-md-3">
                  <label className="form-label">Bus Number</label>
                  <input
                    type="text"
                    className="form-control"
                    value={busForm.busNumber}
                    onChange={(e) => setBusForm({...busForm, busNumber: e.target.value})}
                    required
                  />
                </div>
                <div className="col-md-3">
                  <label className="form-label">Bus Type</label>
                  <select
                    className="form-select"
                    value={busForm.busType}
                    onChange={(e) => setBusForm({...busForm, busType: e.target.value})}
                  >
                    <option value="AC">AC</option>
                    <option value="NON_AC">Non-AC</option>
                    <option value="SLEEPER">Sleeper</option>
                  </select>
                </div>
                <div className="col-md-3">
                  <label className="form-label">Total Seats</label>
                  <input
                    type="number"
                    className="form-control"
                    value={busForm.totalSeats}
                    onChange={(e) => setBusForm({...busForm, totalSeats: e.target.value})}
                    required
                    min="1"
                  />
                </div>
                <div className="col-md-3">
                  <label className="form-label">Operator Name</label>
                  <input
                    type="text"
                    className="form-control"
                    value={busForm.operatorName}
                    onChange={(e) => setBusForm({...busForm, operatorName: e.target.value})}
                    required
                  />
                </div>
                <div className="col-12">
                  <button type="submit" className="btn btn-primary me-2">
                    {editingId ? 'Update Bus' : 'Add Bus'}
                  </button>
                  <button type="button" className="btn btn-outline-secondary" onClick={resetForm}>
                    Clear Form
                  </button>
                </div>
              </form>

              <h5>Existing Buses</h5>
              {loading ? (
                <div className="text-center">
                  <div className="spinner-border text-primary" role="status">
                    <span className="visually-hidden">Loading...</span>
                  </div>
                </div>
              ) : (
                <div className="table-responsive">
                  <table className="table table-striped">
                    <thead>
                      <tr>
                        <th>Bus Number</th>
                        <th>Type</th>
                        <th>Seats</th>
                        <th>Operator</th>
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {buses.map(bus => (
                        <tr key={bus.id}>
                          <td>{bus.busNumber}</td>
                          <td>{bus.busType}</td>
                          <td>{bus.totalSeats}</td>
                          <td>{bus.operatorName}</td>
                          <td>
                            <button 
                              className="btn btn-sm btn-outline-primary me-2"
                              onClick={() => editBus(bus)}
                            >
                              Edit
                            </button>
                            <button 
                              className="btn btn-sm btn-outline-danger"
                              onClick={() => deleteBus(bus.id)}
                            >
                              Delete
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          )}

          {/* Routes Management */}
          {activeTab === 'routes' && (
            <div>
              <div className="d-flex justify-content-between align-items-center mb-4">
                <h4>{editingId ? 'Edit Route' : 'Add New Route'}</h4>
                {editingId && (
                  <button className="btn btn-outline-secondary btn-sm" onClick={cancelEdit}>
                    Cancel Edit
                  </button>
                )}
              </div>
              
              <form onSubmit={handleRouteSubmit} className="row g-3 mb-5">
                <div className="col-md-3">
                  <label className="form-label">Source City</label>
                  <input
                    type="text"
                    className="form-control"
                    value={routeForm.source}
                    onChange={(e) => setRouteForm({...routeForm, source: e.target.value})}
                    required
                  />
                </div>
                <div className="col-md-3">
                  <label className="form-label">Destination City</label>
                  <input
                    type="text"
                    className="form-control"
                    value={routeForm.destination}
                    onChange={(e) => setRouteForm({...routeForm, destination: e.target.value})}
                    required
                  />
                </div>
                <div className="col-md-3">
                  <label className="form-label">Distance (km)</label>
                  <input
                    type="number"
                    className="form-control"
                    value={routeForm.distance}
                    onChange={(e) => setRouteForm({...routeForm, distance: e.target.value})}
                    required
                    min="1"
                  />
                </div>
                <div className="col-md-3">
                  <label className="form-label">Duration</label>
                  <input
                    type="text"
                    className="form-control"
                    value={routeForm.duration}
                    onChange={(e) => setRouteForm({...routeForm, duration: e.target.value})}
                    placeholder="e.g., 4h 30m"
                    required
                  />
                </div>
                <div className="col-12">
                  <button type="submit" className="btn btn-primary me-2">
                    {editingId ? 'Update Route' : 'Add Route'}
                  </button>
                  <button type="button" className="btn btn-outline-secondary" onClick={resetForm}>
                    Clear Form
                  </button>
                </div>
              </form>

              <h5>Existing Routes</h5>
              {loading ? (
                <div className="text-center">
                  <div className="spinner-border text-primary" role="status">
                    <span className="visually-hidden">Loading...</span>
                  </div>
                </div>
              ) : (
                <div className="table-responsive">
                  <table className="table table-striped">
                    <thead>
                      <tr>
                        <th>Source</th>
                        <th>Destination</th>
                        <th>Distance</th>
                        <th>Duration</th>
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {routes.map(route => (
                        <tr key={route.id}>
                          <td>{route.source}</td>
                          <td>{route.destination}</td>
                          <td>{route.distance} km</td>
                          <td>{route.duration}</td>
                          <td>
                            <button 
                              className="btn btn-sm btn-outline-primary me-2"
                              onClick={() => editRoute(route)}
                            >
                              Edit
                            </button>
                            <button 
                              className="btn btn-sm btn-outline-danger"
                              onClick={() => deleteRoute(route.id)}
                            >
                              Delete
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          )}

          {/* Trips Management */}
          {activeTab === 'trips' && (
            <div>
              <div className="d-flex justify-content-between align-items-center mb-4">
                <h4>{editingId ? 'Edit Trip' : 'Schedule New Trip'}</h4>
                {editingId && (
                  <button className="btn btn-outline-secondary btn-sm" onClick={cancelEdit}>
                    Cancel Edit
                  </button>
                )}
              </div>
              
              <form onSubmit={handleTripSubmit} className="row g-3 mb-5">
                <div className="col-md-3">
                  <label className="form-label">Bus ID</label>
                  <input
                    type="number"
                    className="form-control"
                    value={tripForm.busId}
                    onChange={(e) => setTripForm({...tripForm, busId: e.target.value})}
                    required
                    min="1"
                  />
                </div>
                <div className="col-md-3">
                  <label className="form-label">Route ID</label>
                  <input
                    type="number"
                    className="form-control"
                    value={tripForm.routeId}
                    onChange={(e) => setTripForm({...tripForm, routeId: e.target.value})}
                    required
                    min="1"
                  />
                </div>
                <div className="col-md-3">
                  <label className="form-label">Departure Time</label>
                  <input
                    type="datetime-local"
                    className="form-control"
                    value={tripForm.departureTime}
                    onChange={(e) => setTripForm({...tripForm, departureTime: e.target.value})}
                    required
                  />
                </div>
                <div className="col-md-3">
                  <label className="form-label">Arrival Time</label>
                  <input
                    type="datetime-local"
                    className="form-control"
                    value={tripForm.arrivalTime}
                    onChange={(e) => setTripForm({...tripForm, arrivalTime: e.target.value})}
                    required
                  />
                </div>
                <div className="col-md-3">
                  <label className="form-label">Fare ($)</label>
                  <input
                    type="number"
                    className="form-control"
                    value={tripForm.fare}
                    onChange={(e) => setTripForm({...tripForm, fare: e.target.value})}
                    required
                    min="1"
                    step="0.01"
                  />
                </div>
                <div className="col-12">
                  <button type="submit" className="btn btn-primary me-2">
                    {editingId ? 'Update Trip' : 'Schedule Trip'}
                  </button>
                  <button type="button" className="btn btn-outline-secondary" onClick={resetForm}>
                    Clear Form
                  </button>
                </div>
              </form>

              <h5>Existing Trips</h5>
              {loading ? (
                <div className="text-center">
                  <div className="spinner-border text-primary" role="status">
                    <span className="visually-hidden">Loading...</span>
                  </div>
                </div>
              ) : (
                <div className="table-responsive">
                  <table className="table table-striped">
                    <thead>
                      <tr>
                        <th>Bus</th>
                        <th>Route</th>
                        <th>Departure</th>
                        <th>Arrival</th>
                        <th>Fare</th>
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {trips.map(trip => (
                        <tr key={trip.id}>
                          <td>Bus #{trip.bus?.busNumber}</td>
                          <td>{trip.route?.source} → {trip.route?.destination}</td>
                          <td>{new Date(trip.departureTime).toLocaleString()}</td>
                          <td>{new Date(trip.arrivalTime).toLocaleString()}</td>
                          <td>${trip.fare}</td>
                          <td>
                            <button 
                              className="btn btn-sm btn-outline-primary me-2"
                              onClick={() => editTrip(trip)}
                            >
                              Edit
                            </button>
                            <button 
                              className="btn btn-sm btn-outline-danger"
                              onClick={() => deleteTrip(trip.id)}
                            >
                              Delete
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default AdminPanel;