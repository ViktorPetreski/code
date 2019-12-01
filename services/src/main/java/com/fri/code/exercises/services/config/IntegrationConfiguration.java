package com.fri.code.exercises.services.config;

import com.kumuluz.ee.configuration.cdi.ConfigBundle;
import com.kumuluz.ee.configuration.cdi.ConfigValue;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@ConfigBundle("configuration-properties")
public class IntegrationConfiguration {
    @ConfigValue(value = "code-inputs.enabled", watch = true)
    private boolean inputsServiceEnabled;

    public boolean isOutputsServiceEnabled() {
        return outputsServiceEnabled;
    }

    public void setOutputsServiceEnabled(boolean outputsServiceEnabled) {
        this.outputsServiceEnabled = outputsServiceEnabled;
    }

    @ConfigValue(value = "code-outputs.enabled", watch = true)
    private boolean outputsServiceEnabled;

    public String getInputsServiceURL() {
        return inputsServiceURL;
    }

    public void setInputsServiceURL(String inputsServiceURL) {
        this.inputsServiceURL = inputsServiceURL;
    }

    @ConfigValue(value = "code-inputs.endpoint", watch = true)
    private String inputsServiceURL;

    public boolean isInputsServiceEnabled() {
        return inputsServiceEnabled;
    }

    public void setInputsServiceEnabled(boolean inputsServiceEnabled) {
        this.inputsServiceEnabled = inputsServiceEnabled;
    }
}
