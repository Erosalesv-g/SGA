export interface MateriaResponse {
  id: string;
  nombre: string;
  codigo: string;
  creditos: number;
  docenteId: string | null;
  docenteNombre: string | null;
}