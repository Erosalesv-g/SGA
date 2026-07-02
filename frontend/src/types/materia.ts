export interface MateriaRequest {
  nombre: string;
  codigo: string;
  creditos: number;
  nivel: string;
  docenteId: string | null;
}

export interface DocenteJornadaInfo {
  docenteId: string;
  docenteNombre: string;
  jornada: string;
}

export interface MateriaResponse {
  id: string;
  nombre: string;
  codigo: string;
  creditos: number;
  nivel: string | null;
  docenteId: string | null;
  docenteNombre: string | null;
  docentesPorJornada: DocenteJornadaInfo[];
}