export interface MateriaPromedio {
  materiaNombre: string;
  promedio: number;
}

export interface BoletinResponse {
  estudianteId: string;
  estudianteNombre: string;
  materias: MateriaPromedio[];
  promedioGeneral: number;
}

export interface AsistenciaResumenResponse {
  estudianteId: string;
  estudianteNombre: string;
  totalPresente: number;
  totalAusente: number;
  totalJustificado: number;
  totalTardanza: number;
  porcentajeAsistencia: number;
}