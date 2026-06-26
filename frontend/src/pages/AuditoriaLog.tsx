import { useEffect, useState } from 'react';
import apiClient from '../api/client';
import type { AuditoriaLogResponse } from '../types/auditoria';
import './AuditoriaLog.css';

const ACCION_LABEL: Record<string, string> = {
  CREAR: 'Creó',
  EDITAR: 'Editó',
  ELIMINAR: 'Eliminó',
};

function AuditoriaLog() {
  const [registros, setRegistros] = useState<AuditoriaLogResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const cargarRegistros = async () => {
    setLoading(true);
    try {
      const response = await apiClient.get<AuditoriaLogResponse[]>('/auditoria');
      setRegistros(response.data);
    } catch {
      setError('No se pudo cargar el registro de auditoría');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    cargarRegistros();
  }, []);

  return (
    <div className="auditoria-container">
      <div className="auditoria-header">
        <h1>Auditoría del Sistema</h1>
      </div>

      {error && <p className="modal-error">{error}</p>}

      <div className="auditoria-table-wrapper">
        {loading ? (
          <div className="auditoria-cargando">Cargando registros...</div>
        ) : registros.length === 0 ? (
          <div className="auditoria-vacio">No hay registros de auditoría todavía.</div>
        ) : (
          <table className="auditoria-table">
            <thead>
              <tr>
                <th>Usuario</th>
                <th>Acción</th>
                <th>Entidad</th>
                <th>Descripción</th>
                <th>Fecha</th>
              </tr>
            </thead>
            <tbody>
              {registros.map((r) => (
                <tr key={r.id}>
                  <td>{r.usuarioNombre}</td>
                  <td>
                    <span className={`accion-badge accion-${r.accion}`}>
                      {ACCION_LABEL[r.accion] || r.accion}
                    </span>
                  </td>
                  <td>{r.entidad}</td>
                  <td>{r.descripcion}</td>
                  <td>{new Date(r.fecha).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}

export default AuditoriaLog;