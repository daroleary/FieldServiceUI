package org.fieldservice.ui.response.signals;

import com.google.common.collect.ImmutableMultimap;
import org.fieldservice.ui.EquipmentStatusCode;
import org.fieldservice.ui.Period;
import org.fieldservice.ui.util.GuavaCollectors;

import java.util.List;

public class SignalData {

    private final ImmutableMultimap<EquipmentStatusCode, DailyCount> _dailyCountByEquipmentStatus;

    public <T extends SignalResponse> SignalData(List<T> signalResponses) {

        _dailyCountByEquipmentStatus = signalResponses.stream()
                .sorted(new DailySignalResponseComparator())
                .collect(
                        GuavaCollectors.toImmutableMultimap(SignalResponse::getEquipmentStatusCode,
                                                            dailySignalResponse -> new DailyCount(dailySignalResponse.getStatusCodeCount(),
                                                                                                  dailySignalResponse.getEntryDate())));
    }

    public ImmutableMultimap<EquipmentStatusCode, DailyCount> getDailyCountByEquipmentStatus() {
        return _dailyCountByEquipmentStatus == null ? ImmutableMultimap.of() : _dailyCountByEquipmentStatus;
    }
}
