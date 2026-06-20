export interface DocenteRequest {
  nombre: string;
  email: string;
  password: string;
  cedula: string;
  titulo: string;
  especialidad: string;
}

export interface DocenteResponse {
  id: string;
  nombre: string;
  email: string;
  cedula: string;
  titulo: string;
  especialidad: string;
  activo: boolean;
}