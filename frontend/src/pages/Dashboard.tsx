import { useNavigate } from 'react-router-dom';

function Dashboard() {
  const navigate = useNavigate();
  const nombre = localStorage.getItem('nombre');
  const rol = localStorage.getItem('rol');

  const handleLogout = () => {
    localStorage.clear();
    navigate('/');
  };

  return (
    <div style={{ padding: '40px', fontFamily: 'sans-serif' }}>
      <h1 style={{ color: '#6b1e2b' }}>Bienvenido, {nombre}</h1>
      <p>Rol: {rol}</p>
      <button
        onClick={handleLogout}
        style={{
          marginTop: '20px',
          padding: '10px 20px',
          backgroundColor: '#6b1e2b',
          color: '#fff',
          border: 'none',
          borderRadius: '8px',
          cursor: 'pointer',
        }}
      >
        Cerrar sesión
      </button>
    </div>
  );
}

export default Dashboard;