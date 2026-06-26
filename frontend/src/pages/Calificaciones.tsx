import { useEffect, useState } from 'react';
import apiClient from '../api/client';
import type { CalificacionRequest, CalificacionResponse } from '../types/calificacion';
import type { EstudianteResponse } from '../types/estudiante';
import type { DocenteResponse } from '../types/docente';
import type { MateriaResponse } from '../types/materia';
import type { UsuarioBasico } from '../types/comunicado';
import './Calificaciones.css';

const TIPOS = ['PARCIAL', 'EXAMEN', 'TAREA', 'PROYECTO'];

function Calificaciones() {
  const rol = localStorage.getItem('rol') || '';
  const emailActual = localStorage.getItem('email') || '';
  const puedeEditar = rol === 'RECTOR' || rol === 'DOCENTE';

  const [calificaciones, setCalificaciones] = useState<CalificacionResponse[]>([]);
  const [estudiantes, setEstudiantes] = useState<EstudianteResponse[]>([]);
  const [docentes, setDocentes] = useState<DocenteResponse[]>([]);
  const [materias, setMaterias] = useState<MateriaResponse[]>([]);
  const [actorId, setActorId] = useState('');

  const [loading, setLoading] = useState(true);
  const [modalAbierto, setModalAbierto] = useState(false);
  const [editandoId, setEditandoId] = useState<string | null>(null);
  const [error, setError] = useState('');
  const [guardando, setGuardando] = useState(false);

  const [form, setForm] = useState<CalificacionRequest>({
    valor: 0,
    tipo: 'PARCIAL',
    fechaRegistro: new Date().toISOString().slice(0, 10),
    estudianteId: '',
    materiaId: '',
    docenteId: '',
  });

  const cargarDatos = async () => {
    setLoading(true);
    try {
      const [estRes, docRes, matRes, usuRes] = await Promise.all([
        apiClient.get<EstudianteResponse[]>('/estudiantes'),
        apiClient.get<DocenteResponse[]>('/docentes'),
        apiClient.get<MateriaResponse[]>('/materias'),
        apiClient.get<UsuarioBasico[]>('/usuarios'),
      ]);
      setEstudiantes(estRes.data);
      setDocentes(docRes.data);
      setMaterias(matRes.data);

      const miUsuario = usuRes.data.find((u) => u.email === emailActual);
      if (miUsuario) setActorId(miUsuario.id);

      if (rol === 'ESTUDIANTE') {
        const yo = estRes.data.find((e) => e.email === emailActual);
        if (yo) {
          const califRes = await apiClient.get<CalificacionResponse[]>(`/calificaciones/estudiante/${yo.id}`);
          setCalificaciones(califRes.data);
        } else {
          setCalificaciones([]);
        }
      } else if (rol === 'DOCENTE') {
        const yo = docRes.data.find((d) => d.email === emailActual);
        const todasRes = await apiClient.get<CalificacionResponse[]>('/calificaciones');
        const propias = yo ? todasRes.data.filter((c) => c.docenteId === yo.id) : [];
        setCalificaciones(propias);
      } else if (rol === 'REPRESENTANTE') {
        const misEstudianteIds = estRes.data
          .filter((e) => e.representanteId === miUsuario?.id)
          .map((e) => e.id);
        const todasRes = await apiClient.get<CalificacionResponse[]>('/calificaciones');
        const deMisRepresentados = todasRes.data.filter((c) => misEstudianteIds.includes(c.estudianteId));
        setCalificaciones(deMisRepresentados);
      } else {
        const todasRes = await apiClient.get<CalificacionResponse[]>('/calificaciones');
        setCalificaciones(todasRes.data);
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
    setEditandoId(null);
    setForm({
      valor: 0,
      tipo: 'PARCIAL',
      fechaRegistro: new Date().toISOString().slice(0, 10),
      estudianteId: estudiantes[0]?.id || '',
      materiaId: materias[0]?.id || '',
      docenteId: docentes[0]?.id || '',
    });
    setError('');
    setModalAbierto(true);
  };

  const abrirEditar = (calif: CalificacionResponse) => {
    setEditandoId(calif.id);
    setForm({
      valor: calif.valor,
      tipo: calif.tipo,
      fechaRegistro: calif.fechaRegistro,
      estudianteId: calif.estudianteId,
      materiaId: calif.materiaId,
      docenteId: calif.docenteId,
    });
    setError('');
    setModalAbierto(true);
  };

  const cerrarModal = () => setModalAbierto(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setForm({ ...form, [name]: name === 'valor' ? parseFloat(value) : value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setGuardando(true);

    try {
      if (editandoId) {
        await apiClient.put(`/calificaciones/${editandoId}?actorId=${actorId}`, form);
      } else {
        await apiClient.post(`/calificaciones?actorId=${actorId}`, form);
      }
      await cargarDatos();
      setModalAbierto(false);
    } catch {
      setError('No se pudo guardar la calificación. Revisa los datos.');
    } finally {
      setGuardando(false);
    }
  };

  const handleEliminar = async (id: string) => {
    if (!confirm('¿Eliminar esta calificación?')) return;
    try {
      await apiClient.delete(`/calificaciones/${id}?actorId=${actorId}`);
      await cargarDatos();
    } catch {
      setError('No se pudo eliminar la calificación');
    }
  };

  return (
    <div className="calificaciones-container">
      <div className="calificaciones-header">
        <h1>Calificaciones</h1>
        {puedeEditar && (
          <button className="btn-nuevo" onClick={abrirNuevo} disabled={estudiantes.length === 0 || materias.length === 0}>
            + Nueva calificación
          </button>
        )}
      </div>

      <div className="calificaciones-table-wrapper">
        {loading ? (
          <div className="calificaciones-cargando">Cargando calificaciones...</div>
        ) : calificaciones.length === 0 ? (
          <div className="calificaciones-vacio">No hay calificaciones registradas todavía.</div>
        ) : (
          <table className="calificaciones-table">
            <thead>
              <tr>
                <th>Estudiante</th>
                <th>Materia</th>
                <th>Docente</th>
                <th>Tipo</th>
                <th>Nota</th>
                <th>Fecha</th>
                {puedeEditar && <th>Acciones</th>}
              </tr>
            </thead>
            <tbody>
              {calificaciones.map((c) => (
                <tr key={c.id}>
                  <td>{c.estudianteNombre}</td>
                  <td>{c.materiaNombre}</td>
                  <td>{c.docenteNombre}</td>
                  <td>{c.tipo}</td>
                  <td>
                    <span className={`valor-nota ${c.valor >= 7 ? 'valor-aprobado' : 'valor-reprobado'}`}>
                      {c.valor.toFixed(2)}
                    </span>
                  </td>
                  <td>{c.fechaRegistro}</td>
                  {puedeEditar && (
                    <td>
                      <div className="tabla-acciones">
                        <button className="btn-accion btn-editar" onClick={() => abrirEditar(c)}>
                          Editar
                        </button>
                        <button className="btn-accion btn-desactivar" onClick={() => handleEliminar(c.id)}>
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
            <h2>{editandoId ? 'Editar calificación' : 'Nueva calificación'}</h2>

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
                <label>Docente</label>
                <select name="docenteId" value={form.docenteId} onChange={handleChange} required>
                  {docentes.map((d) => (
                    <option key={d.id} value={d.id}>{d.nombre}</option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label>Tipo</label>
                <select name="tipo" value={form.tipo} onChange={handleChange} required>
                  {TIPOS.map((t) => (
                    <option key={t} value={t}>{t}</option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label>Nota (0 a 10)</label>
                <input
                  type="number"
                  name="valor"
                  value={form.valor}
                  onChange={handleChange}
                  min="0"
                  max="10"
                  step="0.01"
                  required
                />
              </div>

              <div className="form-group">
                <label>Fecha</label>
                <input
                  type="date"
                  name="fechaRegistro"
                  value={form.fechaRegistro}
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

export default Calificaciones;