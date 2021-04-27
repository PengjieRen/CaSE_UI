package com.sdu.irlab.chatlabelling.datasource.domain;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Lob;

@Entity
@Cacheable(false)
public class SystemStatus extends BaseEntity {
    private String attrName;

    @Lob
    private String attrValue;

    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    public String getAttrValue() {
        return attrValue;
    }

    public void setAttrValue(String attrValue) {
        this.attrValue = attrValue;
    }
}
