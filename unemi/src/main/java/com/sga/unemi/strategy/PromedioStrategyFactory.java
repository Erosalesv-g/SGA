package com.sga.unemi.strategy;
 
import org.springframework.stereotype.Component;
 
@Component
public class PromedioStrategyFactory {
 
    private final PromedioBasicaStrategy basicaStrategy;
    private final PromedioBachilleratoStrategy bachilleratoStrategy;
 
    public PromedioStrategyFactory(PromedioBasicaStrategy basicaStrategy,
                                    PromedioBachilleratoStrategy bachilleratoStrategy) {
        this.basicaStrategy = basicaStrategy;
        this.bachilleratoStrategy = bachilleratoStrategy;
    }
 
    public PromedioStrategy obtenerStrategy(String nivel) {
        if (nivel != null && nivel.contains("°")) {
            return bachilleratoStrategy;
        }
        return basicaStrategy;
    }
}