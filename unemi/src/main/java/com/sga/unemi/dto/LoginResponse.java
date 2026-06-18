package com.sga.unemi.dto;

public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private String email;
    private String rol;
    private String nombre;

    public LoginResponse(String accessToken, String refreshToken,
                         String email, String rol, String nombre) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.email = email;
        this.rol = rol;
        this.nombre = nombre;
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}