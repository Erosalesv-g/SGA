export interface CalificacionRequest {
  valor: number;
  tipo: string;
  fechaRegistro: string;
  estudianteId: string;
  materiaId: string;
  docenteId: string;
}

export interface CalificacionResponse {
  id: string;
  valor: number;
  tipo: string;
  fechaRegistro: string;
  estudianteId: string;
  estudianteNombre: string;
  materiaId: string;
  materiaNombre: string;
  docenteId: string;
  docenteNombre: string;
}