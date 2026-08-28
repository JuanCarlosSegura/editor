package com.editordocument.editor.model;

public class ElementoDocumento {
    private String tipo;
    private String texto;

    public ElementoDocumento() {
    }

    public ElementoDocumento(String tipo, String texto) {
        this.tipo = tipo;
        this.texto = texto;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }
}
