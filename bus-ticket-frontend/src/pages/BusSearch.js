import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { busAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';

const BusSearch = () => {
  const [searchParams, setSearchParams] = useState({
    origin: '',
    destination: '',
    travelDate: ''
  });
  const [buses, setBuses] = useState([]);
  const [allBuses, setAllBuses] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [showFilters, setShowFilters] = useState(false);
  const [filters, setFilters] = useState({
    busType: '',
    minPrice: '',
    maxPrice: '',
    departureTime: '',
    operator: ''
  });
  
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  const cities = [
    'Mumbai', 'Delhi', 'Bangalore', 'Hyderabad', 'Ahmedabad',
    'Chennai', 'Kolkata', 'Surat', 'Pune', 'Jaipur',
    'Lucknow', 'Kanpur', 'Nagpur', 'Indore', 'Thane',
    'Bhopal', 'Visakhapatnam', 'Pimpri-Chinchwad', 'Patna', 'Vadodara',
    'Ghaziabad', 'Ludhiana', 'Agra', 'Nashik', 'Faridabad',
    'Meerut', 'Rajkot', 'Kalyan-Dombivli', 'Vasai-Virar', 'Varanasi',
    'Srinagar', 'Aurangabad', 'Dhanbad', 'Amritsar', 'Navi Mumbai',
    'Allahabad', 'Howrah', 'Gwalior', 'Jabalpur', 'Coimbatore',
    'Vijayawada', 'Jodhpur', 'Madurai', 'Raipur', 'Kota',
    'Chandigarh', 'Guwahati', 'Solapur', 'Hubli-Dharwad', 'Mysore',
    'Tiruchirappalli', 'Bareilly', 'Aligarh', 'Tiruppur', 'Gurgaon',
    'Moradabad', 'Jalandhar', 'Bhubaneswar', 'Salem', 'Warangal',
    'Guntur', 'Bhiwandi', 'Saharanpur', 'Gorakhpur', 'Bikaner',
    'Amravati', 'Noida', 'Jamshedpur', 'Bhilai', 'Cuttack',
    'Firozabad', 'Kochi', 'Nellore', 'Bhavnagar', 'Dehradun',
    'Durgapur', 'Asansol', 'Rourkela', 'Nanded', 'Kolhapur',
    'Ajmer', 'Akola', 'Gulbarga', 'Jamnagar', 'Ujjain',
    'Loni', 'Siliguri', 'Jhansi'
  ];

  const handleSearch = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    if (!searchParams.origin || !searchParams.destination || !searchParams.travelDate) {
      setError('Please fill in all search fields');
      setLoading(false);
      return;
    }

    if (searchParams.origin === searchParams.destination) {
      setError('Origin and destination cannot be the same');
      setLoading(false);
      return;
    }

    try {
      // Make actual API call
      const response = await busAPI.search({
        origin: searchParams.origin,
        destination: searchParams.destination,
        date: searchParams.travelDate
      });
      
      setBuses(response.data);
      setAllBuses(response.data); // Store all buses for filtering
    } catch (err) {
      console.error('Search error:', err);
      setError(err.response?.data?.error || 'Failed to search buses. Please try again.');
      
      // Fallback to sample data if API fails (for demo purposes)
      const sampleBuses = getSampleBuses(searchParams.origin, searchParams.destination);
      setBuses(sampleBuses);
      setAllBuses(sampleBuses);
    } finally {
      setLoading(false);
    }
  };

  // Sample data generator for fallback
  const getSampleBuses = (origin, destination) => {
    const busTypes = ['AC', 'Non-AC', 'AC Sleeper'];
    const operators = ['City Express', 'Travel Safe', 'Luxury Lines', 'Comfort Travel', 'Express Bus'];
    const times = ['08:00 AM', '10:30 AM', '11:00 PM', '02:00 PM', '06:00 PM', '09:00 PM'];
    
    return Array.from({ length: 5 }, (_, i) => ({
      id: i + 1,
      busNumber: `BUS${String(i + 1).padStart(3, '0')}`,
      busType: busTypes[i % busTypes.length],
      operatorName: operators[i % operators.length],
      origin: origin,
      destination: destination,
      departureTime: times[i % times.length],
      arrivalTime: calculateArrivalTime(times[i % times.length]),
      duration: '4h 30m',
      price: Math.floor(Math.random() * 50) + 20,
      availableSeats: Math.floor(Math.random() * 30) + 5,
      amenities: getRandomAmenities(i % busTypes.length)
    }));
  };

  const calculateArrivalTime = (departureTime) => {
    const [time, modifier] = departureTime.split(' ');
    let [hours, minutes] = time.split(':').map(Number);
    
    if (modifier === 'PM' && hours !== 12) hours += 12;
    if (modifier === 'AM' && hours === 12) hours = 0;
    
    hours = (hours + 4) % 24;
    minutes = (minutes + 30) % 60;
    
    const newModifier = hours >= 12 ? 'PM' : 'AM';
    const displayHours = hours % 12 || 12;
    
    return `${displayHours}:${minutes.toString().padStart(2, '0')} ${newModifier}`;
  };

  const getRandomAmenities = (busTypeIndex) => {
    const baseAmenities = ['Restroom'];
    const premiumAmenities = ['WiFi', 'Charging Ports', 'Blankets', 'Snacks', 'Water Bottle'];
    
    if (busTypeIndex === 0) return [...baseAmenities, 'WiFi', 'Charging Ports'];
    if (busTypeIndex === 2) return [...baseAmenities, ...premiumAmenities];
    return baseAmenities;
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setSearchParams(prev => ({ ...prev, [name]: value }));
  };

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilters(prev => ({ ...prev, [name]: value }));
  };

  const handleBookNow = (busId) => {
    if (!isAuthenticated) {
      alert('Please login to book a bus');
      navigate('/login');
      return;
    }
    navigate(`/booking/${busId}`);
  };

  // Apply filters to the buses
  const filteredBuses = allBuses.filter(bus => {
    if (filters.busType && bus.busType !== filters.busType) return false;
    if (filters.minPrice && bus.price < parseInt(filters.minPrice)) return false;
    if (filters.maxPrice && bus.price > parseInt(filters.maxPrice)) return false;
    if (filters.operator && !bus.operatorName.toLowerCase().includes(filters.operator.toLowerCase())) return false;
    if (filters.departureTime) {
      const timeFilter = filters.departureTime;
      const [time, modifier] = bus.departureTime.split(' ');
      let hours = parseInt(time.split(':')[0]);
      
      if (modifier === 'PM' && hours !== 12) hours += 12;
      if (modifier === 'AM' && hours === 12) hours = 0;
      
      if (timeFilter === 'morning' && (hours < 6 || hours >= 12)) return false;
      if (timeFilter === 'afternoon' && (hours < 12 || hours >= 18)) return false;
      if (timeFilter === 'evening' && (hours < 18 || hours >= 24)) return false;
      if (timeFilter === 'night' && (hours < 0 || hours >= 6)) return false;
    }
    return true;
  });

  // Swap origin and destination
  const swapCities = () => {
    setSearchParams(prev => ({
      ...prev,
      origin: prev.destination,
      destination: prev.origin
    }));
  };

  return (
    <div className="container mt-4">
      <div className="row">
        <div className="col-md-12">
          <h2 className="mb-4">Search Buses</h2>
          
          {/* Search Form */}
          <div className="card mb-4">
            <div className="card-body">
              <form onSubmit={handleSearch}>
                <div className="row align-items-end">
                  <div className="col-md-4">
                    <div className="form-group">
                      <label htmlFor="origin">From</label>
                      <div className="input-group">
                        <input
                          type="text"
                          className="form-control"
                          id="origin"
                          name="origin"
                          value={searchParams.origin}
                          onChange={handleInputChange}
                          list="cityList"
                          placeholder="Departure city"
                          required
                        />
                        <button
                          type="button"
                          className="btn btn-outline-secondary"
                          onClick={swapCities}
                          title="Swap cities"
                        >
                          ⇄
                        </button>
                      </div>
                    </div>
                  </div>
                  <div className="col-md-4">
                    <div className="form-group">
                      <label htmlFor="destination">To</label>
                      <input
                        type="text"
                        className="form-control"
                        id="destination"
                        name="destination"
                        value={searchParams.destination}
                        onChange={handleInputChange}
                        list="cityList"
                        placeholder="Destination city"
                        required
                      />
                    </div>
                  </div>
                  <div className="col-md-3">
                    <div className="form-group">
                      <label htmlFor="travelDate">Travel Date</label>
                      <input
                        type="date"
                        className="form-control"
                        id="travelDate"
                        name="travelDate"
                        value={searchParams.travelDate}
                        onChange={handleInputChange}
                        min={new Date().toISOString().split('T')[0]}
                        required
                      />
                    </div>
                  </div>
                  <div className="col-md-1 d-flex align-items-end">
                    <button 
                      type="submit" 
                      className="btn btn-primary w-100"
                      disabled={loading}
                    >
                      {loading ? (
                        <>
                          <span className="spinner-border spinner-border-sm me-2" role="status"></span>
                          Searching...
                        </>
                      ) : (
                        'Search'
                      )}
                    </button>
                  </div>
                </div>
              </form>

              <datalist id="cityList">
                {cities.map(city => (
                  <option key={city} value={city} />
                ))}
              </datalist>

              <div className="mt-3">
                <button 
                  className="btn btn-outline-secondary btn-sm"
                  onClick={() => setShowFilters(!showFilters)}
                >
                  {showFilters ? 'Hide Filters' : 'Show Filters'}
                </button>
              </div>

              {showFilters && (
                <div className="row mt-3">
                  <div className="col-md-2">
                    <div className="form-group">
                      <label htmlFor="busType">Bus Type</label>
                      <select
                        className="form-control"
                        id="busType"
                        name="busType"
                        value={filters.busType}
                        onChange={handleFilterChange}
                      >
                        <option value="">All Types</option>
                        <option value="AC">AC</option>
                        <option value="Non-AC">Non-AC</option>
                        <option value="AC Sleeper">AC Sleeper</option>
                      </select>
                    </div>
                  </div>
                  <div className="col-md-2">
                    <div className="form-group">
                      <label htmlFor="minPrice">Min Price ($)</label>
                      <input
                        type="number"
                        className="form-control"
                        id="minPrice"
                        name="minPrice"
                        value={filters.minPrice}
                        onChange={handleFilterChange}
                        min="0"
                        placeholder="Min"
                      />
                    </div>
                  </div>
                  <div className="col-md-2">
                    <div className="form-group">
                      <label htmlFor="maxPrice">Max Price ($)</label>
                      <input
                        type="number"
                        className="form-control"
                        id="maxPrice"
                        name="maxPrice"
                        value={filters.maxPrice}
                        onChange={handleFilterChange}
                        min="0"
                        placeholder="Max"
                      />
                    </div>
                  </div>
                  <div className="col-md-3">
                    <div className="form-group">
                      <label htmlFor="operator">Operator</label>
                      <input
                        type="text"
                        className="form-control"
                        id="operator"
                        name="operator"
                        value={filters.operator}
                        onChange={handleFilterChange}
                        placeholder="Operator name"
                      />
                    </div>
                  </div>
                  <div className="col-md-3">
                    <div className="form-group">
                      <label htmlFor="departureTime">Departure Time</label>
                      <select
                        className="form-control"
                        id="departureTime"
                        name="departureTime"
                        value={filters.departureTime}
                        onChange={handleFilterChange}
                      >
                        <option value="">Any Time</option>
                        <option value="morning">Morning (6AM - 12PM)</option>
                        <option value="afternoon">Afternoon (12PM - 6PM)</option>
                        <option value="evening">Evening (6PM - 12AM)</option>
                        <option value="night">Night (12AM - 6AM)</option>
                      </select>
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>

          {error && (
            <div className="alert alert-danger alert-dismissible fade show" role="alert">
              {error}
              <button type="button" className="btn-close" onClick={() => setError('')}></button>
            </div>
          )}

          {/* Results */}
          <div className="mb-4">
            <div className="d-flex justify-content-between align-items-center">
              <h4>Available Buses ({filteredBuses.length})</h4>
              {filteredBuses.length > 0 && (
                <div className="text-muted small">
                  Showing results for: {searchParams.origin} → {searchParams.destination} on {searchParams.travelDate}
                </div>
              )}
            </div>
            {filteredBuses.length === 0 && !loading && (
              <div className="alert alert-info">
                No buses found for your search criteria. Please try different search parameters.
              </div>
            )}
          </div>

          {loading ? (
            <div className="text-center my-5">
              <div className="spinner-border text-primary" role="status">
                <span className="visually-hidden">Loading...</span>
              </div>
              <p className="mt-2">Searching for buses...</p>
            </div>
          ) : (
            <div className="row">
              {filteredBuses.map(bus => (
                <div key={bus.id} className="col-md-12 mb-4">
                  <div className="card card-hover shadow-sm">
                    <div className="card-body">
                      <div className="row align-items-center">
                        <div className="col-md-3">
                          <h5 className="mb-1">{bus.operatorName}</h5>
                          <span className={`badge ${bus.busType === 'AC' ? 'bg-info' : bus.busType === 'AC Sleeper' ? 'bg-primary' : 'bg-secondary'}`}>
                            {bus.busType}
                          </span>
                          <p className="mt-2 mb-0 small">Bus No: {bus.busNumber}</p>
                        </div>
                        <div className="col-md-2">
                          <div className="text-center">
                            <h4 className="mb-0">{bus.departureTime}</h4>
                            <p className="mb-0 small text-muted">{bus.origin}</p>
                          </div>
                        </div>
                        <div className="col-md-2">
                          <div className="text-center">
                            <p className="mb-1 text-muted">{bus.duration}</p>
                            <div className="border-bottom mx-auto" style={{width: '50px'}}></div>
                            <p className="mt-1 small text-muted">Direct</p>
                          </div>
                        </div>
                        <div className="col-md-2">
                          <div className="text-center">
                            <h4 className="mb-0">{bus.arrivalTime}</h4>
                            <p className="mb-0 small text-muted">{bus.destination}</p>
                          </div>
                        </div>
                        <div className="col-md-3">
                          <div className="text-center">
                            <h3 className="text-primary mb-1">${bus.price}</h3>
                            <p className="mb-2 small text-muted">{bus.availableSeats} seats available</p>
                            <button 
                              className="btn btn-primary btn-sm"
                              onClick={() => handleBookNow(bus.id)}
                              disabled={bus.availableSeats === 0}
                            >
                              {bus.availableSeats === 0 ? 'Sold Out' : 'Book Now'}
                            </button>
                          </div>
                        </div>
                      </div>
                      
                      {bus.amenities && bus.amenities.length > 0 && (
                        <div className="row mt-3">
                          <div className="col-md-12">
                            <div className="d-flex flex-wrap align-items-center">
                              <span className="small text-muted me-2">Amenities:</span>
                              {bus.amenities.map((amenity, index) => (
                                <span key={index} className="badge bg-light text-dark me-2 mb-1">
                                  {amenity}
                                </span>
                              ))}
                            </div>
                          </div>
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default BusSearch;