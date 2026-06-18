package com.sga.unemi.dto;

public class MateriaPromedio {

    private String materiaNombre;
    private Double promedio;

    public MateriaPromedio(String materiaNombre, Double promedio) {
        this.materiaNombre = materiaNombre;
        this.promedio = promedio;
    }

    public String getMateriaNombre() { return materiaNombre; }
    public Double getPromedio() { return promedio; }
}