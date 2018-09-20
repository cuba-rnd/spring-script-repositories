package com.company.rnd.scriptrepo.entity;

import com.haulmont.cuba.core.entity.StandardEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Table(name = "SCRIPTREPO_PERSISTENT_SCRIPT_PARAMETER")
@Entity(name = "scriptrepo$PersistentScriptParameter")
public class PersistentScriptParameter extends StandardEntity {
    private static final long serialVersionUID = 6323743611817286101L;

    @NotNull
    @Column(name = "PARAM_ORDER", nullable = false)
    private Integer paramOrder;

    @NotNull
    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "PARAMETER_TYPE")
    private String parameterType;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PERSISTENT_SCRIPT_ID")
    protected PersistentScript persistentScript;

    public void setPersistentScript(PersistentScript persistentScript) {
        this.persistentScript = persistentScript;
    }

    public PersistentScript getPersistentScript() {
        return persistentScript;
    }


    public Integer getParamOrder() {
        return paramOrder;
    }

    public void setParamOrder(Integer paramOrder) {
        this.paramOrder = paramOrder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParameterType() {
        return parameterType;
    }

    public void setParameterType(String parameterType) {
        this.parameterType = parameterType;
    }
}