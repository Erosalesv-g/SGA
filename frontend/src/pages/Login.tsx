import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import apiClient from '../api/client';
import type { LoginRequest, LoginResponse } from '../types/auth';
import logo from '../assets/logo2.png';
import ilustracion from '../assets/ilustracion.png';
import './Login.css';

function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const body: LoginRequest = { email, password };
      const response = await apiClient.post<LoginResponse>('/auth/login', body);

      localStorage.setItem('accessToken', response.data.accessToken);
      localStorage.setItem('refreshToken', response.data.refreshToken);
      localStorage.setItem('email', response.data.email);
      localStorage.setItem('rol', response.data.rol);
      localStorage.setItem('nombre', response.data.nombre);

      navigate('/dashboard');
    } catch {
      setError('Credenciales inválidas');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-card-split">
        <div className="login-left">
          <div className="login-rings" aria-hidden="true">
            <svg viewBox="0 0 400 400" width="100%" height="100%" preserveAspectRatio="xMidYMax slice">
              <circle cx="60" cy="340" r="90" fill="none" stroke="#6b1e2b" strokeOpacity="0.1" />
              <circle cx="60" cy="340" r="150" fill="none" stroke="#6b1e2b" strokeOpacity="0.08" />
              <circle cx="60" cy="340" r="210" fill="none" stroke="#6b1e2b" strokeOpacity="0.06" />
              <circle cx="60" cy="340" r="270" fill="none" stroke="#6b1e2b" strokeOpacity="0.04" />
            </svg>
          </div>

          <div className="login-brand">
            <img src={logo} alt="Unidad Educativa Fiscal Durán" className="login-logo-small" />
            <span className="login-brand-text">
              UNIDAD EDUCATIVA<br />FISCAL "DURÁN"
            </span>
          </div>

          <img src={ilustracion} alt="" className="login-illustration" />
        </div>

        <div className="login-right">
          <h1 className="login-welcome">¡Bienvenido/a a tu plataforma académica!</h1>
          <p className="login-subtext">Por favor, inicia sesión con tus credenciales.</p>

          <form onSubmit={handleSubmit}>
            <label className="login-label">
              <svg className="login-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="12" cy="8" r="4" />
                <path d="M4 20c0-4 3.6-7 8-7s8 3 8 7" />
              </svg>
              Usuario
            </label>
            <input
              type="email"
              className="login-input"
              placeholder="Ingrese su usuario"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />

            <label className="login-label">
              <svg className="login-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <rect x="5" y="11" width="14" height="9" rx="2" />
                <path d="M8 11V8a4 4 0 0 1 8 0v3" />
              </svg>
              Contraseña
            </label>
            <div className="login-password-wrapper">
              <input
                type={showPassword ? 'text' : 'password'}
                className="login-input"
                placeholder="Ingrese su contraseña"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
              <button
                type="button"
                className="login-eye-button"
                onClick={() => setShowPassword(!showPassword)}
                aria-label={showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}
              >
                {showPassword ? (
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M3 3l18 18" />
                    <path d="M9.5 5.5A10.5 10.5 0 0 1 12 5c5 0 9 4 9 7 0 1.1-.5 2.3-1.3 3.4M6.3 6.3C4 7.8 2 10 2 12c0 3 4 7 9 7 1 0 2-.2 2.9-.5" />
                  </svg>
                ) : (
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M2 12s4-7 10-7 10 7 10 7-4 7-10 7-10-7-10-7Z" />
                    <circle cx="12" cy="12" r="3" />
                  </svg>
                )}
              </button>
            </div>

            {error && <p className="login-error">{error}</p>}

            <a href="#" className="login-forgot">¿Olvidaste tu contraseña?</a>

            <button type="submit" className="login-button" disabled={loading}>
              {loading ? 'Ingresando...' : 'Iniciar sesión'}
            </button>
          </form>

          <p className="login-footer">© Unidad Educativa Fiscal Duran - 2026</p>
        </div>
      </div>
    </div>
  );
}

export default Login;