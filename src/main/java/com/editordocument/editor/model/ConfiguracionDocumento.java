package com.editordocument.editor.model;

import java.util.HashMap;
import java.util.Map;

public class ConfiguracionDocumento {
    private String formato = "NUMEROS";
    private Map<String, String> formatosPorNivel = new HashMap<>();

    public String getFormato() {
        return formato;
    }

    public void setFormato(String formato) {
        this.formato = formato;
    }

    public Map<String, String> getFormatosPorNivel() {
        return formatosPorNivel;
    }

    public void setFormatosPorNivel(Map<String, String> formatosPorNivel) {
        this.formatosPorNivel = formatosPorNivel == null ? new HashMap<>() : formatosPorNivel;
    }
}