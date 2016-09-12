package org.fieldservice.ui.response.signals;

import com.google.common.collect.ComparisonChain;

import java.util.Comparator;

public class DailySignalResponseComparator implements Comparator<SignalResponse> {

    @Override
    public int compare(SignalResponse o1, SignalResponse o2) {
        return ComparisonChain.start()
                .compare(o1.getEquipmentStatusCode(), o2.getEquipmentStatusCode())
                .compare(o1.getEntryDate(), o2.getEntryDate())
                .compare(o1.getStatusCodeCount(), o2.getStatusCodeCount())
                .compare(o1.getEquipmentId(), o2.getEquipmentId())
                .result();
    }
}
