package com.sga.unemi.strategy;
 
import com.sga.unemi.model.Calificacion;
import com.sga.unemi.model.TipoCalif;
import org.springframework.stereotype.Component;
 
import java.util.List;
 
@Component
public class PromedioBachilleratoStrategy implements PromedioStrategy {
 
    private static final double PESO_EXAMEN = 0.40;
    private static final double PESO_PARCIAL = 0.30;
    private static final double PESO_PROYECTO = 0.20;
    private static final double PESO_TAREA = 0.10;
 
    @Override
    public Double calcularPromedio(List<Calificacion> calificaciones) {
        if (calificaciones == null || calificaciones.isEmpty()) {
            return 0.0;
        }
 
        double promedioExamen = promedioPorTipo(calificaciones, TipoCalif.EXAMEN);
        double promedioParcial = promedioPorTipo(calificaciones, TipoCalif.PARCIAL);
        double promedioProyecto = promedioPorTipo(calificaciones, TipoCalif.PROYECTO);
        double promedioTarea = promedioPorTipo(calificaciones, TipoCalif.TAREA);
 
        return (promedioExamen * PESO_EXAMEN)
                + (promedioParcial * PESO_PARCIAL)
                + (promedioProyecto * PESO_PROYECTO)
                + (promedioTarea * PESO_TAREA);
    }
 
    private double promedioPorTipo(List<Calificacion> calificaciones, TipoCalif tipo) {
        List<Double> valores = calificaciones.stream()
                .filter(c -> c.getTipo() == tipo)
                .map(Calificacion::getValor)
                .toList();
 
        if (valores.isEmpty()) {
            return 0.0;
        }
 
        return valores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
}