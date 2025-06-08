package org.example;

public class DetalleRequest {
    private String idEncabezado;
    private Integer detalleId;
    private String campo1;
    private String campo2;
    // … otros 23 campos

    public DetalleRequest() { }

    public DetalleRequest(String idEncabezado,
                          Integer detalleId,
                          String campo1,
                          String campo2
                          /*, … otros parámetros */) {
        this.idEncabezado = idEncabezado;
        this.detalleId    = detalleId;
        this.campo1       = campo1;
        this.campo2       = campo2;
        // … inicializar los demás campos
    }

    public String getIdEncabezado() {
        return idEncabezado;
    }
    public void setIdEncabezado(String idEncabezado) {
        this.idEncabezado = idEncabezado;
    }

    public Integer getDetalleId() {
        return detalleId;
    }
    public void setDetalleId(Integer detalleId) {
        this.detalleId = detalleId;
    }

    public String getCampo1() {
        return campo1;
    }
    public void setCampo1(String campo1) {
        this.campo1 = campo1;
    }

    public String getCampo2() {
        return campo2;
    }
    public void setCampo2(String campo2) {
        this.campo2 = campo2;
    }

    // … getters y setters para el resto de campos
}
