import Usuarios from './Usuarios';
import Reportes from './Reportes';
import Comunicados from './Comunicados';
import Horarios from './Horarios';
import Asistencias from './Asistencias';
import Calificaciones from './Calificaciones';
import Docentes from './Docentes';
import Estudiantes from './Estudiantes';
import { useNavigate } from 'react-router-dom';
import { useState } from 'react';
import logo from '../assets/logo2.png';
import './Dashboard.css';

interface MenuItem {
  label: string;
  key: string;
}

const MENU_BY_ROLE: Record<string, MenuItem[]> = {
  RECTOR: [
    { label: 'Usuarios', key: 'usuarios' },
    { label: 'Estudiantes', key: 'estudiantes' },
    { label: 'Docentes', key: 'docentes' },
    { label: 'Calificaciones', key: 'calificaciones' },
    { label: 'Asistencia', key: 'asistencia' },
    { label: 'Horarios', key: 'horarios' },
    { label: 'Comunicados', key: 'comunicados' },
    { label: 'Reportes', key: 'reportes' },
  ],
  INSPECTOR: [
    { label: 'Estudiantes', key: 'estudiantes' },
    { label: 'Asistencia', key: 'asistencia' },
    { label: 'Comunicados', key: 'comunicados' },
  ],
  DOCENTE: [
    { label: 'Calificaciones', key: 'calificaciones' },
    { label: 'Asistencia', key: 'asistencia' },
    { label: 'Horarios', key: 'horarios' },
    { label: 'Comunicados', key: 'comunicados' },
  ],
  ESTUDIANTE: [
    { label: 'Calificaciones', key: 'calificaciones' },
    { label: 'Asistencia', key: 'asistencia' },
    { label: 'Horarios', key: 'horarios' },
    { label: 'Comunicados', key: 'comunicados' },
    { label: 'Reportes', key: 'reportes' },
  ],
  REPRESENTANTE: [
    { label: 'Calificaciones', key: 'calificaciones' },
    { label: 'Asistencia', key: 'asistencia' },
    { label: 'Comunicados', key: 'comunicados' },
    { label: 'Reportes', key: 'reportes' },
  ],
  ORIENTADOR: [
    { label: 'Estudiantes', key: 'estudiantes' },
    { label: 'Asistencia', key: 'asistencia' },
    { label: 'Comunicados', key: 'comunicados' },
    { label: 'Reportes', key: 'reportes' },
  ],
};

function Dashboard() {
  const navigate = useNavigate();
  const [activeKey, setActiveKey] = useState('inicio');
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const nombre = localStorage.getItem('nombre') || 'Usuario';
  const rol = localStorage.getItem('rol') || '';

  const menuItems = MENU_BY_ROLE[rol] || [];

  const handleLogout = () => {
    localStorage.clear();
    navigate('/');
  };

  return (
    <div className="dashboard-layout">
      <aside className={`dashboard-sidebar ${sidebarOpen ? 'open' : ''}`}>
        <div className="sidebar-brand">
          <img src={logo} alt="Logo" className="sidebar-logo" />
          <span className="sidebar-brand-text">
            UNIDAD EDUCATIVA<br />FISCAL "DURÁN"
          </span>
        </div>

        <nav className="sidebar-nav">
          <button
            className={`sidebar-link ${activeKey === 'inicio' ? 'active' : ''}`}
            onClick={() => { setActiveKey('inicio'); setSidebarOpen(false); }}
          >
            Inicio
          </button>

          {menuItems.map((item) => (
            <button
              key={item.key}
              className={`sidebar-link ${activeKey === item.key ? 'active' : ''}`}
              onClick={() => { setActiveKey(item.key); setSidebarOpen(false); }}
            >
              {item.label}
            </button>
          ))}
        </nav>

        <div className="sidebar-footer">
          <div className="sidebar-user">
            <span className="sidebar-user-name">{nombre}</span>
            <span className="sidebar-user-role">{rol}</span>
          </div>
          <button className="sidebar-logout" onClick={handleLogout}>
            Cerrar sesión
          </button>
        </div>
      </aside>

      {sidebarOpen && <div className="sidebar-overlay" onClick={() => setSidebarOpen(false)} />}

      <main className="dashboard-main">
        <button className="mobile-menu-btn" onClick={() => setSidebarOpen(true)} aria-label="Abrir menú">
          ☰
        </button>

        {activeKey === 'inicio' ? (
          <div className="dashboard-welcome">
            <h1>Bienvenido/a, {nombre}</h1>
            <p>Rol: {rol}</p>
          </div>
        ) : activeKey === 'estudiantes' ? (
          <Estudiantes />
        ) : activeKey === 'docentes' ? (
          <Docentes />
        ) : activeKey === 'calificaciones' ? (
          <Calificaciones />
        ) : activeKey === 'asistencia' ? (
          <Asistencias />
        ) : activeKey === 'horarios' ? (
          <Horarios />
        ) : activeKey === 'comunicados' ? (
          <Comunicados />
        ) : activeKey === 'reportes' ? (
          <Reportes />
        ) : activeKey === 'usuarios' ? (
          <Usuarios />
        ) : (
          <div className="dashboard-welcome">
            <h1>{menuItems.find((m) => m.key === activeKey)?.label}</h1>
            <div className="dashboard-placeholder">
              Este módulo todavía está en construcción.
            </div>
          </div>
        )}
      </main>
    </div>
  );
}

export default Dashboard;