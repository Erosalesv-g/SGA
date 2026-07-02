import { useEffect, useState } from 'react';
import apiClient from '../api/client';
import type { HorarioRequest, HorarioResponse } from '../types/horario';
import type { DocenteResponse } from '../types/docente';
import type { MateriaResponse } from '../types/materia';
import type { EstudianteResponse } from '../types/estudiante';
import './Horarios.css';

const DIAS = ['LUNES', 'MARTES', 'MIERCOLES', 'JUEVES', 'VIERNES'];

function Horarios() {
  const rol = localStorage.getItem('rol') || '';
  const emailActual = localStorage.getItem('email') || '';
  const puedeEditar = rol === 'RECTOR';

  const [horarios, setHorarios] = useState<HorarioResponse[]>([]);
  const [docentes, setDocentes] = useState<DocenteResponse[]>([]);
  const [materias, setMaterias] = useState<MateriaResponse[]>([]);

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
        } else {
          setHorarios([]);
        }
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
      docenteId: docentes[0]?.id || '',
      materiaId: materias[0]?.id || '',
      diaSemana: 'LUNES',
      horaInicio: '07:00',
      horaFin: '08:00',
      aula: '',
      periodo: '2026-2027',
    });
    setError('');
    setModalAbierto(true);
  };

  const abrirEditar = (h: HorarioResponse) => {
    setEditandoId(h.id);
    setForm({
      docenteId: h.docenteId,
      materiaId: h.materiaId,
      diaSemana: h.diaSemana,
      horaInicio: h.horaInicio.slice(0, 5),
      horaFin: h.horaFin.slice(0, 5),
      aula: h.aula || '',
      periodo: h.periodo,
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
        await apiClient.put(`/horarios/${editandoId}`, form);
      } else {
        await apiClient.post('/horarios', form);
      }
      await cargarDatos();
      setModalAbierto(false);
    } catch {
      setError('No se pudo guardar el horario. Revisa los datos.');
    } finally {
      setGuardando(false);
    }
  };

  const handleEliminar = async (id: string) => {
    if (!confirm('¿Eliminar este horario?')) return;
    try {
      await apiClient.delete(`/horarios/${id}`);
      await cargarDatos();
    } catch {
      setError('No se pudo eliminar el horario');
    }
  };

  return (
    <div className="horarios-container">
      <div className="horarios-header">
        <h1>Horarios</h1>
        {puedeEditar && (
          <button className="btn-nuevo" onClick={abrirNuevo} disabled={docentes.length === 0 || materias.length === 0}>
            + Nuevo horario
          </button>
        )}
      </div>

      <div className="horarios-table-wrapper">
        {loading ? (
          <div className="horarios-cargando">Cargando horarios...</div>
        ) : horarios.length === 0 ? (
          <div className="horarios-vacio">No hay horarios registrados todavía.</div>
        ) : (
          <table className="horarios-table">
            <thead>
              <tr>
                <th>Docente</th>
                <th>Materia</th>
                <th>Día</th>
                <th>Hora</th>
                <th>Aula</th>
                <th>Período</th>
                {puedeEditar && <th>Acciones</th>}
              </tr>
            </thead>
            <tbody>
              {horarios.map((h) => (
                <tr key={h.id}>
                  <td>{h.docenteNombre}</td>
                  <td>{h.materiaNombre}</td>
                  <td>{h.diaSemana}</td>
                  <td>{h.horaInicio.slice(0, 5)} - {h.horaFin.slice(0, 5)}</td>
                  <td>{h.aula || '—'}</td>
                  <td>{h.periodo}</td>
                  {puedeEditar && (
                    <td>
                      <div className="tabla-acciones">
                        <button className="btn-accion btn-editar" onClick={() => abrirEditar(h)}>
                          Editar
                        </button>
                        <button className="btn-accion btn-desactivar" onClick={() => handleEliminar(h.id)}>
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
            <h2>{editandoId ? 'Editar horario' : 'Nuevo horario'}</h2>

            {error && <p className="modal-error">{error}</p>}

            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Docente</label>
                <select name="docenteId" value={form.docenteId} onChange={handleChange} required>
                  {docentes.map((d) => (
                    <option key={d.id} value={d.id}>{d.nombre}</option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label>Materia</label>
                <select name="materiaId" value={form.materiaId} onChange={handleChange} required>
                  {materias.map((m) => (
                    <option key={m.id} value={m.id}>{m.nombre} ({m.codigo}) - {m.nivel}</option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label>Día</label>
                <select name="diaSemana" value={form.diaSemana} onChange={handleChange} required>
                  {DIAS.map((d) => (
                    <option key={d} value={d}>{d}</option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label>Hora inicio</label>
                <input type="time" name="horaInicio" value={form.horaInicio} onChange={handleChange} required />
              </div>

              <div className="form-group">
                <label>Hora fin</label>
                <input type="time" name="horaFin" value={form.horaFin} onChange={handleChange} required />
              </div>

              <div className="form-group">
                <label>Aula</label>
                <input name="aula" value={form.aula} onChange={handleChange} placeholder="Ej: Aula 12" />
              </div>

              <div className="form-group">
                <label>Período</label>
                <input name="periodo" value={form.periodo} onChange={handleChange} required placeholder="Ej: 2026-2027" />
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

export default Horarios;