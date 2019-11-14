package com.fri.code.exercises.services.config;

import com.kumuluz.ee.configuration.cdi.ConfigBundle;
import com.kumuluz.ee.configuration.cdi.ConfigValue;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@ConfigBundle("configuration-properties")
public class IntegrationConfiguration {
    @ConfigValue(value = "code-inputs-service.enabled", watch = true)
    private boolean orderServiceEnabled;

    public boolean isOrderServiceEnabled() {
        return orderServiceEnabled;
    }

    public void setOrderServiceEnabled(boolean orderServiceEnabled) {
        this.orderServiceEnabled = orderServiceEnabled;
    }
}
