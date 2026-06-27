package com.sga.unemi.observer;

import com.sga.unemi.model.Calificacion;

public interface CalificacionObserver {
    void onCalificacionRegistrada(Calificacion calificacion);
}