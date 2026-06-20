export interface HorarioRequest {
  docenteId: string;
  materiaId: string;
  diaSemana: string;
  horaInicio: string;
  horaFin: string;
  aula: string;
  periodo: string;
}

export interface HorarioResponse {
  id: string;
  docenteId: string;
  docenteNombre: string;
  materiaId: string;
  materiaNombre: string;
  diaSemana: string;
  horaInicio: string;
  horaFin: string;
  aula: string;
  periodo: string;
}