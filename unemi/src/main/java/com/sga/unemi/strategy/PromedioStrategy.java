package com.sga.unemi.strategy;

import com.sga.unemi.model.Calificacion;

import java.util.List;

public interface PromedioStrategy {

    Double calcularPromedio(List<Calificacion> calificaciones);
}