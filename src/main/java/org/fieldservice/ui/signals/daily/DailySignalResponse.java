package org.fieldservice.ui.signals.daily;

import org.fieldservice.ui.EquipmentStatusCode;

import java.util.Date;

public class DailySignalResponse {

    private int statusCodeCount;
    private Long equipmentId;
    private EquipmentStatusCode equipmentStatusCode;
    private Date entryDate;

    public DailySignalResponse() {
    }

    //TODO: returns the types you should return
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

    public Date getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(Date date) {
        entryDate = date;
    }
}
