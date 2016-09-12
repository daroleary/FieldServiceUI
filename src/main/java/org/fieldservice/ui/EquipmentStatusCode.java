package org.fieldservice.ui;

import java.io.Serializable;

public enum EquipmentStatusCode implements Serializable {
    ACTIVE,
    ENGAGED,
    LOAD,
    OVERRIDE,
    UNPLUG;

    public String getName() {
        return name();
    }
}
