package com.editordocument.editor.model;

import java.util.List;

public class SolicitudExportacion {
    private List<ElementoDocumento> elementos;
    private ConfiguracionDocumento configuracion = new ConfiguracionDocumento();

    public List<ElementoDocumento> getElementos() {
        return elementos;
    }

    public void setElementos(List<ElementoDocumento> elementos) {
        this.elementos = elementos;
    }

    public ConfiguracionDocumento getConfiguracion() {
        return configuracion;
    }

    public void setConfiguracion(ConfiguracionDocumento configuracion) {
        this.configuracion = configuracion == null ? new ConfiguracionDocumento() : configuracion;
    }
}