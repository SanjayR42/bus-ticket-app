import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Home = () => {
  const { isAuthenticated, user } = useAuth();

  const features = [
    {
      icon: '🚌',
      title: 'Wide Bus Selection',
      description: 'Choose from hundreds of buses across different routes and operators'
    },
    {
      icon: '💰',
      title: 'Best Prices',
      description: 'Get the best deals and discounts on bus tickets'
    },
    {
      icon: '⚡',
      title: 'Instant Confirmation',
      description: 'Receive instant booking confirmation with e-tickets'
    },
    {
      icon: '🔒',
      title: 'Secure Booking',
      description: 'Your transactions are safe and secure with our payment system'
    },
    {
      icon: '📱',
      title: 'Mobile Friendly',
      description: 'Book tickets anytime, anywhere from your mobile device'
    },
    {
      icon: '⭐',
      title: '24/7 Support',
      description: 'Our customer support team is available round the clock'
    }
  ];

  const popularRoutes = [
    { from: 'Chennai', to: 'Bangalore', price: 'Rs.1550' },
    { from: 'Hyderabad', to: 'Pune', price: 'Rs.1700' },
    { from: 'Delhi', to: 'Varanasi', price: 'Rs.3500' },
    { from: 'Madurai', to: 'Coimbatore', price: '$1440' }
  ];

  return (
    <div className="home-page">
      {/* Hero Section */}
      <section className="hero-section bus-bg-primary text-white py-5">
        <div className="container">
          <div className="row align-items-center">
            <div className="col-lg-6">
              <h1 className="display-4 fw-bold mb-4">
                Book Bus Tickets <br />
                <span className="text-warning">Easy & Fast</span>
              </h1>
              <p className="lead mb-4">
                Travel across the country with comfort and convenience. 
                Book your bus tickets online and enjoy your journey!
              </p>
              <div className="d-flex gap-3 flex-wrap">
                {!isAuthenticated ? (
                  <>
                    <Link to="/register" className="btn btn-warning btn-lg px-4 py-2">
                      Get Started
                    </Link>
                    <Link to="/login" className="btn btn-outline-light btn-lg px-4 py-2">
                      Login
                    </Link>
                  </>
                ) : (
                  <Link to="/search" className="btn btn-warning btn-lg px-4 py-2">
                    Search Buses
                  </Link>
                )}
              </div>
            </div>
            <div className="col-lg-6 text-center">
              <div className="hero-image mt-4 mt-lg-0">
                <div className="bus-icon" style={{ fontSize: '8rem' }}>🚌</div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-5">
        <div className="container">
          <div className="text-center mb-5">
            <h2 className="display-5 fw-bold">Why Choose Us?</h2>
            <p className="lead text-muted">We make bus travel simple and convenient</p>
          </div>
          <div className="row">
            {features.map((feature, index) => (
              <div key={index} className="col-md-4 mb-4">
                <div className="card h-100 border-0 shadow-sm card-hover">
                  <div className="card-body text-center p-4">
                    <div className="feature-icon mb-3" style={{ fontSize: '3rem' }}>
                      {feature.icon}
                    </div>
                    <h5 className="card-title fw-bold">{feature.title}</h5>
                    <p className="card-text text-muted">{feature.description}</p>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Popular Routes */}
      <section className="py-5 bg-light">
        <div className="container">
          <div className="text-center mb-5">
            <h2 className="display-5 fw-bold">Popular Routes</h2>
            <p className="lead text-muted">Check out our most popular bus routes</p>
          </div>
          <div className="row">
            {popularRoutes.map((route, index) => (
              <div key={index} className="col-md-3 mb-4">
                <div className="card h-100 border-0 shadow-sm">
                  <div className="card-body text-center p-4">
                    <h5 className="card-title fw-bold">
                      {route.from} → {route.to}
                    </h5>
                    <p className="text-primary fw-bold fs-4 mb-0">{route.price}</p>
                    <small className="text-muted">Starting from</small>
                    <div className="mt-3">
                      <Link 
                        to="/search" 
                        className="btn btn-outline-primary btn-sm"
                      >
                        Book Now
                      </Link>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* How It Works */}
      <section className="py-5">
        <div className="container">
          <div className="text-center mb-5">
            <h2 className="display-5 fw-bold">How It Works</h2>
            <p className="lead text-muted">Book your bus tickets in 3 simple steps</p>
          </div>
          <div className="row">
            <div className="col-md-4 text-center mb-4">
              <div className="step-number mx-auto mb-3 d-flex align-items-center justify-content-center rounded-circle bg-primary text-white fw-bold fs-3" 
                   style={{ width: '60px', height: '60px' }}>
                1
              </div>
              <h5>Search Buses</h5>
              <p className="text-muted">Enter your route and travel date to find available buses</p>
            </div>
            <div className="col-md-4 text-center mb-4">
              <div className="step-number mx-auto mb-3 d-flex align-items-center justify-content-center rounded-circle bg-primary text-white fw-bold fs-3" 
                   style={{ width: '60px', height: '60px' }}>
                2
              </div>
              <h5>Select & Book</h5>
              <p className="text-muted">Choose your preferred bus and seats, then book your tickets</p>
            </div>
            <div className="col-md-4 text-center mb-4">
              <div className="step-number mx-auto mb-3 d-flex align-items-center justify-content-center rounded-circle bg-primary text-white fw-bold fs-3" 
                   style={{ width: '60px', height: '60px' }}>
                3
              </div>
              <h5>Travel Happy</h5>
              <p className="text-muted">Get your e-ticket and enjoy your comfortable bus journey</p>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-5 bus-bg-primary text-white">
        <div className="container">
          <div className="text-center">
            <h2 className="display-5 fw-bold mb-3">Ready to Travel?</h2>
            <p className="lead mb-4">Book your bus tickets now and start your journey</p>
            {isAuthenticated ? (
              <div>
                <p className="mb-3">Welcome back, {user?.name}! Ready to book your next trip?</p>
                <Link to="/search" className="btn btn-warning btn-lg px-5">
                  Search Buses
                </Link>
              </div>
            ) : (
              <div>
                <Link to="/register" className="btn btn-warning btn-lg px-5 me-3">
                  Create Account
                </Link>
                <Link to="/login" className="btn btn-outline-light btn-lg px-5">
                  Login
                </Link>
              </div>
            )}
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-dark text-white py-4">
        <div className="container">
          <div className="row">
            <div className="col-md-6">
              <h5>🚌 BusReserve</h5>
              <p className="text-muted">Your trusted partner for comfortable bus travel</p>
            </div>
            <div className="col-md-6 text-md-end">
              <p className="text-muted mb-0">© 2024 BusReserve. All rights reserved.</p>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default Home;