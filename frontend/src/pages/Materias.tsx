import { useEffect, useState } from 'react';
import apiClient from '../api/client';
import type { MateriaRequest, MateriaResponse } from '../types/materia';
import type { DocenteResponse } from '../types/docente';
import './Materias.css';

const NIVELES = ['1ro', '2do', '3ro', '4to', '5to', '6to', '7mo', '8vo', '9no', '10mo', '1ro Bach', '2do Bach', '3ro Bach'];

function Materias() {
  const [materias, setMaterias] = useState<MateriaResponse[]>([]);
  const [docentes, setDocentes] = useState<DocenteResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalAbierto, setModalAbierto] = useState(false);
  const [editandoId, setEditandoId] = useState<string | null>(null);
  const [error, setError] = useState('');
  const [guardando, setGuardando] = useState(false);

  const [form, setForm] = useState<MateriaRequest>({
    nombre: '',
    codigo: '',
    creditos: 1,
    nivel: '',
    docenteId: null,
  });

  const cargarDatos = async () => {
    setLoading(true);
    try {
      const [matRes, docRes] = await Promise.all([
        apiClient.get<MateriaResponse[]>('/materias'),
        apiClient.get<DocenteResponse[]>('/docentes'),
      ]);
      setMaterias(matRes.data);
      setDocentes(docRes.data);
    } catch {
      setError('No se pudieron cargar las materias');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    cargarDatos();
  }, []);

  const abrirNuevo = () => {
    setEditandoId(null);
    setForm({ nombre: '', codigo: '', creditos: 1, nivel: '', docenteId: null });
    setError('');
    setModalAbierto(true);
  };

  const abrirEditar = (m: MateriaResponse) => {
    setEditandoId(m.id);
    setForm({
      nombre: m.nombre,
      codigo: m.codigo,
      creditos: m.creditos,
      nivel: m.nivel || '',
      docenteId: m.docenteId,
    });
    setError('');
    setModalAbierto(true);
  };

  const cerrarModal = () => setModalAbierto(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    if (name === 'creditos') {
      setForm({ ...form, creditos: parseInt(value, 10) || 0 });
    } else if (name === 'docenteId') {
      setForm({ ...form, docenteId: value === '' ? null : value });
    } else {
      setForm({ ...form, [name]: value });
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setGuardando(true);

    try {
      if (editandoId) {
        await apiClient.put(`/materias/${editandoId}`, form);
      } else {
        await apiClient.post('/materias', form);
      }
      await cargarDatos();
      setModalAbierto(false);
    } catch {
      setError('No se pudo guardar la materia. Revisa los datos.');
    } finally {
      setGuardando(false);
    }
  };

  const handleEliminar = async (id: string) => {
    if (!confirm('¿Eliminar esta materia?')) return;
    try {
      await apiClient.delete(`/materias/${id}`);
      await cargarDatos();
    } catch {
      setError('No se pudo eliminar la materia');
    }
  };

  return (
    <div className="materias-container">
      <div className="materias-header">
        <h1>Materias</h1>
        <button className="btn-nuevo" onClick={abrirNuevo}>
          + Nueva materia
        </button>
      </div>

      <div className="materias-table-wrapper">
        {loading ? (
          <div className="materias-cargando">Cargando materias...</div>
        ) : materias.length === 0 ? (
          <div className="materias-vacio">No hay materias registradas todavía.</div>
        ) : (
          <table className="materias-table">
            <thead>
              <tr>
                <th>Código</th>
                <th>Nombre</th>
                <th>Nivel</th>
                <th>Créditos</th>
                <th>Docente</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {materias.map((m) => (
                <tr key={m.id}>
                  <td>{m.codigo}</td>
                  <td>{m.nombre}</td>
                  <td>{m.nivel || '—'}</td>
                  <td>{m.creditos}</td>
                  <td>{m.docenteNombre || '—'}</td>
                  <td>
                    <div className="tabla-acciones">
                      <button className="btn-accion btn-editar" onClick={() => abrirEditar(m)}>
                        Editar
                      </button>
                      <button className="btn-accion btn-desactivar" onClick={() => handleEliminar(m.id)}>
                        Eliminar
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
            <h2>{editandoId ? 'Editar materia' : 'Nueva materia'}</h2>

            {error && <p className="modal-error">{error}</p>}

            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Nombre</label>
                <input name="nombre" value={form.nombre} onChange={handleChange} required placeholder="Ej: Matemáticas" />
              </div>

              <div className="form-group">
                <label>Código</label>
                <input name="codigo" value={form.codigo} onChange={handleChange} required placeholder="Ej: MAT-10" />
              </div>

              <div className="form-group">
                <label>Nivel</label>
                <select name="nivel" value={form.nivel} onChange={handleChange} required>
                  <option value="">Selecciona un nivel</option>
                  {NIVELES.map((n) => (
                    <option key={n} value={n}>{n}</option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label>Créditos</label>
                <input type="number" name="creditos" value={form.creditos} onChange={handleChange} min="1" required />
              </div>

              <div className="form-group">
                <label>Docente (opcional)</label>
                <select name="docenteId" value={form.docenteId || ''} onChange={handleChange}>
                  <option value="">Sin docente asignado</option>
                  {docentes.map((d) => (
                    <option key={d.id} value={d.id}>{d.nombre}</option>
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

export default Materias;