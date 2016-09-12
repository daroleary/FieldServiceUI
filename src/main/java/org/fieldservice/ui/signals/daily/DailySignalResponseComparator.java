package org.fieldservice.ui.signals.daily;

import com.google.common.collect.ComparisonChain;

import java.util.Comparator;

public class DailySignalResponseComparator implements Comparator<DailySignalResponse> {

    @Override
    public int compare(DailySignalResponse o1, DailySignalResponse o2) {
        return ComparisonChain.start()
                .compare(o1.getEquipmentStatusCode(), o2.getEquipmentStatusCode())
                .compare(o1.getEntryDate(), o2.getEntryDate())
                .compare(o1.getStatusCodeCount(), o2.getStatusCodeCount())
                .compare(o1.getEquipmentId(), o2.getEquipmentId())
                .result();
    }
}
