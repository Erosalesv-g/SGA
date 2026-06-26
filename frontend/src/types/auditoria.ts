export interface AuditoriaLogResponse {
  id: string;
  usuarioNombre: string;
  accion: string;
  entidad: string;
  entidadId: string | null;
  descripcion: string;
  fecha: string;
}