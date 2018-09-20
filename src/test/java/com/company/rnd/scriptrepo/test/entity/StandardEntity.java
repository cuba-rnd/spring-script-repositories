package com.company.rnd.scriptrepo.test.entity;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.util.UUID;

@MappedSuperclass
public class StandardEntity {

    @Id
    @Column(name = "ID")
    protected UUID id;

    public StandardEntity() {
        id = UUID.randomUUID();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUuid() {
        return id;
    }

    public void setUuid(UUID uuid) {
        this.id = uuid;
    }


}
