import { useEffect, useState, useMemo } from 'react';
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
  const [miDocenteId, setMiDocenteId] = useState('');

  const [filtroMateriaId, setFiltroMateriaId] = useState('');
  const [filtroSeccion, setFiltroSeccion] = useState('');
  const [filtroFecha, setFiltroFecha] = useState(new Date().toISOString().slice(0, 10));

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

      if (rol === 'DOCENTE') {
        const docRes = await apiClient.get<DocenteResponse[]>('/docentes');
        const yo = docRes.data.find((d) => d.email === emailActual);
        if (yo) setMiDocenteId(yo.id);
        const misMateriaIds = matRes.data
          .filter((m) => {
            if (m.docenteId === yo?.id) return true;
            if (m.docentesPorJornada && m.docentesPorJornada.some((dj) => dj.docenteId === yo?.id)) return true;
            return false;
          })
          .map((m) => m.id);
        const todasRes = await apiClient.get<AsistenciaResponse[]>('/asistencias');
        const deMisMaterias = todasRes.data.filter((a) => misMateriaIds.includes(a.materiaId));
        setAsistencias(deMisMaterias);
      } else if (rol === 'ESTUDIANTE') {
        const yo = estRes.data.find((e) => e.email === emailActual);
        if (yo) {
          const asisRes = await apiClient.get<AsistenciaResponse[]>(`/asistencias/estudiante/${yo.id}`);
          setAsistencias(asisRes.data);
        } else { setAsistencias([]); }
      } else if (rol === 'REPRESENTANTE') {
        const misEstudianteIds = estRes.data.filter((e) => e.representanteId === miUsuario?.id).map((e) => e.id);
        const todasRes = await apiClient.get<AsistenciaResponse[]>('/asistencias');
        setAsistencias(todasRes.data.filter((a) => misEstudianteIds.includes(a.estudianteId)));
      } else {
        const asisRes = await apiClient.get<AsistenciaResponse[]>('/asistencias');
        setAsistencias(asisRes.data);
      }
    } catch { setError('No se pudieron cargar los datos'); }
    finally { setLoading(false); }
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
    if (filtroSeccion) { filtrados = filtrados.filter((e) => e.seccion === filtroSeccion); }
    return filtrados.sort((a, b) => a.nombre.localeCompare(b.nombre));
  }, [estudiantes, materiaSeleccionada, filtroSeccion, rol, miJornada]);

  const asistenciasFiltradas = useMemo(() => {
    let filtradas = asistencias;
    if (filtroMateriaId) { filtradas = filtradas.filter((a) => a.materiaId === filtroMateriaId); }
    if (rol === 'DOCENTE' && miJornada && materiaSeleccionada) {
      const sufijo = miJornada === 'Matutina' ? '-M' : '-V';
      const idsJornada = estudiantes.filter((e) => e.nivel === materiaSeleccionada.nivel && e.seccion.endsWith(sufijo)).map((e) => e.id);
      filtradas = filtradas.filter((a) => idsJornada.includes(a.estudianteId));
    }
    if (filtroSeccion && materiaSeleccionada) {
      const ids = estudiantes.filter((e) => e.nivel === materiaSeleccionada.nivel && e.seccion === filtroSeccion).map((e) => e.id);
      filtradas = filtradas.filter((a) => ids.includes(a.estudianteId));
    }
    if (filtroFecha) { filtradas = filtradas.filter((a) => a.fecha === filtroFecha); }
    return filtradas;
  }, [asistencias, filtroMateriaId, filtroSeccion, filtroFecha, estudiantes, materiaSeleccionada, rol, miJornada]);

  const estudiantesSinRegistro = useMemo(() => {
    const idsConRegistro = asistenciasFiltradas.map((a) => a.estudianteId);
    return estudiantesFiltrados.filter((e) => !idsConRegistro.includes(e.id));
  }, [estudiantesFiltrados, asistenciasFiltradas]);

  const abrirNuevo = () => {
    setForm({ fecha: filtroFecha || new Date().toISOString().slice(0, 10), estado: 'P',
      estudianteId: estudiantesSinRegistro[0]?.id || estudiantesFiltrados[0]?.id || '',
      materiaId: filtroMateriaId || materiasDisponibles[0]?.id || '' });
    setError(''); setModalAbierto(true);
  };

  const cerrarModal = () => setModalAbierto(false);
  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => { setForm({ ...form, [e.target.name]: e.target.value }); };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault(); setError(''); setGuardando(true);
    try { await apiClient.post(`/asistencias?actorId=${actorId}`, form); await cargarDatos(); setModalAbierto(false); }
    catch { setError('No se pudo guardar la asistencia. Revisa los datos.'); }
    finally { setGuardando(false); }
  };

  const handleJustificar = async (id: string) => {
    try { await apiClient.patch(`/asistencias/${id}/justificar?actorId=${actorId}`); await cargarDatos(); }
    catch { setError('No se pudo justificar la asistencia'); }
  };

  const handleEliminar = async (id: string) => {
    if (!confirm('¿Eliminar este registro de asistencia?')) return;
    try { await apiClient.delete(`/asistencias/${id}?actorId=${actorId}`); await cargarDatos(); }
    catch { setError('No se pudo eliminar el registro'); }
  };

  const contadores = useMemo(() => {
    const c = { P: 0, A: 0, J: 0, T: 0 };
    asistenciasFiltradas.forEach((a) => { if (c[a.estado as keyof typeof c] !== undefined) c[a.estado as keyof typeof c]++; });
    return c;
  }, [asistenciasFiltradas]);

  return (
    <div className="asistencias-container">
      <div className="asistencias-header">
        <h1>Asistencia</h1>
        {puedeEditar && filtroMateriaId && (
          <button className="btn-nuevo" onClick={abrirNuevo} disabled={estudiantesFiltrados.length === 0}>+ Nuevo registro</button>
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
        <div className="filtro-group">
          <label>Fecha</label>
          <input type="date" value={filtroFecha} onChange={(e) => setFiltroFecha(e.target.value)} />
        </div>
        {filtroMateriaId && (
          <div className="filtro-info">
            {estudiantesFiltrados.length} estudiantes ·
            <span className="estado-mini estado-P"> {contadores.P} P</span>
            <span className="estado-mini estado-A"> {contadores.A} A</span>
            <span className="estado-mini estado-J"> {contadores.J} J</span>
            <span className="estado-mini estado-T"> {contadores.T} T</span>
            {estudiantesSinRegistro.length > 0 && (<span className="sin-registro"> · {estudiantesSinRegistro.length} sin registrar</span>)}
          </div>
        )}
      </div>

      <div className="asistencias-table-wrapper">
        {loading ? (<div className="asistencias-cargando">Cargando asistencias...</div>)
        : !filtroMateriaId ? (<div className="asistencias-vacio">Selecciona una materia para ver la asistencia.</div>)
        : asistenciasFiltradas.length === 0 ? (<div className="asistencias-vacio">No hay registros de asistencia para esta fecha y materia.</div>)
        : (
          <table className="asistencias-table">
            <thead><tr><th>Estudiante</th><th>Sección</th><th>Estado</th><th>Fecha</th>{puedeEditar && <th>Acciones</th>}</tr></thead>
            <tbody>
              {asistenciasFiltradas.map((a) => {
                const est = estudiantes.find((e) => e.id === a.estudianteId);
                return (
                  <tr key={a.id}>
                    <td>{a.estudianteNombre}</td>
                    <td>{est?.seccion || '—'}</td>
                    <td><span className={`estado-badge estado-${a.estado}`}>{ESTADOS[a.estado] || a.estado}</span></td>
                    <td>{a.fecha}</td>
                    {puedeEditar && (<td><div className="tabla-acciones">
                      {a.estado === 'A' && (<button className="btn-accion btn-justificar" onClick={() => handleJustificar(a.id)}>Justificar</button>)}
                      <button className="btn-accion btn-desactivar" onClick={() => handleEliminar(a.id)}>Eliminar</button>
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
            <h2>Nuevo registro de asistencia</h2>
            {error && <p className="modal-error">{error}</p>}
            <form onSubmit={handleSubmit}>
              <div className="form-group"><label>Estudiante</label>
                <select name="estudianteId" value={form.estudianteId} onChange={handleChange} required>
                  <option value="">Selecciona un estudiante</option>
                  {estudiantesSinRegistro.length > 0 ? (
                    <>
                      <optgroup label="Sin registrar hoy">
                        {estudiantesSinRegistro.map((e) => (<option key={e.id} value={e.id}>{e.nombre} ({e.seccion})</option>))}
                      </optgroup>
                      {estudiantesFiltrados.filter((e) => !estudiantesSinRegistro.some((s) => s.id === e.id)).length > 0 && (
                        <optgroup label="Ya registrados">
                          {estudiantesFiltrados.filter((e) => !estudiantesSinRegistro.some((s) => s.id === e.id)).map((e) => (
                            <option key={e.id} value={e.id}>{e.nombre} ({e.seccion})</option>))}
                        </optgroup>
                      )}
                    </>
                  ) : (estudiantesFiltrados.map((e) => (<option key={e.id} value={e.id}>{e.nombre} ({e.seccion})</option>)))}
                </select>
              </div>
              <div className="form-group"><label>Materia</label>
                <select name="materiaId" value={form.materiaId} onChange={handleChange} required>
                  {materiasDisponibles.map((m) => (<option key={m.id} value={m.id}>{m.nombre} ({m.codigo}) - {m.nivel}</option>))}
                </select>
              </div>
              <div className="form-group"><label>Estado</label>
                <select name="estado" value={form.estado} onChange={handleChange} required>
                  {Object.entries(ESTADOS).map(([codigo, label]) => (<option key={codigo} value={codigo}>{label}</option>))}
                </select>
              </div>
              <div className="form-group"><label>Fecha</label>
                <input type="date" name="fecha" value={form.fecha} onChange={handleChange} required />
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

export default Asistencias;