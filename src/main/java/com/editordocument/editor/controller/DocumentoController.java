package com.editordocument.editor.controller;

import com.editordocument.editor.model.ElementoDocumento;
import com.editordocument.editor.model.SolicitudExportacion;
import com.editordocument.editor.service.DocumentoExportService;
import com.editordocument.editor.util.DocumentoParser;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static com.editordocument.editor.constants.DocumentoConstants.NOMBRE_DOCX;
import static com.editordocument.editor.constants.DocumentoConstants.NOMBRE_PDF;
import static com.editordocument.editor.constants.DocumentoConstants.NOMBRE_TXT;

@RestController
@RequestMapping("/api/documentos")
@CrossOrigin(origins = "*")
public class DocumentoController {

    private final DocumentoExportService exportService;

    public DocumentoController(DocumentoExportService exportService) {
        this.exportService = exportService;
    }

    @PostMapping("/importar/txt")
    public ResponseEntity<List<ElementoDocumento>> importarTxt(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return ResponseEntity.badRequest().build();
        try {
            return ResponseEntity.ok(DocumentoParser.desdeTxt(file));
        } catch (IOException exception) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/importar/docx")
    public ResponseEntity<List<ElementoDocumento>> importarDocx(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return ResponseEntity.badRequest().build();
        try {
            return ResponseEntity.ok(DocumentoParser.desdeDocx(file));
        } catch (IOException exception) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/exportar/txt")
    public ResponseEntity<byte[]> exportarTxt(@RequestBody SolicitudExportacion solicitud) {
        return archivo(exportService.aTxt(solicitud.getElementos(), solicitud.getConfiguracion()), NOMBRE_TXT, MediaType.TEXT_PLAIN);
    }

    @PostMapping("/exportar/docx")
    public ResponseEntity<byte[]> exportarDocx(@RequestBody SolicitudExportacion solicitud) throws IOException {
        return archivo(exportService.aDocx(solicitud.getElementos(), solicitud.getConfiguracion()), NOMBRE_DOCX,
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
    }

    @PostMapping("/exportar/pdf")
    public ResponseEntity<byte[]> exportarPdf(@RequestBody SolicitudExportacion solicitud) {
        return archivo(exportService.aPdf(solicitud.getElementos(), solicitud.getConfiguracion()), NOMBRE_PDF, MediaType.APPLICATION_PDF);
    }

    private ResponseEntity<byte[]> archivo(byte[] contenido, String nombre, MediaType tipo) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nombre)
                .contentType(tipo)
                .body(contenido);
    }
}
