package com.editordocument.editor.service;

import com.editordocument.editor.model.ElementoDocumento;
import com.editordocument.editor.model.ConfiguracionDocumento;
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
import java.util.Map;

import static com.editordocument.editor.constants.DocumentoConstants.*;

@Service
public class DocumentoExportService {

    public byte[] aTxt(List<ElementoDocumento> estructura) {
        return aTxt(estructura, new ConfiguracionDocumento());
    }

    public byte[] aTxt(List<ElementoDocumento> estructura, ConfiguracionDocumento configuracion) {
        StringBuilder contenido = new StringBuilder();
        Contadores contadores = new Contadores();
        FormatosActivos formatos = new FormatosActivos(configuracion);

        for (ElementoDocumento elemento : estructura) {
            String tipo = elemento.getTipo();
            String texto = elemento.getTexto();
            String formato = formatos.para(tipo);
            switch (tipo) {
                case PRINCIPAL -> {
                    contadores.principal++; contadores.secundario = 0; contadores.terciario = 0;
                    contenido.append('\n').append(prefijo(formato, contadores.principal, 0, 0))
                            .append(texto.toUpperCase()).append(formato.equals("PUNTOS") ? "\n" : "\n=====\n");
                }
                case SECUNDARIO -> {
                    contadores.secundario++; contadores.terciario = 0;
                    contenido.append(prefijo(formato, contadores.principal, contadores.secundario, 0))
                            .append(texto).append('\n');
                }
                case TERCIARIO -> {
                    contadores.terciario++;
                    contenido.append(prefijo(formato, contadores.principal, contadores.secundario, contadores.terciario))
                            .append(texto).append('\n');
                }
                case DESCRIPCION -> contenido.append(formato.equals("PUNTOS") ? "            " : "         ")
                        .append(texto).append("\n\n");
                default -> throw new IllegalArgumentException("Tipo de elemento no válido: " + tipo);
            }
        }
        return contenido.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String prefijo(String formato, int principal, int secundario, int terciario) {
        int sangria = terciario > 0 ? 8 : secundario > 0 ? 4 : 0;
        String espacios = " ".repeat(sangria);
        if (formato.equals("PUNTOS")) return espacios + "• ";
        if (terciario > 0) return "      " + principal + "." + secundario + "." + terciario + ") ";
        if (secundario > 0) return "   " + principal + "." + secundario + ". ";
        return principal + ". ";
    }

    public byte[] aDocx(List<ElementoDocumento> estructura) throws IOException {
        return aDocx(estructura, new ConfiguracionDocumento());
    }

    public byte[] aDocx(List<ElementoDocumento> estructura, ConfiguracionDocumento configuracion) throws IOException {
        try (XWPFDocument document = new XWPFDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Contadores contadores = new Contadores();
            FormatosActivos formatos = new FormatosActivos(configuracion);
            for (ElementoDocumento elemento : estructura) {
                XWPFParagraph paragraph = document.createParagraph();
                XWPFRun run = paragraph.createRun();
                run.setFontFamily("Arial");
                configurarDocx(paragraph, run, elemento, contadores, formatos.para(elemento.getTipo()));
            }
            document.write(output);
            return output.toByteArray();
        }
    }

    public byte[] aPdf(List<ElementoDocumento> estructura) {
        return aPdf(estructura, new ConfiguracionDocumento());
    }

    public byte[] aPdf(List<ElementoDocumento> estructura, ConfiguracionDocumento configuracion) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, output);
            document.open();
            Contadores contadores = new Contadores();
            FormatosActivos formatos = new FormatosActivos(configuracion);
            Font principal = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(31, 73, 125));
            Font secundario = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, new Color(89, 89, 89));
            Font terciario = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 11, Color.BLACK);
            Font descripcion = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.BLACK);

            for (ElementoDocumento elemento : estructura) {
                Paragraph paragraph = new Paragraph();
                String prefijo;
                boolean puntos = formatos.para(elemento.getTipo()).equals("PUNTOS");
                switch (elemento.getTipo()) {
                    case PRINCIPAL -> { contadores.principal++; contadores.secundario = 0; contadores.terciario = 0; paragraph.setFont(principal); paragraph.setSpacingBefore(15); paragraph.setSpacingAfter(5); prefijo = puntos ? "• " : contadores.principal + ". "; }
                    case SECUNDARIO -> { contadores.secundario++; contadores.terciario = 0; paragraph.setFont(secundario); paragraph.setSpacingBefore(10); paragraph.setSpacingAfter(4); paragraph.setIndentationLeft(20); prefijo = puntos ? "• " : contadores.principal + "." + contadores.secundario + ". "; }
                    case TERCIARIO -> { contadores.terciario++; paragraph.setFont(terciario); paragraph.setSpacingBefore(5); paragraph.setSpacingAfter(3); paragraph.setIndentationLeft(40); prefijo = puntos ? "• " : contadores.principal + "." + contadores.secundario + "." + contadores.terciario + ") "; }
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

    private void configurarDocx(XWPFParagraph paragraph, XWPFRun run, ElementoDocumento elemento,
                                Contadores contadores, String formato) {
        boolean puntos = formato.equals("PUNTOS");
        switch (elemento.getTipo()) {
            case PRINCIPAL -> { contadores.principal++; contadores.secundario = 0; contadores.terciario = 0; paragraph.setSpacingBefore(300); run.setText(puntos ? "• " + elemento.getTexto() : contadores.principal + ". " + elemento.getTexto()); run.setBold(true); run.setFontSize(16); run.setColor("1F497D"); }
            case SECUNDARIO -> { contadores.secundario++; contadores.terciario = 0; paragraph.setSpacingBefore(200); paragraph.setIndentationLeft(360); run.setText(puntos ? "• " + elemento.getTexto() : contadores.principal + "." + contadores.secundario + ". " + elemento.getTexto()); run.setBold(true); run.setFontSize(13); run.setColor("595959"); }
            case TERCIARIO -> { contadores.terciario++; paragraph.setSpacingBefore(100); paragraph.setIndentationLeft(720); run.setText(puntos ? "• " + elemento.getTexto() : contadores.principal + "." + contadores.secundario + "." + contadores.terciario + ") " + elemento.getTexto()); run.setItalic(true); run.setFontSize(11); }
            case DESCRIPCION -> { paragraph.setSpacingAfter(150); paragraph.setIndentationLeft(1080); paragraph.setAlignment(ParagraphAlignment.DISTRIBUTE); run.setText(elemento.getTexto()); run.setFontSize(11); }
            default -> throw new IllegalArgumentException("Tipo de elemento no válido: " + elemento.getTipo());
        }
    }

    private static final class Contadores {
        private int principal;
        private int secundario;
        private int terciario;
    }

    private static final class FormatosActivos {
        private final String general;
        private final Map<String, String> porNivel;
        private final String[] niveles = {PRINCIPAL, SECUNDARIO, TERCIARIO, DESCRIPCION};
        private final String[] heredados = new String[niveles.length];

        private FormatosActivos(ConfiguracionDocumento configuracion) {
            if (configuracion == null) configuracion = new ConfiguracionDocumento();
            general = "PUNTOS".equals(configuracion.getFormato()) ? "PUNTOS" : "NUMEROS";
            porNivel = configuracion.getFormatosPorNivel() == null ? Map.of() : configuracion.getFormatosPorNivel();
        }

        private String para(String tipo) {
            int nivel = nivel(tipo);
            String formato = general;
            for (int i = nivel; i >= 0; i--) {
                if (heredados[i] != null) { formato = heredados[i]; break; }
            }
            String configurado = porNivel.get(tipo);
            if (configurado != null && ("PUNTOS".equals(configurado) || "NUMEROS".equals(configurado))) {
                heredados[nivel] = configurado;
                for (int i = nivel + 1; i < heredados.length; i++) heredados[i] = null;
            }
            return formato;
        }

        private int nivel(String tipo) {
            for (int i = 0; i < niveles.length; i++) if (niveles[i].equals(tipo)) return i;
            throw new IllegalArgumentException("Tipo de elemento no válido: " + tipo);
        }
    }
}
