export interface ComunicadoRequest {
  titulo: string;
  contenido: string;
  remitenteId: string;
  destinatarioRol: string;
}

export interface ComunicadoResponse {
  id: string;
  titulo: string;
  contenido: string;
  remitenteId: string;
  remitenteNombre: string;
  destinatarioRol: string;
  fechaEnvio: string;
}

export interface UsuarioBasico {
  id: string;
  nombre: string;
  email: string;
  rol: string;
}