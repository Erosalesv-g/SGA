package com.sga.unemi.strategy;
 
import com.sga.unemi.model.Calificacion;
import org.springframework.stereotype.Component;
 
import java.util.List;
 
@Component
public class PromedioBasicaStrategy implements PromedioStrategy {
 
    @Override
    public Double calcularPromedio(List<Calificacion> calificaciones) {
        if (calificaciones == null || calificaciones.isEmpty()) {
            return 0.0;
        }
 
        return calificaciones.stream()
                .map(Calificacion::getValor)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }
}