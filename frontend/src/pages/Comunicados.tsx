import { useEffect, useState } from 'react';
import apiClient from '../api/client';
import type { ComunicadoRequest, ComunicadoResponse, UsuarioBasico } from '../types/comunicado';
import './Comunicados.css';

const ROLES = ['TODOS', 'RECTOR', 'INSPECTOR', 'DOCENTE', 'ESTUDIANTE', 'REPRESENTANTE', 'ORIENTADOR'];

function Comunicados() {
  const rol = localStorage.getItem('rol') || '';
  const emailActual = localStorage.getItem('email') || '';
  const puedePublicar = rol === 'RECTOR' || rol === 'DOCENTE' || rol === 'INSPECTOR' || rol === 'ORIENTADOR';

  const [comunicados, setComunicados] = useState<ComunicadoResponse[]>([]);
  const [usuarioActualId, setUsuarioActualId] = useState<string>('');
  const [loading, setLoading] = useState(true);
  const [modalAbierto, setModalAbierto] = useState(false);
  const [error, setError] = useState('');
  const [guardando, setGuardando] = useState(false);

  const [form, setForm] = useState<ComunicadoRequest>({
    titulo: '',
    contenido: '',
    remitenteId: '',
    destinatarioRol: 'DOCENTE',
  });

 const cargarDatos = async () => {
    setLoading(true);
    try {
      const [usuRes, comRes] = await Promise.all([
        apiClient.get<UsuarioBasico[]>('/usuarios'),
        apiClient.get<ComunicadoResponse[]>('/comunicados'),
      ]);

      const usuarioActual = usuRes.data.find((u) => u.email === emailActual);
      const miId = usuarioActual?.id || '';
      if (usuarioActual) setUsuarioActualId(usuarioActual.id);

      if (rol === 'RECTOR') {
        setComunicados(comRes.data);
      } else {
        const filtrados = comRes.data.filter(
          (c) => c.destinatarioRol === rol || c.destinatarioRol === 'TODOS' || c.remitenteId === miId
        );
        setComunicados(filtrados);
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
    setForm({ titulo: '', contenido: '', remitenteId: usuarioActualId, destinatarioRol: 'DOCENTE' });
    setError('');
    setModalAbierto(true);
  };

  const cerrarModal = () => setModalAbierto(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setGuardando(true);

    try {
      await apiClient.post('/comunicados', { ...form, remitenteId: usuarioActualId });
      await cargarDatos();
      setModalAbierto(false);
    } catch {
      setError('No se pudo publicar el comunicado. Revisa los datos.');
    } finally {
      setGuardando(false);
    }
  };

  const handleEliminar = async (id: string) => {
    if (!confirm('¿Eliminar este comunicado?')) return;
    try {
      await apiClient.delete(`/comunicados/${id}`);
      await cargarDatos();
    } catch {
      setError('No se pudo eliminar el comunicado');
    }
  };

  return (
    <div className="comunicados-container">
      <div className="comunicados-header">
        <h1>Comunicados</h1>
        {puedePublicar && (
          <button className="btn-nuevo" onClick={abrirNuevo} disabled={!usuarioActualId}>
            + Nuevo comunicado
          </button>
        )}
      </div>

      {loading ? (
        <div className="comunicados-cargando">Cargando comunicados...</div>
      ) : comunicados.length === 0 ? (
        <div className="comunicados-vacio">No hay comunicados publicados todavía.</div>
      ) : (
        <div className="comunicados-lista">
          {comunicados.map((c) => (
            <div className="comunicado-card" key={c.id}>
              <div className="comunicado-card-header">
                <h3>{c.titulo}</h3>
                <span className="comunicado-rol-badge">{c.destinatarioRol}</span>
              </div>
              <p className="comunicado-meta">
                Por {c.remitenteNombre} · {new Date(c.fechaEnvio).toLocaleString()}
              </p>
              <p className="comunicado-contenido">{c.contenido}</p>
              {puedePublicar && (
                <div className="tabla-acciones" style={{ marginTop: '12px' }}>
                  <button className="btn-accion btn-desactivar" onClick={() => handleEliminar(c.id)}>
                    Eliminar
                  </button>
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      {modalAbierto && (
        <div className="modal-overlay" onClick={cerrarModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>Nuevo comunicado</h2>

            {error && <p className="modal-error">{error}</p>}

            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Título</label>
                <input name="titulo" value={form.titulo} onChange={handleChange} required />
              </div>

              <div className="form-group">
                <label>Contenido</label>
                <textarea name="contenido" value={form.contenido} onChange={handleChange} required />
              </div>

              <div className="form-group">
                <label>Dirigido a</label>
                <select name="destinatarioRol" value={form.destinatarioRol} onChange={handleChange} required>
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
                  {guardando ? 'Publicando...' : 'Publicar'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default Comunicados;