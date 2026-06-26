package com.sga.unemi.service;

import com.sga.unemi.dto.RecursoPedagogicoResponse;
import com.sga.unemi.model.Materia;
import com.sga.unemi.model.RecursoPedagogico;
import com.sga.unemi.model.Usuario;
import com.sga.unemi.repository.MateriaRepository;
import com.sga.unemi.repository.RecursoPedagogicoRepository;
import com.sga.unemi.repository.UsuarioRepository;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class RecursoPedagogicoService {

    private final RecursoPedagogicoRepository recursoRepository;
    private final MateriaRepository materiaRepository;
    private final UsuarioRepository usuarioRepository;
    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    public RecursoPedagogicoService(RecursoPedagogicoRepository recursoRepository,
                                     MateriaRepository materiaRepository,
                                     UsuarioRepository usuarioRepository,
                                     MinioClient minioClient) {
        this.recursoRepository = recursoRepository;
        this.materiaRepository = materiaRepository;
        this.usuarioRepository = usuarioRepository;
        this.minioClient = minioClient;
    }

    public List<RecursoPedagogicoResponse> listarTodos() {
        return recursoRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public List<RecursoPedagogicoResponse> listarPorMateria(UUID materiaId) {
        return recursoRepository.findByMateriaId(materiaId).stream()
                .map(this::toResponse)
                .toList();
    }

    public RecursoPedagogicoResponse subir(String titulo, String descripcion, UUID materiaId,
                                            UUID docenteId, MultipartFile archivo) {
        Materia materia = materiaRepository.findById(materiaId)
                .orElseThrow(() -> new NoSuchElementException("Materia no encontrada"));
        Usuario docente = usuarioRepository.findById(docenteId)
                .orElseThrow(() -> new NoSuchElementException("Docente no encontrado"));

        String nombreOriginal = archivo.getOriginalFilename();
        String nombreMinio = UUID.randomUUID() + "_" + nombreOriginal;

        try (InputStream is = archivo.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(nombreMinio)
                            .stream(is, archivo.getSize(), -1)
                            .contentType(archivo.getContentType())
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error al subir el archivo a MinIO: " + e.getMessage(), e);
        }

        RecursoPedagogico recurso = new RecursoPedagogico();
        recurso.setTitulo(titulo);
        recurso.setDescripcion(descripcion);
        recurso.setNombreArchivo(nombreOriginal);
        recurso.setNombreArchivoMinio(nombreMinio);
        recurso.setTipoArchivo(archivo.getContentType());
        recurso.setTamanoBytes(archivo.getSize());
        recurso.setMateria(materia);
        recurso.setDocente(docente);

        RecursoPedagogico guardado = recursoRepository.save(recurso);
        return toResponse(guardado);
    }

    public InputStream descargar(UUID id) {
        RecursoPedagogico recurso = recursoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Recurso no encontrado"));

        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(recurso.getNombreArchivoMinio())
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error al descargar el archivo: " + e.getMessage(), e);
        }
    }

    public RecursoPedagogico obtenerEntidad(UUID id) {
        return recursoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Recurso no encontrado"));
    }

    public void eliminar(UUID id) {
        RecursoPedagogico recurso = recursoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Recurso no encontrado"));

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(recurso.getNombreArchivoMinio())
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar el archivo de MinIO: " + e.getMessage(), e);
        }

        recursoRepository.deleteById(id);
    }

    private RecursoPedagogicoResponse toResponse(RecursoPedagogico r) {
        return new RecursoPedagogicoResponse(
                r.getId(), r.getTitulo(), r.getDescripcion(),
                r.getNombreArchivo(), r.getTipoArchivo(), r.getTamanoBytes(),
                r.getMateria().getId(), r.getMateria().getNombre(),
                r.getDocente().getId(), r.getDocente().getNombre(),
                r.getFechaPublicacion()
        );
    }
}