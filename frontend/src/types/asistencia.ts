export interface AsistenciaRequest {
  fecha: string;
  estado: string;
  estudianteId: string;
  materiaId: string;
}

export interface AsistenciaResponse {
  id: string;
  fecha: string;
  estado: string;
  estudianteId: string;
  estudianteNombre: string;
  materiaId: string;
  materiaNombre: string;
}