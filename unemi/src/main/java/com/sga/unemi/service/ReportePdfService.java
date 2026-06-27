package com.sga.unemi.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.sga.unemi.dto.AsistenciaResumenResponse;
import com.sga.unemi.dto.BoletinResponse;
import com.sga.unemi.dto.MateriaPromedio;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class ReportePdfService {

    private static final Font TITULO = new Font(Font.HELVETICA, 16, Font.BOLD);
    private static final Font SUBTITULO = new Font(Font.HELVETICA, 12, Font.BOLD);
    private static final Font NORMAL = new Font(Font.HELVETICA, 11, Font.NORMAL);

    public byte[] generarBoletinPdf(BoletinResponse boletin) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph("Unidad Educativa Fiscal Durán", TITULO));
            document.add(new Paragraph("Boletín de Calificaciones", SUBTITULO));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Estudiante: " + boletin.getEstudianteNombre(), NORMAL));
            document.add(new Paragraph(" "));

            PdfPTable tabla = new PdfPTable(2);
            tabla.setWidthPercentage(100);

            PdfPCell encabezadoMateria = new PdfPCell(new Phrase("Materia", SUBTITULO));
            PdfPCell encabezadoPromedio = new PdfPCell(new Phrase("Promedio", SUBTITULO));
            tabla.addCell(encabezadoMateria);
            tabla.addCell(encabezadoPromedio);

            for (MateriaPromedio mp : boletin.getMaterias()) {
                tabla.addCell(new Phrase(mp.getMateriaNombre(), NORMAL));
                tabla.addCell(new Phrase(String.valueOf(mp.getPromedio()), NORMAL));
            }

            document.add(tabla);
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Promedio General: " + boletin.getPromedioGeneral(), SUBTITULO));

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Error al generar el PDF del boletín: " + e.getMessage(), e);
        }

        return out.toByteArray();
    }

    public byte[] generarAsistenciaPdf(AsistenciaResumenResponse resumen) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph("Unidad Educativa Fiscal Durán", TITULO));
            document.add(new Paragraph("Resumen de Asistencia", SUBTITULO));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Estudiante: " + resumen.getEstudianteNombre(), NORMAL));
            document.add(new Paragraph(" "));

            PdfPTable tabla = new PdfPTable(2);
            tabla.setWidthPercentage(100);

            tabla.addCell(new Phrase("Presente", SUBTITULO));
            tabla.addCell(new Phrase(String.valueOf(resumen.getTotalPresente()), NORMAL));

            tabla.addCell(new Phrase("Ausente", SUBTITULO));
            tabla.addCell(new Phrase(String.valueOf(resumen.getTotalAusente()), NORMAL));

            tabla.addCell(new Phrase("Justificado", SUBTITULO));
            tabla.addCell(new Phrase(String.valueOf(resumen.getTotalJustificado()), NORMAL));

            tabla.addCell(new Phrase("Tardanza", SUBTITULO));
            tabla.addCell(new Phrase(String.valueOf(resumen.getTotalTardanza()), NORMAL));

            document.add(tabla);
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Porcentaje de asistencia: " + resumen.getPorcentajeAsistencia() + "%", SUBTITULO));

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Error al generar el PDF de asistencia: " + e.getMessage(), e);
        }

        return out.toByteArray();
    }
}