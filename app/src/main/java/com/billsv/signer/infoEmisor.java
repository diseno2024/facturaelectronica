package com.billsv.signer;

public class infoEmisor {

    private String nombre;
    private String nombreC;
    private String dui;
    private String nit;
    private String nrc;
    private String ActividadEco;
    private String departamento;
    private String municipio;
    private String direccion;
    private String telefono;
    private String correo;
    private String pais = "SV";

    public infoEmisor(String nombre, String nombreC, String dui, String nit, String nrc, String ActividadEco, String departamento, String municipio, String direccion, String telefono, String correo) {
        this.nombre = nombre;
        this.nombreC = nombreC;
        this.dui = dui;
        this.nit = nit;
        this.nrc = nrc;
        this.ActividadEco = ActividadEco;
        this.departamento = departamento;
        this.municipio = municipio;
        this.direccion = direccion;
        this.telefono = telefono;
        this.correo = correo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombrec() {
        return nombreC;
    }

    public void setNombrec(String nombrec) {
        this.nombreC = nombrec;
    }

    public String getDui() {
        return dui;
    }

    public void setDui(String dui) {
        this.dui = dui;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getNrc() {
        return nrc;
    }

    public void setNrc(String nrc) {
        this.nrc = nrc;
    }

    public String getActividadEco() {
        return ActividadEco;
    }

    public void setActividadEco(String ActividadEco) {
        this.ActividadEco = ActividadEco;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPais() {
        return pais;
    }
}