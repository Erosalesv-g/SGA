import { useEffect, useState } from 'react';
import apiClient from '../api/client';
import type { UsuarioRequest, UsuarioResponse } from '../types/usuario';
import './Usuarios.css';

const ROLES = ['RECTOR', 'INSPECTOR', 'DOCENTE', 'ESTUDIANTE', 'REPRESENTANTE', 'ORIENTADOR'];

function Usuarios() {
  const [usuarios, setUsuarios] = useState<UsuarioResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalAbierto, setModalAbierto] = useState(false);
  const [editandoId, setEditandoId] = useState<string | null>(null);
  const [error, setError] = useState('');
  const [guardando, setGuardando] = useState(false);

  const [form, setForm] = useState<UsuarioRequest>({
    nombre: '',
    email: '',
    password: '',
    rol: 'DOCENTE',
  });

  const cargarUsuarios = async () => {
    setLoading(true);
    try {
      const response = await apiClient.get<UsuarioResponse[]>('/usuarios');
      setUsuarios(response.data);
    } catch {
      setError('No se pudieron cargar los usuarios');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    cargarUsuarios();
  }, []);

  const abrirNuevo = () => {
    setEditandoId(null);
    setForm({ nombre: '', email: '', password: '', rol: 'DOCENTE' });
    setError('');
    setModalAbierto(true);
  };

  const abrirEditar = (usuario: UsuarioResponse) => {
    setEditandoId(usuario.id);
    setForm({
      nombre: usuario.nombre,
      email: usuario.email,
      password: '',
      rol: usuario.rol,
    });
    setError('');
    setModalAbierto(true);
  };

  const cerrarModal = () => setModalAbierto(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setGuardando(true);

    try {
      if (editandoId) {
        await apiClient.put(`/usuarios/${editandoId}`, form);
      } else {
        await apiClient.post('/usuarios', form);
      }
      await cargarUsuarios();
      setModalAbierto(false);
    } catch {
      setError('No se pudo guardar el usuario. Revisa los datos.');
    } finally {
      setGuardando(false);
    }
  };

  const handleDesactivar = async (id: string) => {
    if (!confirm('¿Desactivar este usuario?')) return;
    try {
      await apiClient.delete(`/usuarios/${id}`);
      await cargarUsuarios();
    } catch {
      setError('No se pudo desactivar el usuario');
    }
  };

  return (
    <div className="usuarios-container">
      <div className="usuarios-header">
        <h1>Usuarios</h1>
        <button className="btn-nuevo" onClick={abrirNuevo}>
          + Nuevo usuario
        </button>
      </div>

      <div className="usuarios-table-wrapper">
        {loading ? (
          <div className="usuarios-cargando">Cargando usuarios...</div>
        ) : usuarios.length === 0 ? (
          <div className="usuarios-vacio">No hay usuarios registrados todavía.</div>
        ) : (
          <table className="usuarios-table">
            <thead>
              <tr>
                <th>Nombre</th>
                <th>Email</th>
                <th>Rol</th>
                <th>Estado</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {usuarios.map((u) => (
                <tr key={u.id}>
                  <td>{u.nombre}</td>
                  <td>{u.email}</td>
                  <td>{u.rol}</td>
                  <td>
                    <span className={`badge ${u.activo ? 'badge-activo' : 'badge-inactivo'}`}>
                      {u.activo ? 'Activo' : 'Inactivo'}
                    </span>
                  </td>
                  <td>
                    <div className="tabla-acciones">
                      <button className="btn-accion btn-editar" onClick={() => abrirEditar(u)}>
                        Editar
                      </button>
                      <button className="btn-accion btn-desactivar" onClick={() => handleDesactivar(u.id)}>
                        Desactivar
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {modalAbierto && (
        <div className="modal-overlay" onClick={cerrarModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>{editandoId ? 'Editar usuario' : 'Nuevo usuario'}</h2>

            {error && <p className="modal-error">{error}</p>}

            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Nombre completo</label>
                <input name="nombre" value={form.nombre} onChange={handleChange} required />
              </div>

              <div className="form-group">
                <label>Email</label>
                <input type="email" name="email" value={form.email} onChange={handleChange} required />
              </div>

              <div className="form-group">
                <label>Contraseña {editandoId && '(dejar vacío para no cambiarla)'}</label>
                <input
                  type="password"
                  name="password"
                  value={form.password}
                  onChange={handleChange}
                  required={!editandoId}
                />
              </div>

              <div className="form-group">
                <label>Rol</label>
                <select name="rol" value={form.rol} onChange={handleChange} required>
                  {ROLES.map((r) => (
                    <option key={r} value={r}>{r}</option>
                  ))}
                </select>
              </div>

              <div className="modal-actions">
                <button type="button" className="btn-cancelar" onClick={cerrarModal}>
                  Cancelar
                </button>
                <button type="submit" className="btn-guardar" disabled={guardando}>
                  {guardando ? 'Guardando...' : 'Guardar'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default Usuarios;