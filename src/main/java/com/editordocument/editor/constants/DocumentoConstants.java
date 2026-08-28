package com.editordocument.editor.constants;

import java.util.Set;
import java.util.regex.Pattern;

public final class DocumentoConstants {

    public static final String PRINCIPAL = "PRINCIPAL";
    public static final String SECUNDARIO = "SECUNDARIO";
    public static final String TERCIARIO = "TERCIARIO";
    public static final String DESCRIPCION = "DESCRIPCION";

    public static final Pattern PATRON_PRINCIPAL = Pattern.compile("^\\d+\\.\\s+.*");
    public static final Pattern PATRON_SECUNDARIO = Pattern.compile("^\\d+\\.\\d+\\.\\s+.*");
    public static final Pattern PATRON_TERCIARIO = Pattern.compile("^\\d+\\.\\d+\\.\\d+\\)?\\s+.*");

    public static final String REEMPLAZO_PRINCIPAL = "^\\d+\\.\\s+";
    public static final String REEMPLAZO_SECUNDARIO = "^\\d+\\.\\d+\\.\\s+";
    public static final String REEMPLAZO_TERCIARIO = "^\\d+\\.\\d+\\.\\d+\\)?\\s+";

    public static final Set<String> FORMATOS_EXPORTACION = Set.of("txt", "docx", "pdf");
    public static final String NOMBRE_TXT = "documento_final.txt";
    public static final String NOMBRE_DOCX = "documento_final.docx";
    public static final String NOMBRE_PDF = "documento_final.pdf";

    private DocumentoConstants() {
    }
}
