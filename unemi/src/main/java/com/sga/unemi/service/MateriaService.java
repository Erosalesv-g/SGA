package com.sga.unemi.service;

import com.sga.unemi.dto.MateriaRequest;
import com.sga.unemi.dto.MateriaResponse;
import com.sga.unemi.model.Docente;
import com.sga.unemi.model.Materia;
import com.sga.unemi.repository.DocenteRepository;
import com.sga.unemi.repository.MateriaRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Lógica de negocio del catálogo de materias.
 * <p>
 * Las materias son datos de alta consulta (se leen constantemente desde
 * Calificaciones, Horarios y Recursos Pedagógicos) y baja frecuencia de
 * cambio, por lo que se cachean en Redis conforme al RNF-0008 (Optimización
 * de Consultas Frecuentes). Si Redis no está disponible, Spring Cache cae
 * de vuelta a consultar PostgreSQL directamente (comportamiento de
 * fallback por defecto de la abstracción de caché).
 */
@Service
public class MateriaService {

    private final MateriaRepository materiaRepository;
    private final DocenteRepository docenteRepository;

    public MateriaService(MateriaRepository materiaRepository, DocenteRepository docenteRepository) {
        this.materiaRepository = materiaRepository;
        this.docenteRepository = docenteRepository;
    }

    /**
     * Lista todas las materias del catálogo.
     * <p>
     * El resultado se guarda en la caché {@code materias} bajo la clave
     * {@code "todas"} la primera vez que se llama; las llamadas siguientes
     * se sirven desde Redis hasta que el catálogo cambie (ver los métodos
     * de creación, actualización y eliminación, que invalidan la caché).
     *
     * @return la lista completa de materias
     */
    @Cacheable(value = "materias", key = "'todas'")
    public List<MateriaResponse> listarMaterias() {
        return materiaRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Obtiene una materia por su id.
     * <p>
     * El resultado se cachea individualmente por id en la caché
     * {@code materias}.
     *
     * @param id id de la materia
     * @return la materia solicitada
     * @throws NoSuchElementException si no existe una materia con ese id
     */
    @Cacheable(value = "materias", key = "#id")
    public MateriaResponse obtenerMateria(UUID id) {
        Materia materia = materiaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Materia no encontrada"));
        return toResponse(materia);
    }

    /**
     * Crea una nueva materia y limpia toda la caché de materias, ya que el
     * listado completo (clave {@code "todas"}) quedaría desactualizado.
     *
     * @param request datos de la materia a crear
     * @return la materia creada
     */
    @CacheEvict(value = "materias", allEntries = true)
    public MateriaResponse crearMateria(MateriaRequest request) {
        Materia materia = new Materia();
        materia.setNombre(request.getNombre());
        materia.setCodigo(request.getCodigo());
        materia.setCreditos(request.getCreditos());
        materia.setNivel(request.getNivel());
        asignarDocente(materia, request.getDocenteId());

        Materia guardada = materiaRepository.save(materia);
        return toResponse(guardada);
    }

    /**
     * Actualiza una materia existente y limpia toda la caché de materias
     * para evitar servir datos desactualizados (tanto el listado completo
     * como la entrada individual de esta materia).
     *
     * @param id      id de la materia a actualizar
     * @param request nuevos datos de la materia
     * @return la materia actualizada
     * @throws NoSuchElementException si la materia o el docente no existen
     */
    @CacheEvict(value = "materias", allEntries = true)
    public MateriaResponse actualizarMateria(UUID id, MateriaRequest request) {
        Materia materia = materiaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Materia no encontrada"));

        materia.setNombre(request.getNombre());
        materia.setCodigo(request.getCodigo());
        materia.setCreditos(request.getCreditos());
        materia.setNivel(request.getNivel());
        asignarDocente(materia, request.getDocenteId());

        Materia actualizada = materiaRepository.save(materia);
        return toResponse(actualizada);
    }

    /**
     * Elimina una materia y limpia toda la caché de materias.
     *
     * @param id id de la materia a eliminar
     */
    @CacheEvict(value = "materias", allEntries = true)
    public void eliminarMateria(UUID id) {
        materiaRepository.deleteById(id);
    }

    /**
     * Asigna (o desasigna, si {@code docenteId} es nulo) el docente
     * responsable de una materia.
     *
     * @param materia   la materia a la que se le asigna el docente
     * @param docenteId id del docente a asignar, o {@code null} para
     *                  desasignar
     * @throws NoSuchElementException si el docente no existe
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
     * Convierte una entidad {@link Materia} a su DTO de respuesta.
     *
     * @param materia la entidad de materia a convertir
     * @return el DTO de respuesta correspondiente
     */
    private MateriaResponse toResponse(Materia materia) {
        return new MateriaResponse(
                materia.getId(),
                materia.getNombre(),
                materia.getCodigo(),
                materia.getCreditos(),
                materia.getNivel(),
                materia.getDocente() != null ? materia.getDocente().getId() : null,
                materia.getDocente() != null ? materia.getDocente().getNombre() : null
        );
    }
}