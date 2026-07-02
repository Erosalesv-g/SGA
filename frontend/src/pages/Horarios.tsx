import { useEffect, useState, useMemo } from 'react';
import apiClient from '../api/client';
import type { HorarioRequest, HorarioResponse } from '../types/horario';
import type { DocenteResponse } from '../types/docente';
import type { MateriaResponse } from '../types/materia';
import type { EstudianteResponse } from '../types/estudiante';
import './Horarios.css';

const DIAS = ['LUNES', 'MARTES', 'MIERCOLES', 'JUEVES', 'VIERNES'];
const DIAS_LABEL: Record<string, string> = { LUNES: 'Lunes', MARTES: 'Martes', MIERCOLES: 'Miércoles', JUEVES: 'Jueves', VIERNES: 'Viernes' };

const COLORES_MATERIA = [
  { bg: '#E8F4FD', border: '#1a5f8a', text: '#1a5f8a' },
  { bg: '#F3E8FD', border: '#6b2fa0', text: '#6b2fa0' },
  { bg: '#E8FDE8', border: '#2e7d32', text: '#2e7d32' },
  { bg: '#FDE8E8', border: '#c62828', text: '#c62828' },
  { bg: '#FDF6E8', border: '#ef6c00', text: '#ef6c00' },
  { bg: '#E8F0FD', border: '#1565c0', text: '#1565c0' },
  { bg: '#FDE8F3', border: '#ad1457', text: '#ad1457' },
  { bg: '#E8FDF6', border: '#00695c', text: '#00695c' },
  { bg: '#F5E8FD', border: '#6a1b9a', text: '#6a1b9a' },
  { bg: '#FDF0E8', border: '#d84315', text: '#d84315' },
];

function Horarios() {
  const rol = localStorage.getItem('rol') || '';
  const emailActual = localStorage.getItem('email') || '';
  const puedeEditar = rol === 'RECTOR';

  const [horarios, setHorarios] = useState<HorarioResponse[]>([]);
  const [docentes, setDocentes] = useState<DocenteResponse[]>([]);
  const [materias, setMaterias] = useState<MateriaResponse[]>([]);
  const [vistaTabla, setVistaTabla] = useState(false);

  const [loading, setLoading] = useState(true);
  const [modalAbierto, setModalAbierto] = useState(false);
  const [editandoId, setEditandoId] = useState<string | null>(null);
  const [error, setError] = useState('');
  const [guardando, setGuardando] = useState(false);

  const [form, setForm] = useState<HorarioRequest>({
    docenteId: '',
    materiaId: '',
    diaSemana: 'LUNES',
    horaInicio: '07:00',
    horaFin: '08:00',
    aula: '',
    periodo: '2026-2027',
  });

  const cargarDatos = async () => {
    setLoading(true);
    try {
      const [docRes, matRes] = await Promise.all([
        apiClient.get<DocenteResponse[]>('/docentes'),
        apiClient.get<MateriaResponse[]>('/materias'),
      ]);
      setDocentes(docRes.data);
      setMaterias(matRes.data);

      if (rol === 'DOCENTE') {
        const yo = docRes.data.find((d) => d.email === emailActual);
        if (yo) {
          const horRes = await apiClient.get<HorarioResponse[]>(`/horarios/docente/${yo.id}`);
          setHorarios(horRes.data);
        } else { setHorarios([]); }
      } else if (rol === 'ESTUDIANTE') {
        const estRes = await apiClient.get<EstudianteResponse[]>('/estudiantes');
        const yo = estRes.data.find((e) => e.email === emailActual);
        const misMateriaIds = matRes.data.filter((m) => m.nivel === yo?.nivel).map((m) => m.id);
        const todasRes = await apiClient.get<HorarioResponse[]>('/horarios');
        const deMiNivel = todasRes.data.filter((h) => misMateriaIds.includes(h.materiaId));
        setHorarios(deMiNivel);
      } else {
        const horRes = await apiClient.get<HorarioResponse[]>('/horarios');
        setHorarios(horRes.data);
      }
    } catch { setError('No se pudieron cargar los datos'); }
    finally { setLoading(false); }
  };

  useEffect(() => { cargarDatos(); }, []);

  // Generar franjas horarias únicas ordenadas
  const franjasHorarias = useMemo(() => {
    const franjas = new Map<string, { inicio: string; fin: string }>();
    horarios.forEach((h) => {
      const inicio = h.horaInicio.slice(0, 5);
      const fin = h.horaFin.slice(0, 5);
      const key = `${inicio}-${fin}`;
      if (!franjas.has(key)) franjas.set(key, { inicio, fin });
    });
    return Array.from(franjas.values()).sort((a, b) => a.inicio.localeCompare(b.inicio));
  }, [horarios]);

  // Mapa de colores por materia
  const coloresMateria = useMemo(() => {
    const mapa = new Map<string, typeof COLORES_MATERIA[0]>();
    const materiasUnicas = [...new Set(horarios.map((h) => h.materiaNombre))];
    materiasUnicas.sort().forEach((m, i) => {
      mapa.set(m, COLORES_MATERIA[i % COLORES_MATERIA.length]);
    });
    return mapa;
  }, [horarios]);

  // Buscar horario por día y franja
  const getHorario = (dia: string, inicio: string, fin: string) => {
    return horarios.find((h) =>
      h.diaSemana === dia &&
      h.horaInicio.slice(0, 5) === inicio &&
      h.horaFin.slice(0, 5) === fin
    );
  };

  const abrirNuevo = () => {
    setEditandoId(null);
    setForm({ docenteId: docentes[0]?.id || '', materiaId: materias[0]?.id || '', diaSemana: 'LUNES', horaInicio: '07:00', horaFin: '08:00', aula: '', periodo: '2026-2027' });
    setError(''); setModalAbierto(true);
  };

  const abrirEditar = (h: HorarioResponse) => {
    setEditandoId(h.id);
    setForm({ docenteId: h.docenteId, materiaId: h.materiaId, diaSemana: h.diaSemana, horaInicio: h.horaInicio.slice(0, 5), horaFin: h.horaFin.slice(0, 5), aula: h.aula || '', periodo: h.periodo });
    setError(''); setModalAbierto(true);
  };

  const cerrarModal = () => setModalAbierto(false);
  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => { setForm({ ...form, [e.target.name]: e.target.value }); };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault(); setError(''); setGuardando(true);
    try {
      if (editandoId) { await apiClient.put(`/horarios/${editandoId}`, form); }
      else { await apiClient.post('/horarios', form); }
      await cargarDatos(); setModalAbierto(false);
    } catch { setError('No se pudo guardar el horario. Revisa los datos.'); }
    finally { setGuardando(false); }
  };

  const handleEliminar = async (id: string) => {
    if (!confirm('¿Eliminar este horario?')) return;
    try { await apiClient.delete(`/horarios/${id}`); await cargarDatos(); }
    catch { setError('No se pudo eliminar el horario'); }
  };

  return (
    <div className="horarios-container">
      <div className="horarios-header">
        <h1>Horarios</h1>
        <div className="horarios-header-actions">
          <button className={`btn-vista ${!vistaTabla ? 'btn-vista-activo' : ''}`} onClick={() => setVistaTabla(false)}>Grilla</button>
          <button className={`btn-vista ${vistaTabla ? 'btn-vista-activo' : ''}`} onClick={() => setVistaTabla(true)}>Tabla</button>
          {puedeEditar && (
            <button className="btn-nuevo" onClick={abrirNuevo} disabled={docentes.length === 0 || materias.length === 0}>+ Nuevo horario</button>
          )}
        </div>
      </div>

      {loading ? (<div className="horarios-cargando">Cargando horarios...</div>)
      : horarios.length === 0 ? (<div className="horarios-vacio">No hay horarios registrados todavía.</div>)
      : vistaTabla ? (
        /* === VISTA TABLA === */
        <div className="horarios-table-wrapper">
          <table className="horarios-table">
            <thead><tr><th>Docente</th><th>Materia</th><th>Día</th><th>Hora</th><th>Aula</th><th>Período</th>{puedeEditar && <th>Acciones</th>}</tr></thead>
            <tbody>
              {horarios.map((h) => (
                <tr key={h.id}>
                  <td>{h.docenteNombre}</td><td>{h.materiaNombre}</td><td>{DIAS_LABEL[h.diaSemana] || h.diaSemana}</td>
                  <td>{h.horaInicio.slice(0, 5)} - {h.horaFin.slice(0, 5)}</td><td>{h.aula || '—'}</td><td>{h.periodo}</td>
                  {puedeEditar && (<td><div className="tabla-acciones">
                    <button className="btn-accion btn-editar" onClick={() => abrirEditar(h)}>Editar</button>
                    <button className="btn-accion btn-desactivar" onClick={() => handleEliminar(h.id)}>Eliminar</button>
                  </div></td>)}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        /* === VISTA GRILLA === */
        <div className="horarios-grilla-wrapper">
          <div className="horarios-grilla">
            {/* Header de días */}
            <div className="grilla-header grilla-hora-header">Hora</div>
            {DIAS.map((dia) => (
              <div key={dia} className="grilla-header">{DIAS_LABEL[dia]}</div>
            ))}

            {/* Filas por franja horaria */}
            {franjasHorarias.map((franja) => (
              <>
                <div key={`hora-${franja.inicio}`} className="grilla-hora">
                  <span className="hora-inicio">{franja.inicio}</span>
                  <span className="hora-fin">{franja.fin}</span>
                </div>
                {DIAS.map((dia) => {
                  const h = getHorario(dia, franja.inicio, franja.fin);
                  const color = h ? coloresMateria.get(h.materiaNombre) : null;
                  return (
                    <div
                      key={`${dia}-${franja.inicio}`}
                      className={`grilla-celda ${h ? 'grilla-celda-ocupada' : ''}`}
                      style={h && color ? { backgroundColor: color.bg, borderLeft: `3px solid ${color.border}` } : {}}
                      onClick={() => h && puedeEditar && abrirEditar(h)}
                    >
                      {h && (
                        <>
                          <span className="celda-materia" style={{ color: color?.text }}>{h.materiaNombre}</span>
                          <span className="celda-docente">{h.docenteNombre}</span>
                          <span className="celda-aula">{h.aula}</span>
                        </>
                      )}
                    </div>
                  );
                })}
              </>
            ))}
          </div>
        </div>
      )}

      {modalAbierto && (
        <div className="modal-overlay" onClick={cerrarModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>{editandoId ? 'Editar horario' : 'Nuevo horario'}</h2>
            {error && <p className="modal-error">{error}</p>}
            <form onSubmit={handleSubmit}>
              <div className="form-group"><label>Docente</label>
                <select name="docenteId" value={form.docenteId} onChange={handleChange} required>
                  {docentes.map((d) => (<option key={d.id} value={d.id}>{d.nombre}</option>))}
                </select>
              </div>
              <div className="form-group"><label>Materia</label>
                <select name="materiaId" value={form.materiaId} onChange={handleChange} required>
                  {materias.map((m) => (<option key={m.id} value={m.id}>{m.nombre} ({m.codigo}) - {m.nivel}</option>))}
                </select>
              </div>
              <div className="form-group"><label>Día</label>
                <select name="diaSemana" value={form.diaSemana} onChange={handleChange} required>
                  {DIAS.map((d) => (<option key={d} value={d}>{DIAS_LABEL[d]}</option>))}
                </select>
              </div>
              <div className="form-group"><label>Hora inicio</label>
                <input type="time" name="horaInicio" value={form.horaInicio} onChange={handleChange} required />
              </div>
              <div className="form-group"><label>Hora fin</label>
                <input type="time" name="horaFin" value={form.horaFin} onChange={handleChange} required />
              </div>
              <div className="form-group"><label>Aula</label>
                <input name="aula" value={form.aula} onChange={handleChange} placeholder="Ej: Aula 101" />
              </div>
              <div className="form-group"><label>Período</label>
                <input name="periodo" value={form.periodo} onChange={handleChange} required placeholder="Ej: 2026-2027" />
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

export default Horarios;