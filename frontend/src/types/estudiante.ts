export interface EstudianteRequest {
  nombre: string;
  email: string;
  password: string;
  codigo: string;
  nivel: string;
  seccion: string;
}

export interface EstudianteResponse {
  id: string;
  nombre: string;
  email: string;
  codigo: string;
  nivel: string;
  seccion: string;
  activo: boolean;
}