package org.fieldservice.ui.response.signals;

import org.fieldservice.ui.Period;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public class DailySignalResponse extends SignalResponse {

    private LocalDate _entryDate;

    @Override
    protected Period getPeriod() {
        return Period.DAILY;
    }

    @Override
    protected LocalDate getEntryDate() {
        return _entryDate;
    }

    protected void setEntryDate(Date date) {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of("MST7MDT"));
        _entryDate = zonedDateTime.toLocalDate();
    }
}
