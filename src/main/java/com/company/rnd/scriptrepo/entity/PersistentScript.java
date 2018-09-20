package com.company.rnd.scriptrepo.entity;

import com.haulmont.cuba.core.entity.StandardEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.List;

@Table(name = "SCRIPTREPO_PERSISTENT_SCRIPT")
@Entity(name = "scriptrepo$PersistentScript")
public class PersistentScript extends StandardEntity {
    private static final long serialVersionUID = 6323743611817286101L;

    @NotNull
    @Column(name = "NAME", nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "persistentScript")
    protected List<PersistentScriptParameter> parameters;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "RETURN_TYPE")
    private String returnType;

    @Column(name = "SOURCE_TEXT")
    private String sourceText;

    @Column(name = "ENABLED")
    private Boolean enabled;

    @Column(name = "SCRIPT_VERSION")
    private Integer scriptVersion;

    public void setParameters(List<PersistentScriptParameter> paremeters) {
        this.parameters = paremeters;
    }

    public List<PersistentScriptParameter> getParameters() {
        return parameters;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getSourceText() {
        return sourceText;
    }

    public void setSourceText(String sourceText) {
        this.sourceText = sourceText;
    }

    public Boolean isEnabled() {
        return getEnabled();
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getScriptVersion() {
        return scriptVersion;
    }

    public void setScriptVersion(Integer scriptVersion) {
        this.scriptVersion = scriptVersion;
    }

}