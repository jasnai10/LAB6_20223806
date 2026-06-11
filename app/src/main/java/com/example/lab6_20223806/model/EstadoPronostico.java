package com.example.lab6_20223806.model;

public final class EstadoPronostico {
    public static final String PENDIENTE = "PENDIENTE";
    public static final String ACERTADO  = "ACERTADO";
    public static final String FALLADO   = "FALLADO";

    private EstadoPronostico() {}

    public static String displayName(String estado) {
        switch (estado) {
            case PENDIENTE: return "Pendiente";
            case ACERTADO:  return "Acertado";
            case FALLADO:   return "Fallado";
            default: return estado;
        }
    }
}
