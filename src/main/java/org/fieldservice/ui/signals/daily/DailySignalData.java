package org.fieldservice.ui.signals.daily;

import com.google.common.collect.ImmutableMultimap;
import org.fieldservice.ui.EquipmentStatusCode;
import org.fieldservice.ui.util.GuavaCollectors;

import java.util.List;

public class DailySignalData {

    private final ImmutableMultimap<EquipmentStatusCode, DailyCount> _dailyCountByEquipmentStatus;

    public DailySignalData(List<DailySignalResponse> dailySignalResponses) {
        _dailyCountByEquipmentStatus = dailySignalResponses.stream()
                .sorted(new DailySignalResponseComparator())
                .collect(
                        GuavaCollectors.toImmutableMultimap(DailySignalResponse::getEquipmentStatusCode,
                                                            dailySignalResponse -> new DailyCount(dailySignalResponse.getStatusCodeCount(),
                                                                                                  dailySignalResponse.getEntryDate())));
    }

    public ImmutableMultimap<EquipmentStatusCode, DailyCount> getDailyCountByEquipmentStatus() {
        return _dailyCountByEquipmentStatus == null ? ImmutableMultimap.of() : _dailyCountByEquipmentStatus;
    }
}
