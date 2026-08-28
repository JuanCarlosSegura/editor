package com.editordocument.editor.util;

import com.editordocument.editor.model.ElementoDocumento;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.editordocument.editor.constants.DocumentoConstants.*;

public final class DocumentoParser {

    private DocumentoParser() {
    }

    public static List<ElementoDocumento> desdeTxt(MultipartFile file) throws IOException {
        List<ElementoDocumento> elementos = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                agregarLinea(elementos, linea);
            }
        }
        return elementos;
    }

    public static List<ElementoDocumento> desdeDocx(MultipartFile file) throws IOException {
        List<ElementoDocumento> elementos = new ArrayList<>();
        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String texto = paragraph.getText().trim();
                if (!texto.isEmpty()) {
                    elementos.add(clasificar(texto, getFontSize(paragraph), isItalic(paragraph)));
                }
            }
        }
        return elementos;
    }

    private static void agregarLinea(List<ElementoDocumento> elementos, String linea) {
        String texto = linea.trim();
        if (!texto.isEmpty() && !texto.startsWith("==") && !texto.startsWith("--")) {
            elementos.add(clasificar(texto, 0, false));
        }
    }

    private static ElementoDocumento clasificar(String texto, int fontSize, boolean italic) {
        if (PATRON_PRINCIPAL.matcher(texto).matches() || fontSize >= 16) {
            return elemento(PRINCIPAL, texto, REEMPLAZO_PRINCIPAL);
        }
        if (PATRON_SECUNDARIO.matcher(texto).matches() || fontSize == 13) {
            return elemento(SECUNDARIO, texto, REEMPLAZO_SECUNDARIO);
        }
        if (PATRON_TERCIARIO.matcher(texto).matches() || italic) {
            return elemento(TERCIARIO, texto, REEMPLAZO_TERCIARIO);
        }
        return new ElementoDocumento(DESCRIPCION, texto);
    }

    private static ElementoDocumento elemento(String tipo, String texto, String reemplazo) {
        return new ElementoDocumento(tipo, texto.replaceFirst(reemplazo, ""));
    }

    private static int getFontSize(XWPFParagraph paragraph) {
        return paragraph.getRuns().stream()
                .mapToInt(XWPFRun::getFontSize)
                .filter(size -> size > 0)
                .findFirst()
                .orElse(0);
    }

    private static boolean isItalic(XWPFParagraph paragraph) {
        return paragraph.getRuns().stream().anyMatch(XWPFRun::isItalic);
    }
}
