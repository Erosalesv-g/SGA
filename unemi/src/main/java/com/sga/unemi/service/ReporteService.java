package com.sga.unemi.service;

import com.sga.unemi.dto.AsistenciaResumenResponse;
import com.sga.unemi.dto.BoletinResponse;
import com.sga.unemi.dto.MateriaPromedio;
import com.sga.unemi.model.Asistencia;
import com.sga.unemi.model.Calificacion;
import com.sga.unemi.model.EstadoAsist;
import com.sga.unemi.model.Estudiante;
import com.sga.unemi.repository.AsistenciaRepository;
import com.sga.unemi.repository.CalificacionRepository;
import com.sga.unemi.repository.EstudianteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReporteService {

    private final CalificacionRepository calificacionRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final EstudianteRepository estudianteRepository;

    public ReporteService(CalificacionRepository calificacionRepository,
                           AsistenciaRepository asistenciaRepository,
                           EstudianteRepository estudianteRepository) {
        this.calificacionRepository = calificacionRepository;
        this.asistenciaRepository = asistenciaRepository;
        this.estudianteRepository = estudianteRepository;
    }

    public BoletinResponse generarBoletin(UUID estudianteId) {
        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

        List<Calificacion> calificaciones = calificacionRepository.findByEstudianteId(estudianteId);

        if (calificaciones.isEmpty()) {
            return new BoletinResponse(estudiante.getId(), estudiante.getNombre(), List.of(), 0.0);
        }

        Map<String, Double> promediosPorMateria = calificaciones.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getMateria().getNombre(),
                        Collectors.averagingDouble(Calificacion::getValor)
                ));

        List<MateriaPromedio> materias = promediosPorMateria.entrySet().stream()
                .map(e -> new MateriaPromedio(e.getKey(), Math.round(e.getValue() * 100) / 100.0))
                .collect(Collectors.toList());

        double promedioGeneral = calificaciones.stream()
                .mapToDouble(Calificacion::getValor)
                .average()
                .orElse(0.0);

        return new BoletinResponse(estudiante.getId(), estudiante.getNombre(),
                materias, Math.round(promedioGeneral * 100) / 100.0);
    }

    public AsistenciaResumenResponse generarResumenAsistencia(UUID estudianteId) {
        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

        List<Asistencia> registros = asistenciaRepository.findByEstudianteId(estudianteId);

        long presente = registros.stream().filter(a -> a.getEstado() == EstadoAsist.P).count();
        long ausente = registros.stream().filter(a -> a.getEstado() == EstadoAsist.A).count();
        long justificado = registros.stream().filter(a -> a.getEstado() == EstadoAsist.J).count();
        long tardanza = registros.stream().filter(a -> a.getEstado() == EstadoAsist.T).count();

        long total = registros.size();
        double porcentaje = total > 0 ? Math.round((presente * 10000.0) / total) / 100.0 : 0.0;

        return new AsistenciaResumenResponse(estudiante.getId(), estudiante.getNombre(),
                presente, ausente, justificado, tardanza, porcentaje);
    }
}