package org.fieldservice.ui.response.signals;

import org.fieldservice.ui.EquipmentStatusCode;
import org.fieldservice.ui.Period;

import java.time.LocalDate;

public abstract class SignalResponse {

    private int statusCodeCount;
    private Long equipmentId;
    private EquipmentStatusCode equipmentStatusCode;

    public SignalResponse() {
    }

    protected abstract Period getPeriod();

    protected abstract LocalDate getEntryDate();

    public int getStatusCodeCount() {
        return statusCodeCount;
    }

    public void setStatusCodeCount(int count) {
        statusCodeCount = count;
    }

    public Long getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Long id) {
        equipmentId = id;
    }

    public EquipmentStatusCode getEquipmentStatusCode() {
        return equipmentStatusCode;
    }

    public void setEquipmentStatusCode(String status) {
        equipmentStatusCode = EquipmentStatusCode.valueOf(status);
    }
}
