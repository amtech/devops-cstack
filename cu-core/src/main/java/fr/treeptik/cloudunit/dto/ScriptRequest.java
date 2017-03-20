package fr.treeptik.cloudunit.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by stagiaire on 10/06/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScriptRequest {

    private String scriptName;

    private String scriptContent;

    public String getScriptName() {
        return scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public String getScriptContent() {
        return scriptContent;
    }

    public void setScriptContent(String scriptContent) {
        this.scriptContent = scriptContent;
    }
}
