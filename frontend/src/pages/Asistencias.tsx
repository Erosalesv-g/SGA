import { useEffect, useState } from 'react';
import apiClient from '../api/client';
import type { AsistenciaRequest, AsistenciaResponse } from '../types/asistencia';
import type { EstudianteResponse } from '../types/estudiante';
import type { MateriaResponse } from '../types/materia';
import type { DocenteResponse } from '../types/docente';
import type { UsuarioBasico } from '../types/comunicado';
import './Asistencias.css';

const ESTADOS: Record<string, string> = {
  P: 'Presente',
  A: 'Ausente',
  J: 'Justificado',
  T: 'Tardanza',
};

function Asistencias() {
  const rol = localStorage.getItem('rol') || '';
  const emailActual = localStorage.getItem('email') || '';
  const puedeEditar = rol === 'RECTOR' || rol === 'INSPECTOR' || rol === 'DOCENTE';

  const [asistencias, setAsistencias] = useState<AsistenciaResponse[]>([]);
  const [estudiantes, setEstudiantes] = useState<EstudianteResponse[]>([]);
  const [materias, setMaterias] = useState<MateriaResponse[]>([]);
  const [actorId, setActorId] = useState('');

  const [loading, setLoading] = useState(true);
  const [modalAbierto, setModalAbierto] = useState(false);
  const [error, setError] = useState('');
  const [guardando, setGuardando] = useState(false);

  const [form, setForm] = useState<AsistenciaRequest>({
    fecha: new Date().toISOString().slice(0, 10),
    estado: 'P',
    estudianteId: '',
    materiaId: '',
  });

  const cargarDatos = async () => {
    setLoading(true);
    try {
      const [estRes, matRes, usuRes] = await Promise.all([
        apiClient.get<EstudianteResponse[]>('/estudiantes'),
        apiClient.get<MateriaResponse[]>('/materias'),
        apiClient.get<UsuarioBasico[]>('/usuarios'),
      ]);
      setEstudiantes(estRes.data);
      setMaterias(matRes.data);

      const miUsuario = usuRes.data.find((u) => u.email === emailActual);
      if (miUsuario) setActorId(miUsuario.id);

      if (rol === 'ESTUDIANTE') {
        const yo = estRes.data.find((e) => e.email === emailActual);
        if (yo) {
          const asisRes = await apiClient.get<AsistenciaResponse[]>(`/asistencias/estudiante/${yo.id}`);
          setAsistencias(asisRes.data);
        } else {
          setAsistencias([]);
        }
      } else if (rol === 'DOCENTE') {
        const docRes = await apiClient.get<DocenteResponse[]>('/docentes');
        const yo = docRes.data.find((d) => d.email === emailActual);
        const misMateriaIds = matRes.data.filter((m) => m.docenteId === yo?.id).map((m) => m.id);
        const todasRes = await apiClient.get<AsistenciaResponse[]>('/asistencias');
        const deMisMaterias = todasRes.data.filter((a) => misMateriaIds.includes(a.materiaId));
        setAsistencias(deMisMaterias);
      } else if (rol === 'REPRESENTANTE') {
        const misEstudianteIds = estRes.data
          .filter((e) => e.representanteId === miUsuario?.id)
          .map((e) => e.id);
        const todasRes = await apiClient.get<AsistenciaResponse[]>('/asistencias');
        const deMisRepresentados = todasRes.data.filter((a) => misEstudianteIds.includes(a.estudianteId));
        setAsistencias(deMisRepresentados);
      } else {
        const asisRes = await apiClient.get<AsistenciaResponse[]>('/asistencias');
        setAsistencias(asisRes.data);
      }
    } catch {
      setError('No se pudieron cargar los datos');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    cargarDatos();
  }, []);

  const abrirNuevo = () => {
    setForm({
      fecha: new Date().toISOString().slice(0, 10),
      estado: 'P',
      estudianteId: estudiantes[0]?.id || '',
      materiaId: materias[0]?.id || '',
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
      await apiClient.post(`/asistencias?actorId=${actorId}`, form);
      await cargarDatos();
      setModalAbierto(false);
    } catch {
      setError('No se pudo guardar la asistencia. Revisa los datos.');
    } finally {
      setGuardando(false);
    }
  };

  const handleJustificar = async (id: string) => {
    try {
      await apiClient.patch(`/asistencias/${id}/justificar?actorId=${actorId}`);
      await cargarDatos();
    } catch {
      setError('No se pudo justificar la asistencia');
    }
  };

  const handleEliminar = async (id: string) => {
    if (!confirm('¿Eliminar este registro de asistencia?')) return;
    try {
      await apiClient.delete(`/asistencias/${id}?actorId=${actorId}`);
      await cargarDatos();
    } catch {
      setError('No se pudo eliminar el registro');
    }
  };

  return (
    <div className="asistencias-container">
      <div className="asistencias-header">
        <h1>Asistencia</h1>
        {puedeEditar && (
          <button className="btn-nuevo" onClick={abrirNuevo} disabled={estudiantes.length === 0 || materias.length === 0}>
            + Nuevo registro
          </button>
        )}
      </div>

      <div className="asistencias-table-wrapper">
        {loading ? (
          <div className="asistencias-cargando">Cargando asistencias...</div>
        ) : asistencias.length === 0 ? (
          <div className="asistencias-vacio">No hay registros de asistencia todavía.</div>
        ) : (
          <table className="asistencias-table">
            <thead>
              <tr>
                <th>Estudiante</th>
                <th>Materia</th>
                <th>Estado</th>
                <th>Fecha</th>
                {puedeEditar && <th>Acciones</th>}
              </tr>
            </thead>
            <tbody>
              {asistencias.map((a) => (
                <tr key={a.id}>
                  <td>{a.estudianteNombre}</td>
                  <td>{a.materiaNombre}</td>
                  <td>
                    <span className={`estado-badge estado-${a.estado}`}>
                      {ESTADOS[a.estado] || a.estado}
                    </span>
                  </td>
                  <td>{a.fecha}</td>
                  {puedeEditar && (
                    <td>
                      <div className="tabla-acciones">
                        {a.estado === 'A' && (
                          <button className="btn-accion btn-justificar" onClick={() => handleJustificar(a.id)}>
                            Justificar
                          </button>
                        )}
                        <button className="btn-accion btn-desactivar" onClick={() => handleEliminar(a.id)}>
                          Eliminar
                        </button>
                      </div>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {modalAbierto && (
        <div className="modal-overlay" onClick={cerrarModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>Nuevo registro de asistencia</h2>

            {error && <p className="modal-error">{error}</p>}

            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Estudiante</label>
                <select name="estudianteId" value={form.estudianteId} onChange={handleChange} required>
                  {estudiantes.map((e) => (
                    <option key={e.id} value={e.id}>{e.nombre}</option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label>Materia</label>
                <select name="materiaId" value={form.materiaId} onChange={handleChange} required>
                  {materias.map((m) => (
                    <option key={m.id} value={m.id}>{m.nombre}</option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label>Estado</label>
                <select name="estado" value={form.estado} onChange={handleChange} required>
                  {Object.entries(ESTADOS).map(([codigo, label]) => (
                    <option key={codigo} value={codigo}>{label}</option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label>Fecha</label>
                <input
                  type="date"
                  name="fecha"
                  value={form.fecha}
                  onChange={handleChange}
                  required
                />
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

export default Asistencias;