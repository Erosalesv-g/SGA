import { useEffect, useState } from 'react';
import apiClient from '../api/client';
import type { DocenteRequest, DocenteResponse } from '../types/docente';
import './Docentes.css';

function Docentes() {
  const [docentes, setDocentes] = useState<DocenteResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalAbierto, setModalAbierto] = useState(false);
  const [editandoId, setEditandoId] = useState<string | null>(null);
  const [error, setError] = useState('');
  const [guardando, setGuardando] = useState(false);

  const [form, setForm] = useState<DocenteRequest>({
    nombre: '',
    email: '',
    password: '',
    cedula: '',
    titulo: '',
    especialidad: '',
  });

  const cargarDocentes = async () => {
    setLoading(true);
    try {
      const response = await apiClient.get<DocenteResponse[]>('/docentes');
      setDocentes(response.data);
    } catch {
      setError('No se pudieron cargar los docentes');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    cargarDocentes();
  }, []);

  const abrirNuevo = () => {
    setEditandoId(null);
    setForm({ nombre: '', email: '', password: '', cedula: '', titulo: '', especialidad: '' });
    setError('');
    setModalAbierto(true);
  };

  const abrirEditar = (docente: DocenteResponse) => {
    setEditandoId(docente.id);
    setForm({
      nombre: docente.nombre,
      email: docente.email,
      password: '',
      cedula: docente.cedula,
      titulo: docente.titulo,
      especialidad: docente.especialidad,
    });
    setError('');
    setModalAbierto(true);
  };

  const cerrarModal = () => setModalAbierto(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setGuardando(true);

    try {
      if (editandoId) {
        await apiClient.put(`/docentes/${editandoId}`, form);
      } else {
        await apiClient.post('/docentes', form);
      }
      await cargarDocentes();
      setModalAbierto(false);
    } catch {
      setError('No se pudo guardar el docente. Revisa los datos.');
    } finally {
      setGuardando(false);
    }
  };

  const handleDesactivar = async (id: string) => {
    if (!confirm('¿Desactivar este docente?')) return;
    try {
      await apiClient.delete(`/docentes/${id}`);
      await cargarDocentes();
    } catch {
      setError('No se pudo desactivar el docente');
    }
  };

  const handleActivar = async (id: string) => {
    try {
      await apiClient.patch(`/docentes/${id}/activar`);
      await cargarDocentes();
    } catch {
      setError('No se pudo activar el docente');
    }
  };

  return (
    <div className="docentes-container">
      <div className="docentes-header">
        <h1>Docentes</h1>
        <button className="btn-nuevo" onClick={abrirNuevo}>
          + Nuevo docente
        </button>
      </div>

      <div className="docentes-table-wrapper">
        {loading ? (
          <div className="docentes-cargando">Cargando docentes...</div>
        ) : docentes.length === 0 ? (
          <div className="docentes-vacio">No hay docentes registrados todavía.</div>
        ) : (
          <table className="docentes-table">
            <thead>
              <tr>
                <th>Cédula</th>
                <th>Nombre</th>
                <th>Email</th>
                <th>Título</th>
                <th>Especialidad</th>
                <th>Estado</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {docentes.map((doc) => (
                <tr key={doc.id}>
                  <td>{doc.cedula}</td>
                  <td>{doc.nombre}</td>
                  <td>{doc.email}</td>
                  <td>{doc.titulo}</td>
                  <td>{doc.especialidad}</td>
                  <td>
                    <span className={`badge ${doc.activo ? 'badge-activo' : 'badge-inactivo'}`}>
                      {doc.activo ? 'Activo' : 'Inactivo'}
                    </span>
                  </td>
                  <td>
                    <div className="tabla-acciones">
                      <button className="btn-accion btn-editar" onClick={() => abrirEditar(doc)}>
                        Editar
                      </button>
                      {doc.activo ? (
                        <button className="btn-accion btn-desactivar" onClick={() => handleDesactivar(doc.id)}>
                          Desactivar
                        </button>
                      ) : (
                        <button className="btn-accion btn-editar" onClick={() => handleActivar(doc.id)}>
                          Activar
                        </button>
                      )}
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
            <h2>{editandoId ? 'Editar docente' : 'Nuevo docente'}</h2>

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
                <label>Cédula</label>
                <input name="cedula" value={form.cedula} onChange={handleChange} required />
              </div>

              <div className="form-group">
                <label>Título académico</label>
                <input name="titulo" value={form.titulo} onChange={handleChange} required placeholder="Ej: Lic. en Educación" />
              </div>

              <div className="form-group">
                <label>Especialidad</label>
                <input name="especialidad" value={form.especialidad} onChange={handleChange} required placeholder="Ej: Matemáticas" />
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

export default Docentes;