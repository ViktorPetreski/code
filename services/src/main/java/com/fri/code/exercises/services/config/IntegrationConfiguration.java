package com.fri.code.exercises.services.config;

import com.kumuluz.ee.configuration.cdi.ConfigBundle;
import com.kumuluz.ee.configuration.cdi.ConfigValue;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@ConfigBundle("configuration-properties")
public class IntegrationConfiguration {
    @ConfigValue(value = "code-inputs.enabled", watch = true)
    private boolean inputsServiceEnabled;

    public boolean isInputsServiceEnabled() {
        return inputsServiceEnabled;
    }

    public void setInputsServiceEnabled(boolean inputsServiceEnabled) {
        this.inputsServiceEnabled = inputsServiceEnabled;
    }
}
