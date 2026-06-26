import { useEffect, useState } from 'react';
import apiClient from '../api/client';
import type { RecursoPedagogicoResponse } from '../types/recurso';
import type { MateriaResponse } from '../types/materia';
import type { UsuarioBasico } from '../types/comunicado';
import './RecursosPedagogicos.css';

function formatearTamano(bytes: number | null): string {
  if (!bytes) return '—';
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

function RecursosPedagogicos() {
  const rol = localStorage.getItem('rol') || '';
  const emailActual = localStorage.getItem('email') || '';
  const puedePublicar = rol === 'RECTOR' || rol === 'DOCENTE';

  const [recursos, setRecursos] = useState<RecursoPedagogicoResponse[]>([]);
  const [materias, setMaterias] = useState<MateriaResponse[]>([]);
  const [usuarioActualId, setUsuarioActualId] = useState('');
  const [loading, setLoading] = useState(true);
  const [modalAbierto, setModalAbierto] = useState(false);
  const [error, setError] = useState('');
  const [guardando, setGuardando] = useState(false);

  const [titulo, setTitulo] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [materiaId, setMateriaId] = useState('');
  const [archivo, setArchivo] = useState<File | null>(null);

  const cargarDatos = async () => {
    setLoading(true);
    try {
      const [recRes, matRes, usuRes] = await Promise.all([
        apiClient.get<RecursoPedagogicoResponse[]>('/recursos'),
        apiClient.get<MateriaResponse[]>('/materias'),
        apiClient.get<UsuarioBasico[]>('/usuarios'),
      ]);
      setRecursos(recRes.data);
      setMaterias(matRes.data);
      const yo = usuRes.data.find((u) => u.email === emailActual);
      if (yo) setUsuarioActualId(yo.id);
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
    setTitulo('');
    setDescripcion('');
    setMateriaId(materias[0]?.id || '');
    setArchivo(null);
    setError('');
    setModalAbierto(true);
  };

  const cerrarModal = () => setModalAbierto(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!archivo) {
      setError('Selecciona un archivo');
      return;
    }
    setError('');
    setGuardando(true);

    const formData = new FormData();
    formData.append('titulo', titulo);
    formData.append('descripcion', descripcion);
    formData.append('materiaId', materiaId);
    formData.append('docenteId', usuarioActualId);
    formData.append('archivo', archivo);

    try {
      await apiClient.post('/recursos', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      await cargarDatos();
      setModalAbierto(false);
    } catch {
      setError('No se pudo subir el recurso. Revisa los datos.');
    } finally {
      setGuardando(false);
    }
  };

  const handleDescargar = async (recurso: RecursoPedagogicoResponse) => {
    try {
      const response = await apiClient.get(`/recursos/${recurso.id}/descargar`, {
        responseType: 'blob',
      });
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', recurso.nombreArchivo);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch {
      setError('No se pudo descargar el archivo');
    }
  };

  const handleEliminar = async (id: string) => {
    if (!confirm('¿Eliminar este recurso?')) return;
    try {
      await apiClient.delete(`/recursos/${id}`);
      await cargarDatos();
    } catch {
      setError('No se pudo eliminar el recurso');
    }
  };

  return (
    <div className="recursos-container">
      <div className="recursos-header">
        <h1>Recursos Pedagógicos</h1>
        {puedePublicar && (
          <button className="btn-nuevo" onClick={abrirNuevo} disabled={materias.length === 0}>
            + Subir recurso
          </button>
        )}
      </div>

      {error && <p className="modal-error">{error}</p>}

      {loading ? (
        <div className="recursos-cargando">Cargando recursos...</div>
      ) : recursos.length === 0 ? (
        <div className="recursos-vacio">No hay recursos pedagógicos publicados todavía.</div>
      ) : (
        <div className="recursos-lista">
          {recursos.map((r) => (
            <div className="recurso-card" key={r.id}>
              <div className="recurso-info">
                <h3>{r.titulo}</h3>
                <p className="recurso-meta">
                  {r.materiaNombre} · Por {r.docenteNombre} · {new Date(r.fechaPublicacion).toLocaleDateString()}
                </p>
                {r.descripcion && <p className="recurso-descripcion">{r.descripcion}</p>}
                <span className="recurso-archivo-badge">{r.nombreArchivo} ({formatearTamano(r.tamanoBytes)})</span>
              </div>
              <div className="tabla-acciones">
                <button className="btn-accion btn-editar" onClick={() => handleDescargar(r)}>
                  Descargar
                </button>
                {puedePublicar && (
                  <button className="btn-accion btn-desactivar" onClick={() => handleEliminar(r.id)}>
                    Eliminar
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {modalAbierto && (
        <div className="modal-overlay" onClick={cerrarModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>Subir recurso pedagógico</h2>

            {error && <p className="modal-error">{error}</p>}

            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Título</label>
                <input value={titulo} onChange={(e) => setTitulo(e.target.value)} required />
              </div>

              <div className="form-group">
                <label>Descripción (opcional)</label>
                <textarea value={descripcion} onChange={(e) => setDescripcion(e.target.value)} />
              </div>

              <div className="form-group">
                <label>Materia</label>
                <select value={materiaId} onChange={(e) => setMateriaId(e.target.value)} required>
                  {materias.map((m) => (
                    <option key={m.id} value={m.id}>{m.nombre}</option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label>Archivo</label>
                <input
                  type="file"
                  onChange={(e) => setArchivo(e.target.files ? e.target.files[0] : null)}
                  required
                />
              </div>

              <div className="modal-actions">
                <button type="button" className="btn-cancelar" onClick={cerrarModal}>
                  Cancelar
                </button>
                <button type="submit" className="btn-guardar" disabled={guardando}>
                  {guardando ? 'Subiendo...' : 'Subir'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default RecursosPedagogicos;