import { useEffect, useState } from 'react';
import apiClient from '../api/client';
import type { EstudianteRequest, EstudianteResponse } from '../types/estudiante';
import type { UsuarioBasico } from '../types/comunicado';
import './Estudiantes.css';

const NIVELES = ['1ro', '2do', '3ro', '4to', '5to', '6to', '7mo', '8vo', '9no', '10mo', '1ro Bach', '2do Bach', '3ro Bach'];

function Estudiantes() {
  const [estudiantes, setEstudiantes] = useState<EstudianteResponse[]>([]);
  const [representantes, setRepresentantes] = useState<UsuarioBasico[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalAbierto, setModalAbierto] = useState(false);
  const [editandoId, setEditandoId] = useState<string | null>(null);
  const [error, setError] = useState('');
  const [guardando, setGuardando] = useState(false);

  const [form, setForm] = useState<EstudianteRequest>({
    nombre: '',
    email: '',
    password: '',
    codigo: '',
    nivel: '',
    seccion: '',
    representanteId: null,
  });

  const cargarEstudiantes = async () => {
    setLoading(true);
    try {
      const [estRes, usuRes] = await Promise.all([
        apiClient.get<EstudianteResponse[]>('/estudiantes'),
        apiClient.get<UsuarioBasico[]>('/usuarios'),
      ]);
      setEstudiantes(estRes.data);
      setRepresentantes(usuRes.data.filter((u) => u.rol === 'REPRESENTANTE'));
    } catch {
      setError('No se pudieron cargar los estudiantes');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    cargarEstudiantes();
  }, []);

  const abrirNuevo = () => {
    setEditandoId(null);
    setForm({ nombre: '', email: '', password: '', codigo: '', nivel: '', seccion: '', representanteId: null });
    setError('');
    setModalAbierto(true);
  };

  const abrirEditar = (estudiante: EstudianteResponse) => {
    setEditandoId(estudiante.id);
    setForm({
      nombre: estudiante.nombre,
      email: estudiante.email,
      password: '',
      codigo: estudiante.codigo,
      nivel: estudiante.nivel,
      seccion: estudiante.seccion,
      representanteId: estudiante.representanteId,
    });
    setError('');
    setModalAbierto(true);
  };

  const cerrarModal = () => {
    setModalAbierto(false);
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setForm({ ...form, [name]: name === 'representanteId' && value === '' ? null : value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setGuardando(true);

    try {
      if (editandoId) {
        await apiClient.put(`/estudiantes/${editandoId}`, form);
      } else {
        await apiClient.post('/estudiantes', form);
      }
      await cargarEstudiantes();
      setModalAbierto(false);
    } catch {
      setError('No se pudo guardar el estudiante. Revisa los datos.');
    } finally {
      setGuardando(false);
    }
  };

  const handleDesactivar = async (id: string) => {
    if (!confirm('¿Desactivar este estudiante?')) return;
    try {
      await apiClient.delete(`/estudiantes/${id}`);
      await cargarEstudiantes();
    } catch {
      setError('No se pudo desactivar el estudiante');
    }
  };

  const handleActivar = async (id: string) => {
    try {
      await apiClient.patch(`/estudiantes/${id}/activar`);
      await cargarEstudiantes();
    } catch {
      setError('No se pudo activar el estudiante');
    }
  };

  return (
    <div className="estudiantes-container">
      <div className="estudiantes-header">
        <h1>Estudiantes</h1>
        <button className="btn-nuevo" onClick={abrirNuevo}>
          + Nuevo estudiante
        </button>
      </div>

      <div className="estudiantes-table-wrapper">
        {loading ? (
          <div className="estudiantes-cargando">Cargando estudiantes...</div>
        ) : estudiantes.length === 0 ? (
          <div className="estudiantes-vacio">No hay estudiantes registrados todavía.</div>
        ) : (
          <table className="estudiantes-table">
            <thead>
              <tr>
                <th>Código</th>
                <th>Nombre</th>
                <th>Email</th>
                <th>Nivel</th>
                <th>Sección</th>
                <th>Representante</th>
                <th>Estado</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {estudiantes.map((est) => (
                <tr key={est.id}>
                  <td>{est.codigo}</td>
                  <td>{est.nombre}</td>
                  <td>{est.email}</td>
                  <td>{est.nivel}</td>
                  <td>{est.seccion}</td>
                  <td>{est.representanteNombre || '—'}</td>
                  <td>
                    <span className={`badge ${est.activo ? 'badge-activo' : 'badge-inactivo'}`}>
                      {est.activo ? 'Activo' : 'Inactivo'}
                    </span>
                  </td>
                  <td>
                    <div className="tabla-acciones">
                      <button className="btn-accion btn-editar" onClick={() => abrirEditar(est)}>
                        Editar
                      </button>
                      {est.activo ? (
                        <button className="btn-accion btn-desactivar" onClick={() => handleDesactivar(est.id)}>
                          Desactivar
                        </button>
                      ) : (
                        <button className="btn-accion btn-editar" onClick={() => handleActivar(est.id)}>
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
            <h2>{editandoId ? 'Editar estudiante' : 'Nuevo estudiante'}</h2>

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
                <label>Código de estudiante</label>
                <input name="codigo" value={form.codigo} onChange={handleChange} required />
              </div>

              <div className="form-group">
                <label>Nivel</label>
                <input
                  name="nivel"
                  value={form.nivel}
                  onChange={handleChange}
                  list="niveles-lista"
                  required
                  placeholder="Ej: 10mo"
                />
                <datalist id="niveles-lista">
                  {NIVELES.map((n) => (
                    <option key={n} value={n} />
                  ))}
                </datalist>
              </div>

              <div className="form-group">
                <label>Sección</label>
                <input name="seccion" value={form.seccion} onChange={handleChange} required placeholder="Ej: A" />
              </div>

              <div className="form-group">
                <label>Representante (opcional)</label>
                <select name="representanteId" value={form.representanteId || ''} onChange={handleChange}>
                  <option value="">Sin representante asignado</option>
                  {representantes.map((r) => (
                    <option key={r.id} value={r.id}>{r.nombre} ({r.email})</option>
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

export default Estudiantes;