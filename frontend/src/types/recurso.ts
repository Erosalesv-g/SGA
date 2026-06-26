export interface RecursoPedagogicoResponse {
  id: string;
  titulo: string;
  descripcion: string | null;
  nombreArchivo: string;
  tipoArchivo: string | null;
  tamanoBytes: number | null;
  materiaId: string;
  materiaNombre: string;
  docenteId: string;
  docenteNombre: string;
  fechaPublicacion: string;
}