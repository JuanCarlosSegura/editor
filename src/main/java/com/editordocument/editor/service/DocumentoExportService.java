package com.editordocument.editor.service;

import com.editordocument.editor.model.ElementoDocumento;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.editordocument.editor.constants.DocumentoConstants.*;

@Service
public class DocumentoExportService {

    public byte[] aTxt(List<ElementoDocumento> estructura) {
        StringBuilder contenido = new StringBuilder();
        Contadores contadores = new Contadores();

        for (ElementoDocumento elemento : estructura) {
            String tipo = elemento.getTipo();
            String texto = elemento.getTexto();
            switch (tipo) {
                case PRINCIPAL -> {
                    contadores.principal++; contadores.secundario = 0; contadores.terciario = 0;
                    contenido.append('\n').append(contadores.principal).append(". ")
                            .append(texto.toUpperCase()).append("\n=====\n");
                }
                case SECUNDARIO -> {
                    contadores.secundario++; contadores.terciario = 0;
                    contenido.append("\n   ").append(contadores.principal).append('.')
                            .append(contadores.secundario).append(". ").append(texto).append('\n');
                }
                case TERCIARIO -> {
                    contadores.terciario++;
                    contenido.append("      ").append(contadores.principal).append('.')
                            .append(contadores.secundario).append('.').append(contadores.terciario)
                            .append(") ").append(texto).append('\n');
                }
                case DESCRIPCION -> contenido.append("         ").append(texto).append("\n\n");
                default -> throw new IllegalArgumentException("Tipo de elemento no válido: " + tipo);
            }
        }
        return contenido.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] aDocx(List<ElementoDocumento> estructura) throws IOException {
        try (XWPFDocument document = new XWPFDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Contadores contadores = new Contadores();
            for (ElementoDocumento elemento : estructura) {
                XWPFParagraph paragraph = document.createParagraph();
                XWPFRun run = paragraph.createRun();
                run.setFontFamily("Arial");
                configurarDocx(paragraph, run, elemento, contadores);
            }
            document.write(output);
            return output.toByteArray();
        }
    }

    public byte[] aPdf(List<ElementoDocumento> estructura) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, output);
            document.open();
            Contadores contadores = new Contadores();
            Font principal = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(31, 73, 125));
            Font secundario = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, new Color(89, 89, 89));
            Font terciario = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 11, Color.BLACK);
            Font descripcion = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.BLACK);

            for (ElementoDocumento elemento : estructura) {
                Paragraph paragraph = new Paragraph();
                String prefijo;
                switch (elemento.getTipo()) {
                    case PRINCIPAL -> { contadores.principal++; contadores.secundario = 0; contadores.terciario = 0; paragraph.setFont(principal); paragraph.setSpacingBefore(15); paragraph.setSpacingAfter(5); prefijo = contadores.principal + ". "; }
                    case SECUNDARIO -> { contadores.secundario++; contadores.terciario = 0; paragraph.setFont(secundario); paragraph.setSpacingBefore(10); paragraph.setSpacingAfter(4); paragraph.setIndentationLeft(20); prefijo = contadores.principal + "." + contadores.secundario + ". "; }
                    case TERCIARIO -> { contadores.terciario++; paragraph.setFont(terciario); paragraph.setSpacingBefore(5); paragraph.setSpacingAfter(3); paragraph.setIndentationLeft(40); prefijo = contadores.principal + "." + contadores.secundario + "." + contadores.terciario + ") "; }
                    case DESCRIPCION -> { paragraph.setFont(descripcion); paragraph.setSpacingAfter(8); paragraph.setIndentationLeft(60); paragraph.setAlignment(Paragraph.ALIGN_JUSTIFIED); prefijo = ""; }
                    default -> throw new IllegalArgumentException("Tipo de elemento no válido: " + elemento.getTipo());
                }
                paragraph.add(prefijo + elemento.getTexto());
                document.add(paragraph);
            }
            document.close();
            return output.toByteArray();
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo generar el PDF", exception);
        }
    }

    private void configurarDocx(XWPFParagraph paragraph, XWPFRun run, ElementoDocumento elemento, Contadores contadores) {
        switch (elemento.getTipo()) {
            case PRINCIPAL -> { contadores.principal++; contadores.secundario = 0; contadores.terciario = 0; paragraph.setSpacingBefore(300); run.setText(contadores.principal + ". " + elemento.getTexto()); run.setBold(true); run.setFontSize(16); run.setColor("1F497D"); }
            case SECUNDARIO -> { contadores.secundario++; contadores.terciario = 0; paragraph.setSpacingBefore(200); paragraph.setIndentationLeft(360); run.setText(contadores.principal + "." + contadores.secundario + ". " + elemento.getTexto()); run.setBold(true); run.setFontSize(13); run.setColor("595959"); }
            case TERCIARIO -> { contadores.terciario++; paragraph.setSpacingBefore(100); paragraph.setIndentationLeft(720); run.setText(contadores.principal + "." + contadores.secundario + "." + contadores.terciario + ") " + elemento.getTexto()); run.setItalic(true); run.setFontSize(11); }
            case DESCRIPCION -> { paragraph.setSpacingAfter(150); paragraph.setIndentationLeft(1080); paragraph.setAlignment(ParagraphAlignment.DISTRIBUTE); run.setText(elemento.getTexto()); run.setFontSize(11); }
            default -> throw new IllegalArgumentException("Tipo de elemento no válido: " + elemento.getTipo());
        }
    }

    private static final class Contadores {
        private int principal;
        private int secundario;
        private int terciario;
    }
}
