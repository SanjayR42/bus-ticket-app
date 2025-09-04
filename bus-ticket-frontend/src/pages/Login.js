import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import { useAuth } from '../context/AuthContext';
import { authAPI } from '../services/api';
//import { mockAuthAPI as authAPI } from '../services/mockApi';
const Login = () => {
  const { login } = useAuth();
  const navigate = useNavigate();

  // Validation schema
  const validationSchema = Yup.object({
    email: Yup.string()
      .email('Invalid email address')
      .required('Email is required'),
    password: Yup.string()
      .min(6, 'Password must be at least 6 characters')
      .required('Password is required')
  });

  // Formik setup
  const formik = useFormik({
    initialValues: {
      email: '',
      password: ''
    },
    validationSchema,
    onSubmit: async (values, { setSubmitting, setErrors }) => {
      try {
        console.log('Attempting login with:', values);
        
        const response = await authAPI.login(values);
        console.log('Login response:', response);
        
        // Check if response and response.data exist
        if (!response || !response.data) {
          throw new Error('No response from server');
        }

        // Handle different response structures
        const responseData = response.data;
        
        // Extract data with fallbacks for different response structures
        const token = responseData.token || responseData.access_token;
        const userId = responseData.userId || responseData.id || responseData.user?.id;
        const name = responseData.name || responseData.user?.name || 'User';
        const role = responseData.role || responseData.user?.role || 'CUSTOMER';

        if (!token) {
          throw new Error('No authentication token received');
        }

        login({ id: userId, name, email: values.email, role }, token);
        navigate(role === 'ADMIN' ? '/admin' : '/dashboard');
        
      } catch (error) {
        console.error('Login error details:', error);
        
        // Handle different error scenarios
        if (error.response?.status === 500) {
          setErrors({ 
            submit: 'Server error. Please try again later or contact support.' 
          });
        } else if (error.response?.data?.error) {
          setErrors({ 
            submit: error.response.data.error 
          });
        } else if (error.message) {
          setErrors({ 
            submit: error.message 
          });
        } else {
          setErrors({ 
            submit: 'Login failed. Please check your credentials and try again.' 
          });
        }
      } finally {
        setSubmitting(false);
      }
    }
  });

  return (
    <div className="container mt-5">
      <div className="row justify-content-center">
        <div className="col-md-6">
          <div className="card">
            <div className="card-body">
              <h2 className="card-title text-center">Login</h2>
              
              {formik.errors.submit && (
                <div className="alert alert-danger">{formik.errors.submit}</div>
              )}
              
              <form onSubmit={formik.handleSubmit}>
                <div className="mb-3">
                  <label className="form-label">Email</label>
                  <input
                    type="email"
                    className={`form-control ${
                      formik.touched.email && formik.errors.email ? 'is-invalid' : ''
                    }`}
                    name="email"
                    value={formik.values.email}
                    onChange={formik.handleChange}
                    onBlur={formik.handleBlur}
                  />
                  {formik.touched.email && formik.errors.email ? (
                    <div className="invalid-feedback">{formik.errors.email}</div>
                  ) : null}
                </div>
                
                <div className="mb-3">
                  <label className="form-label">Password</label>
                  <input
                    type="password"
                    className={`form-control ${
                      formik.touched.password && formik.errors.password ? 'is-invalid' : ''
                    }`}
                    name="password"
                    value={formik.values.password}
                    onChange={formik.handleChange}
                    onBlur={formik.handleBlur}
                  />
                  {formik.touched.password && formik.errors.password ? (
                    <div className="invalid-feedback">{formik.errors.password}</div>
                  ) : null}
                </div>
                
                <button 
                  type="submit" 
                  className="btn btn-primary w-100"
                  disabled={formik.isSubmitting}
                >
                  {formik.isSubmitting ? 'Logging in...' : 'Login'}
                </button>
              </form>
              
              <div className="text-center mt-3">
                <p>Don't have an account? <Link to="/register">Register here</Link></p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;