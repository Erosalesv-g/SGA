export interface UsuarioRequest {
  nombre: string;
  email: string;
  password: string;
  rol: string;
}

export interface UsuarioResponse {
  id: string;
  nombre: string;
  email: string;
  rol: string;
  activo: boolean;
}