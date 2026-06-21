import { useEffect, useState } from 'react';
import apiClient from '../api/client';
import type { ComunicadoResponse, UsuarioBasico } from '../types/comunicado';
import type { EstudianteResponse } from '../types/estudiante';
import type { DocenteResponse } from '../types/docente';
import type { MateriaResponse } from '../types/materia';
import './Inicio.css';

function Inicio() {
  const nombre = localStorage.getItem('nombre') || 'Usuario';
  const rol = localStorage.getItem('rol') || '';
  const emailActual = localStorage.getItem('email') || '';
  const esRector = rol === 'RECTOR';

  const [loading, setLoading] = useState(true);
  const [totalEstudiantes, setTotalEstudiantes] = useState(0);
  const [totalDocentes, setTotalDocentes] = useState(0);
  const [totalMaterias, setTotalMaterias] = useState(0);
  const [comunicados, setComunicados] = useState<ComunicadoResponse[]>([]);

  useEffect(() => {
    const cargar = async () => {
      setLoading(true);
      try {
        if (esRector) {
          const [estRes, docRes, matRes, comRes] = await Promise.all([
            apiClient.get<EstudianteResponse[]>('/estudiantes'),
            apiClient.get<DocenteResponse[]>('/docentes'),
            apiClient.get<MateriaResponse[]>('/materias'),
            apiClient.get<ComunicadoResponse[]>('/comunicados'),
          ]);
          setTotalEstudiantes(estRes.data.filter((e) => e.activo).length);
          setTotalDocentes(docRes.data.filter((d) => d.activo).length);
          setTotalMaterias(matRes.data.length);
          setComunicados(comRes.data.slice(0, 3));
        } else {
          const [usuRes, comRes] = await Promise.all([
            apiClient.get<UsuarioBasico[]>('/usuarios'),
            apiClient.get<ComunicadoResponse[]>('/comunicados'),
          ]);
          const miId = usuRes.data.find((u) => u.email === emailActual)?.id || '';
          const relevantes = comRes.data.filter(
            (c) => c.destinatarioRol === rol || c.remitenteId === miId
          );
          setComunicados(relevantes.slice(0, 3));
        }
      } catch {
        // si falla, simplemente no mostramos las tarjetas/lista
      } finally {
        setLoading(false);
      }
    };
    cargar();
  }, []);

  return (
    <div className="inicio-container">
      <h1>Bienvenido/a, {nombre}</h1>
      <p>Rol: {rol}</p>

      {esRector && (
        <div className="inicio-stats-grid">
          <div className="inicio-stat-card">
            <span className="inicio-stat-valor">{loading ? '—' : totalEstudiantes}</span>
            <span className="inicio-stat-label">Estudiantes activos</span>
          </div>
          <div className="inicio-stat-card">
            <span className="inicio-stat-valor">{loading ? '—' : totalDocentes}</span>
            <span className="inicio-stat-label">Docentes activos</span>
          </div>
          <div className="inicio-stat-card">
            <span className="inicio-stat-valor">{loading ? '—' : totalMaterias}</span>
            <span className="inicio-stat-label">Materias registradas</span>
          </div>
        </div>
      )}

      <h2 className="inicio-section-title">Últimos comunicados</h2>
      {loading ? (
        <p className="inicio-vacio">Cargando...</p>
      ) : comunicados.length === 0 ? (
        <p className="inicio-vacio">No hay comunicados recientes.</p>
      ) : (
        <div className="inicio-comunicados-lista">
          {comunicados.map((c) => (
            <div className="inicio-comunicado-item" key={c.id}>
              <h4>{c.titulo}</h4>
              <p>{c.contenido}</p>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default Inicio;