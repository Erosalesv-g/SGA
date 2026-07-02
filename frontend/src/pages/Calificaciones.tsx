import { useEffect, useState, useMemo } from 'react';
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
  const [miDocenteId, setMiDocenteId] = useState('');

  const [filtroMateriaId, setFiltroMateriaId] = useState('');
  const [filtroSeccion, setFiltroSeccion] = useState('');

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

      if (rol === 'DOCENTE') {
        const yo = docRes.data.find((d) => d.email === emailActual);
        if (yo) setMiDocenteId(yo.id);
        const todasRes = await apiClient.get<CalificacionResponse[]>('/calificaciones');
        const propias = yo ? todasRes.data.filter((c) => c.docenteId === yo.id) : [];
        setCalificaciones(propias);
      } else if (rol === 'ESTUDIANTE') {
        const yo = estRes.data.find((e) => e.email === emailActual);
        if (yo) {
          const califRes = await apiClient.get<CalificacionResponse[]>(`/calificaciones/estudiante/${yo.id}`);
          setCalificaciones(califRes.data);
        } else {
          setCalificaciones([]);
        }
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

  useEffect(() => { cargarDatos(); }, []);

  const materiasDisponibles = useMemo(() => {
    if (rol === 'DOCENTE' && miDocenteId) {
      return materias.filter((m) => {
        if (m.docenteId === miDocenteId) return true;
        if (m.docentesPorJornada && m.docentesPorJornada.some((dj) => dj.docenteId === miDocenteId)) return true;
        return false;
      });
    }
    if (rol === 'ESTUDIANTE') {
      const yo = estudiantes.find((e) => e.email === emailActual);
      if (yo) return materias.filter((m) => m.nivel === yo.nivel);
    }
    return materias;
  }, [materias, miDocenteId, rol, estudiantes, emailActual]);

  const materiaSeleccionada = useMemo(() => {
    return materias.find((m) => m.id === filtroMateriaId);
  }, [materias, filtroMateriaId]);

  const miJornada = useMemo(() => {
    if (!materiaSeleccionada || !miDocenteId) return '';
    const dj = materiaSeleccionada.docentesPorJornada?.find((d) => d.docenteId === miDocenteId);
    return dj ? dj.jornada : '';
  }, [materiaSeleccionada, miDocenteId]);

  const seccionesDisponibles = useMemo(() => {
    if (!materiaSeleccionada) return [];
    let filtrados = estudiantes.filter((e) => e.nivel === materiaSeleccionada.nivel);
    if (rol === 'DOCENTE' && miJornada) {
      const sufijo = miJornada === 'Matutina' ? '-M' : '-V';
      filtrados = filtrados.filter((e) => e.seccion.endsWith(sufijo));
    }
    const secciones = [...new Set(filtrados.map((e) => e.seccion))];
    return secciones.sort();
  }, [estudiantes, materiaSeleccionada, rol, miJornada]);

  const estudiantesFiltrados = useMemo(() => {
    if (!materiaSeleccionada) return [];
    let filtrados = estudiantes.filter((e) => e.nivel === materiaSeleccionada.nivel);
    if (rol === 'DOCENTE' && miJornada) {
      const sufijo = miJornada === 'Matutina' ? '-M' : '-V';
      filtrados = filtrados.filter((e) => e.seccion.endsWith(sufijo));
    }
    if (filtroSeccion) {
      filtrados = filtrados.filter((e) => e.seccion === filtroSeccion);
    }
    return filtrados.sort((a, b) => a.nombre.localeCompare(b.nombre));
  }, [estudiantes, materiaSeleccionada, filtroSeccion, rol, miJornada]);

  const calificacionesFiltradas = useMemo(() => {
    let filtradas = calificaciones;
    if (filtroMateriaId) {
      filtradas = filtradas.filter((c) => c.materiaId === filtroMateriaId);
    }
    if (rol === 'DOCENTE' && miJornada && materiaSeleccionada) {
      const sufijo = miJornada === 'Matutina' ? '-M' : '-V';
      const idsJornada = estudiantes
        .filter((e) => e.nivel === materiaSeleccionada.nivel && e.seccion.endsWith(sufijo))
        .map((e) => e.id);
      filtradas = filtradas.filter((c) => idsJornada.includes(c.estudianteId));
    }
    if (filtroSeccion && materiaSeleccionada) {
      const idsEstudiantesSeccion = estudiantes
        .filter((e) => e.nivel === materiaSeleccionada.nivel && e.seccion === filtroSeccion)
        .map((e) => e.id);
      filtradas = filtradas.filter((c) => idsEstudiantesSeccion.includes(c.estudianteId));
    }
    return filtradas;
  }, [calificaciones, filtroMateriaId, filtroSeccion, estudiantes, materiaSeleccionada, rol, miJornada]);

  const abrirNuevo = () => {
    setEditandoId(null);
    setForm({
      valor: 0, tipo: 'PARCIAL',
      fechaRegistro: new Date().toISOString().slice(0, 10),
      estudianteId: estudiantesFiltrados[0]?.id || '',
      materiaId: filtroMateriaId || materiasDisponibles[0]?.id || '',
      docenteId: miDocenteId || docentes[0]?.id || '',
    });
    setError(''); setModalAbierto(true);
  };

  const abrirEditar = (calif: CalificacionResponse) => {
    setEditandoId(calif.id);
    setForm({ valor: calif.valor, tipo: calif.tipo, fechaRegistro: calif.fechaRegistro, estudianteId: calif.estudianteId, materiaId: calif.materiaId, docenteId: calif.docenteId });
    setError(''); setModalAbierto(true);
  };

  const cerrarModal = () => setModalAbierto(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setForm({ ...form, [name]: name === 'valor' ? parseFloat(value) : value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault(); setError(''); setGuardando(true);
    try {
      if (editandoId) { await apiClient.put(`/calificaciones/${editandoId}?actorId=${actorId}`, form); }
      else { await apiClient.post(`/calificaciones?actorId=${actorId}`, form); }
      await cargarDatos(); setModalAbierto(false);
    } catch { setError('No se pudo guardar la calificación. Revisa los datos.'); }
    finally { setGuardando(false); }
  };

  const handleEliminar = async (id: string) => {
    if (!confirm('¿Eliminar esta calificación?')) return;
    try { await apiClient.delete(`/calificaciones/${id}?actorId=${actorId}`); await cargarDatos(); }
    catch { setError('No se pudo eliminar la calificación'); }
  };

  return (
    <div className="calificaciones-container">
      <div className="calificaciones-header">
        <h1>Calificaciones</h1>
        {puedeEditar && filtroMateriaId && (
          <button className="btn-nuevo" onClick={abrirNuevo} disabled={estudiantesFiltrados.length === 0}>+ Nueva calificación</button>
        )}
      </div>

      <div className="filtros-container">
        <div className="filtro-group">
          <label>Materia</label>
          <select value={filtroMateriaId} onChange={(e) => { setFiltroMateriaId(e.target.value); setFiltroSeccion(''); }}>
            <option value="">— Selecciona una materia —</option>
            {materiasDisponibles.map((m) => (<option key={m.id} value={m.id}>{m.nombre} ({m.codigo}) - {m.nivel}</option>))}
          </select>
        </div>
        {filtroMateriaId && seccionesDisponibles.length > 0 && (
          <div className="filtro-group">
            <label>Sección</label>
            <select value={filtroSeccion} onChange={(e) => setFiltroSeccion(e.target.value)}>
              <option value="">Todas las secciones</option>
              {seccionesDisponibles.map((s) => (<option key={s} value={s}>{s}</option>))}
            </select>
          </div>
        )}
        {filtroMateriaId && (
          <div className="filtro-info">{estudiantesFiltrados.length} estudiantes · {calificacionesFiltradas.length} calificaciones</div>
        )}
      </div>

      <div className="calificaciones-table-wrapper">
        {loading ? (<div className="calificaciones-cargando">Cargando calificaciones...</div>)
        : !filtroMateriaId ? (<div className="calificaciones-vacio">Selecciona una materia para ver las calificaciones.</div>)
        : calificacionesFiltradas.length === 0 ? (<div className="calificaciones-vacio">No hay calificaciones registradas para esta materia.</div>)
        : (
          <table className="calificaciones-table">
            <thead><tr><th>Estudiante</th><th>Sección</th><th>Tipo</th><th>Nota</th><th>Fecha</th>{puedeEditar && <th>Acciones</th>}</tr></thead>
            <tbody>
              {calificacionesFiltradas.map((c) => {
                const est = estudiantes.find((e) => e.id === c.estudianteId);
                return (
                  <tr key={c.id}>
                    <td>{c.estudianteNombre}</td>
                    <td>{est?.seccion || '—'}</td>
                    <td>{c.tipo}</td>
                    <td><span className={`valor-nota ${c.valor >= 7 ? 'valor-aprobado' : 'valor-reprobado'}`}>{c.valor.toFixed(2)}</span></td>
                    <td>{c.fechaRegistro}</td>
                    {puedeEditar && (<td><div className="tabla-acciones">
                      <button className="btn-accion btn-editar" onClick={() => abrirEditar(c)}>Editar</button>
                      <button className="btn-accion btn-desactivar" onClick={() => handleEliminar(c.id)}>Eliminar</button>
                    </div></td>)}
                  </tr>);
              })}
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
              <div className="form-group"><label>Estudiante</label>
                <select name="estudianteId" value={form.estudianteId} onChange={handleChange} required>
                  <option value="">Selecciona un estudiante</option>
                  {estudiantesFiltrados.map((e) => (<option key={e.id} value={e.id}>{e.nombre} ({e.seccion})</option>))}
                </select>
              </div>
              <div className="form-group"><label>Materia</label>
                <select name="materiaId" value={form.materiaId} onChange={handleChange} required>
                  {materiasDisponibles.map((m) => (<option key={m.id} value={m.id}>{m.nombre} ({m.codigo}) - {m.nivel}</option>))}
                </select>
              </div>
              {rol !== 'DOCENTE' && (<div className="form-group"><label>Docente</label>
                <select name="docenteId" value={form.docenteId} onChange={handleChange} required>
                  {docentes.map((d) => (<option key={d.id} value={d.id}>{d.nombre}</option>))}
                </select>
              </div>)}
              <div className="form-group"><label>Tipo</label>
                <select name="tipo" value={form.tipo} onChange={handleChange} required>
                  {TIPOS.map((t) => (<option key={t} value={t}>{t}</option>))}
                </select>
              </div>
              <div className="form-group"><label>Nota (0 a 10)</label>
                <input type="number" name="valor" value={form.valor} onChange={handleChange} min="0" max="10" step="0.01" required />
              </div>
              <div className="form-group"><label>Fecha</label>
                <input type="date" name="fechaRegistro" value={form.fechaRegistro} onChange={handleChange} required />
              </div>
              <div className="modal-actions">
                <button type="button" className="btn-cancelar" onClick={cerrarModal}>Cancelar</button>
                <button type="submit" className="btn-guardar" disabled={guardando}>{guardando ? 'Guardando...' : 'Guardar'}</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default Calificaciones;