package com.sga.unemi.service;

import com.sga.unemi.dto.HorarioRequest;
import com.sga.unemi.dto.HorarioResponse;
import com.sga.unemi.model.Docente;
import com.sga.unemi.model.Horario;
import com.sga.unemi.model.Materia;
import com.sga.unemi.repository.DocenteRepository;
import com.sga.unemi.repository.HorarioRepository;
import com.sga.unemi.repository.MateriaRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Lógica de negocio del módulo de horarios (RF-05).
 * <p>
 * Los horarios son datos de alta consulta (estudiantes, docentes y
 * representantes los revisan constantemente) y baja frecuencia de cambio,
 * por lo que se cachean en Redis conforme al RNF-0008 (Optimización de
 * Consultas Frecuentes), siguiendo el mismo enfoque que {@link MateriaService}.
 */
@Service
public class HorarioService {

    private final HorarioRepository horarioRepository;
    private final DocenteRepository docenteRepository;
    private final MateriaRepository materiaRepository;

    public HorarioService(HorarioRepository horarioRepository,
                           DocenteRepository docenteRepository,
                           MateriaRepository materiaRepository) {
        this.horarioRepository = horarioRepository;
        this.docenteRepository = docenteRepository;
        this.materiaRepository = materiaRepository;
    }

    /**
     * Lista todos los horarios registrados.
     * <p>
     * El resultado se guarda en la caché {@code horarios} bajo la clave
     * {@code "todos"}; las llamadas siguientes se sirven desde Redis hasta
     * que el horario cambie (ver los métodos de creación, actualización y
     * eliminación, que invalidan la caché).
     *
     * @return la lista completa de horarios
     */
    @Cacheable(value = "horarios", key = "'todos'")
    public List<HorarioResponse> listarTodos() {
        return horarioRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lista los horarios de un docente específico.
     * <p>
     * El resultado se cachea individualmente por id de docente en la
     * caché {@code horarios}.
     *
     * @param docenteId id del docente
     * @return los horarios de ese docente
     */
    @Cacheable(value = "horarios", key = "#docenteId")
    public List<HorarioResponse> listarPorDocente(UUID docenteId) {
        return horarioRepository.findByDocenteId(docenteId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Registra un nuevo horario, validando que no haya conflicto con otro
     * horario del mismo docente, y limpia toda la caché de horarios.
     *
     * @param request datos del horario a crear
     * @return el horario creado
     * @throws RuntimeException si el docente o la materia no existen, si la
     *                          hora de inicio es posterior a la de fin, o si
     *                          hay un conflicto de horario con otra clase
     */
    @CacheEvict(value = "horarios", allEntries = true)
    public HorarioResponse crear(HorarioRequest request) {
        Docente docente = docenteRepository.findById(request.getDocenteId())
                .orElseThrow(() -> new RuntimeException("Docente no encontrado"));
        Materia materia = materiaRepository.findById(request.getMateriaId())
                .orElseThrow(() -> new RuntimeException("Materia no encontrada"));

        if (request.getHoraInicio().isAfter(request.getHoraFin())) {
            throw new RuntimeException("La hora de inicio debe ser antes que la hora de fin");
        }

        boolean hayConflicto = horarioRepository.findByDocenteId(request.getDocenteId()).stream()
                .filter(h -> h.getDiaSemana().equals(request.getDiaSemana()))
                .anyMatch(h -> request.getHoraInicio().isBefore(h.getHoraFin())
                        && request.getHoraFin().isAfter(h.getHoraInicio()));

        if (hayConflicto) {
            throw new RuntimeException("El docente ya tiene una clase asignada en ese día y horario");
        }

        Horario horario = new Horario();
        horario.setDocente(docente);
        horario.setMateria(materia);
        horario.setDiaSemana(request.getDiaSemana());
        horario.setHoraInicio(request.getHoraInicio());
        horario.setHoraFin(request.getHoraFin());
        horario.setAula(request.getAula());
        horario.setPeriodo(request.getPeriodo());

        Horario guardado = horarioRepository.save(horario);
        return toResponse(guardado);
    }

    /**
     * Obtiene un horario por su id.
     *
     * @param id id del horario
     * @return el horario solicitado
     * @throws RuntimeException si no existe un horario con ese id
     */
    public HorarioResponse obtener(UUID id) {
        Horario horario = horarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Horario no encontrado"));
        return toResponse(horario);
    }

    /**
     * Actualiza un horario existente y limpia toda la caché de horarios.
     *
     * @param id      id del horario a actualizar
     * @param request nuevos datos del horario
     * @return el horario actualizado
     * @throws RuntimeException si el horario no existe
     */
    @CacheEvict(value = "horarios", allEntries = true)
    public HorarioResponse actualizar(UUID id, HorarioRequest request) {
        Horario horario = horarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Horario no encontrado"));

        horario.setDiaSemana(request.getDiaSemana());
        horario.setHoraInicio(request.getHoraInicio());
        horario.setHoraFin(request.getHoraFin());
        horario.setAula(request.getAula());
        horario.setPeriodo(request.getPeriodo());

        Horario actualizado = horarioRepository.save(horario);
        return toResponse(actualizado);
    }

    /**
     * Elimina un horario y limpia toda la caché de horarios.
     *
     * @param id id del horario a eliminar
     */
    @CacheEvict(value = "horarios", allEntries = true)
    public void eliminar(UUID id) {
        horarioRepository.deleteById(id);
    }

    /**
     * Convierte una entidad {@link Horario} a su DTO de respuesta.
     *
     * @param h la entidad de horario a convertir
     * @return el DTO de respuesta correspondiente
     */
    private HorarioResponse toResponse(Horario h) {
        return new HorarioResponse(
                h.getId(), h.getDocente().getId(), h.getDocente().getNombre(),
                h.getMateria().getId(), h.getMateria().getNombre(),
                h.getDiaSemana(), h.getHoraInicio(), h.getHoraFin(),
                h.getAula(), h.getPeriodo()
        );
    }
}