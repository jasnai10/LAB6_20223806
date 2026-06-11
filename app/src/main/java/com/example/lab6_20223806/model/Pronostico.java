package com.example.lab6_20223806.model;

import com.google.firebase.firestore.Exclude;
import java.util.Date;

public class Pronostico {
    @Exclude private String id;
    private String userId;
    private String seleccionA;
    private String seleccionB;
    private Date fechaPartido;
    private long golesA;
    private long golesB;
    private String estado;
    private Date fechaCreacion;

    public Pronostico() {}

    @Exclude public String getId() { return id; }
    @Exclude public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSeleccionA() { return seleccionA; }
    public void setSeleccionA(String seleccionA) { this.seleccionA = seleccionA; }

    public String getSeleccionB() { return seleccionB; }
    public void setSeleccionB(String seleccionB) { this.seleccionB = seleccionB; }

    public Date getFechaPartido() { return fechaPartido; }
    public void setFechaPartido(Date fechaPartido) { this.fechaPartido = fechaPartido; }

    public long getGolesA() { return golesA; }
    public void setGolesA(long golesA) { this.golesA = golesA; }

    public long getGolesB() { return golesB; }
    public void setGolesB(long golesB) { this.golesB = golesB; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
