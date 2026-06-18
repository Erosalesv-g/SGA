package com.sga.unemi.dto;

import java.util.List;
import java.util.UUID;

public class BoletinResponse {

    private UUID estudianteId;
    private String estudianteNombre;
    private List<MateriaPromedio> materias;
    private Double promedioGeneral;

    public BoletinResponse(UUID estudianteId, String estudianteNombre,
                            List<MateriaPromedio> materias, Double promedioGeneral) {
        this.estudianteId = estudianteId;
        this.estudianteNombre = estudianteNombre;
        this.materias = materias;
        this.promedioGeneral = promedioGeneral;
    }

    public UUID getEstudianteId() { return estudianteId; }
    public String getEstudianteNombre() { return estudianteNombre; }
    public List<MateriaPromedio> getMaterias() { return materias; }
    public Double getPromedioGeneral() { return promedioGeneral; }
}