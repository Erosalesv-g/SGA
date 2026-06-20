import { useEffect, useState } from 'react';
import apiClient from '../api/client';
import type { BoletinResponse, AsistenciaResumenResponse } from '../types/reporte';
import type { EstudianteResponse } from '../types/estudiante';
import './Reportes.css';

function Reportes() {
  const [estudiantes, setEstudiantes] = useState<EstudianteResponse[]>([]);
  const [estudianteId, setEstudianteId] = useState('');
  const [boletin, setBoletin] = useState<BoletinResponse | null>(null);
  const [resumenAsistencia, setResumenAsistencia] = useState<AsistenciaResumenResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [cargandoReporte, setCargandoReporte] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    const cargarEstudiantes = async () => {
      setLoading(true);
      try {
        const res = await apiClient.get<EstudianteResponse[]>('/estudiantes');
        setEstudiantes(res.data);
        if (res.data.length > 0) {
          setEstudianteId(res.data[0].id);
        }
      } catch {
        setError('No se pudieron cargar los estudiantes');
      } finally {
        setLoading(false);
      }
    };
    cargarEstudiantes();
  }, []);

  useEffect(() => {
    if (!estudianteId) return;

    const cargarReportes = async () => {
      setCargandoReporte(true);
      setError('');
      try {
        const [boletinRes, asistenciaRes] = await Promise.all([
          apiClient.get<BoletinResponse>(`/reportes/boletin/${estudianteId}`),
          apiClient.get<AsistenciaResumenResponse>(`/reportes/asistencia/${estudianteId}`),
        ]);
        setBoletin(boletinRes.data);
        setResumenAsistencia(asistenciaRes.data);
      } catch {
        setError('No se pudieron generar los reportes para este estudiante');
        setBoletin(null);
        setResumenAsistencia(null);
      } finally {
        setCargandoReporte(false);
      }
    };
    cargarReportes();
  }, [estudianteId]);

  return (
    <div className="reportes-container">
      <div className="reportes-header">
        <h1>Reportes</h1>
        <div className="form-group reportes-selector">
          <label>Seleccionar estudiante</label>
          <select value={estudianteId} onChange={(e) => setEstudianteId(e.target.value)} disabled={loading}>
            {estudiantes.map((e) => (
              <option key={e.id} value={e.id}>{e.nombre}</option>
            ))}
          </select>
        </div>
      </div>

      {error && <p className="reportes-vacio">{error}</p>}

      {cargandoReporte ? (
        <p className="reportes-vacio">Generando reportes...</p>
      ) : estudiantes.length === 0 ? (
        <p className="reportes-vacio">No hay estudiantes registrados.</p>
      ) : (
        <div className="reportes-grid">
          <div className="reporte-card">
            <h2>Boletín de calificaciones</h2>
            {boletin && boletin.materias.length > 0 ? (
              <>
                <table className="boletin-table">
                  <thead>
                    <tr>
                      <th>Materia</th>
                      <th>Promedio</th>
                    </tr>
                  </thead>
                  <tbody>
                    {boletin.materias.map((m, i) => (
                      <tr key={i}>
                        <td>{m.materiaNombre}</td>
                        <td>{m.promedio.toFixed(2)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                <div className="boletin-promedio-general">
                  <span>Promedio general</span>
                  <span>{boletin.promedioGeneral.toFixed(2)}</span>
                </div>
              </>
            ) : (
              <p className="reportes-vacio">Este estudiante no tiene calificaciones registradas.</p>
            )}
          </div>

          <div className="reporte-card">
            <h2>Resumen de asistencia</h2>
            {resumenAsistencia ? (
              <>
                <div className="asistencia-resumen-grid">
                  <div className="resumen-stat">
                    <span className="resumen-stat-valor">{resumenAsistencia.totalPresente}</span>
                    <span className="resumen-stat-label">Presente</span>
                  </div>
                  <div className="resumen-stat">
                    <span className="resumen-stat-valor">{resumenAsistencia.totalAusente}</span>
                    <span className="resumen-stat-label">Ausente</span>
                  </div>
                  <div className="resumen-stat">
                    <span className="resumen-stat-valor">{resumenAsistencia.totalJustificado}</span>
                    <span className="resumen-stat-label">Justificado</span>
                  </div>
                  <div className="resumen-stat">
                    <span className="resumen-stat-valor">{resumenAsistencia.totalTardanza}</span>
                    <span className="resumen-stat-label">Tardanza</span>
                  </div>
                </div>
                <div className="resumen-porcentaje">
                  <div className="resumen-porcentaje-valor">
                    {resumenAsistencia.porcentajeAsistencia.toFixed(1)}%
                  </div>
                  <div className="resumen-stat-label">Porcentaje de asistencia</div>
                </div>
              </>
            ) : (
              <p className="reportes-vacio">Sin datos de asistencia.</p>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

export default Reportes;