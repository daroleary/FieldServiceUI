package org.fieldservice.ui.signals.daily;

public class EquipmentResponse {

    private Long equipmentId;
    private String assetNumber;

    public EquipmentResponse() {
    }

    public EquipmentResponse(Long id, String asset) {
        equipmentId = id;
        assetNumber = asset;
    }

    public Long getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Long id) {
        equipmentId = id;
    }

    public String getAssetNumber() {
        return assetNumber;
    }

    public void setAssetNumber(String asset) {
        assetNumber = asset;
    }
}
