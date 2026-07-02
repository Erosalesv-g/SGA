package com.sga.unemi.service;

import com.sga.unemi.dto.DocenteJornadaInfo;
import com.sga.unemi.dto.MateriaRequest;
import com.sga.unemi.dto.MateriaResponse;
import com.sga.unemi.model.Docente;
import com.sga.unemi.model.Horario;
import com.sga.unemi.model.Materia;
import com.sga.unemi.repository.DocenteRepository;
import com.sga.unemi.repository.HorarioRepository;
import com.sga.unemi.repository.MateriaRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Lógica de negocio del catálogo de materias.
 * <p>
 * Las materias son datos de alta consulta (se leen constantemente desde
 * Calificaciones, Horarios y Recursos Pedagógicos) y baja frecuencia de
 * cambio, por lo que se cachean conforme al RNF-0008 (Optimización
 * de Consultas Frecuentes).
 * <p>
 * La columna de docentes se enriquece consultando los horarios asociados
 * a cada materia, agrupando los docentes por jornada (Matutina/Vespertina)
 * según la hora de inicio del horario.
 */
@Service
public class MateriaService {

    private static final LocalTime CORTE_JORNADA = LocalTime.of(13, 0);

    private final MateriaRepository materiaRepository;
    private final DocenteRepository docenteRepository;
    private final HorarioRepository horarioRepository;

    public MateriaService(MateriaRepository materiaRepository,
                          DocenteRepository docenteRepository,
                          HorarioRepository horarioRepository) {
        this.materiaRepository = materiaRepository;
        this.docenteRepository = docenteRepository;
        this.horarioRepository = horarioRepository;
    }

    /**
     * Lista todas las materias del catálogo, incluyendo los docentes
     * asignados por jornada (extraídos de los horarios).
     */
    @Cacheable(value = "materias", key = "'todas'")
    public List<MateriaResponse> listarMaterias() {
        List<Materia> materias = materiaRepository.findAll();
        List<Horario> todosHorarios = horarioRepository.findAll();

        // Agrupar horarios por materia_id
        Map<UUID, List<Horario>> horariosPorMateria = todosHorarios.stream()
                .collect(Collectors.groupingBy(h -> h.getMateria().getId()));

        return materias.stream()
                .map(m -> toResponse(m, horariosPorMateria.getOrDefault(m.getId(), List.of())))
                .toList();
    }

    /**
     * Obtiene una materia por su id, incluyendo docentes por jornada.
     */
    @Cacheable(value = "materias", key = "#id")
    public MateriaResponse obtenerMateria(UUID id) {
        Materia materia = materiaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Materia no encontrada"));
        List<Horario> horarios = horarioRepository.findByMateriaId(id);
        return toResponse(materia, horarios);
    }

    /**
     * Crea una nueva materia y limpia toda la caché de materias.
     */
    @CacheEvict(value = {"materias", "horarios"}, allEntries = true)
    public MateriaResponse crearMateria(MateriaRequest request) {
        Materia materia = new Materia();
        materia.setNombre(request.getNombre());
        materia.setCodigo(request.getCodigo());
        materia.setCreditos(request.getCreditos());
        materia.setNivel(request.getNivel());
        asignarDocente(materia, request.getDocenteId());

        Materia guardada = materiaRepository.save(materia);
        return toResponse(guardada, List.of());
    }

    /**
     * Actualiza una materia existente y limpia toda la caché.
     */
    @CacheEvict(value = {"materias", "horarios"}, allEntries = true)
    public MateriaResponse actualizarMateria(UUID id, MateriaRequest request) {
        Materia materia = materiaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Materia no encontrada"));

        materia.setNombre(request.getNombre());
        materia.setCodigo(request.getCodigo());
        materia.setCreditos(request.getCreditos());
        materia.setNivel(request.getNivel());
        asignarDocente(materia, request.getDocenteId());

        Materia actualizada = materiaRepository.save(materia);
        List<Horario> horarios = horarioRepository.findByMateriaId(id);
        return toResponse(actualizada, horarios);
    }

    /**
     * Elimina una materia y limpia toda la caché.
     */
    @CacheEvict(value = {"materias", "horarios"}, allEntries = true)
    public void eliminarMateria(UUID id) {
        materiaRepository.deleteById(id);
    }

    /**
     * Asigna (o desasigna) el docente directo de una materia.
     */
    private void asignarDocente(Materia materia, UUID docenteId) {
        if (docenteId != null) {
            Docente docente = docenteRepository.findById(docenteId)
                    .orElseThrow(() -> new NoSuchElementException("Docente no encontrado"));
            materia.setDocente(docente);
        } else {
            materia.setDocente(null);
        }
    }

    /**
     * Determina la jornada según la hora de inicio del horario.
     */
    private String determinarJornada(LocalTime horaInicio) {
        return horaInicio.isBefore(CORTE_JORNADA) ? "Matutina" : "Vespertina";
    }

    /**
     * Extrae los docentes únicos por jornada a partir de los horarios
     * asociados a una materia. Si un mismo docente tiene múltiples horarios
     * en la misma jornada, aparece una sola vez.
     */
    private List<DocenteJornadaInfo> extraerDocentesPorJornada(List<Horario> horarios) {
        // Usar un Set para evitar duplicados (mismo docente + misma jornada)
        Set<String> vistos = new HashSet<>();
        List<DocenteJornadaInfo> resultado = new ArrayList<>();

        // Ordenar: Matutina primero, Vespertina después
        List<Horario> ordenados = horarios.stream()
                .sorted(Comparator.comparing(Horario::getHoraInicio))
                .toList();

        for (Horario h : ordenados) {
            String jornada = determinarJornada(h.getHoraInicio());
            String clave = h.getDocente().getId() + "-" + jornada;
            if (vistos.add(clave)) {
                resultado.add(new DocenteJornadaInfo(
                        h.getDocente().getId(),
                        h.getDocente().getNombre(),
                        jornada
                ));
            }
        }

        return resultado;
    }

    /**
     * Convierte una entidad Materia a su DTO de respuesta,
     * incluyendo la lista de docentes por jornada.
     */
    private MateriaResponse toResponse(Materia materia, List<Horario> horarios) {
        List<DocenteJornadaInfo> docentesPorJornada = extraerDocentesPorJornada(horarios);

        return new MateriaResponse(
                materia.getId(),
                materia.getNombre(),
                materia.getCodigo(),
                materia.getCreditos(),
                materia.getNivel(),
                materia.getDocente() != null ? materia.getDocente().getId() : null,
                materia.getDocente() != null ? materia.getDocente().getNombre() : null,
                docentesPorJornada
        );
    }
}
