package org.fieldservice.ui.response.signals;

import org.fieldservice.ui.Period;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;

public class YearlySignalResponse extends SignalResponse {

    private int _year;

    public int getYear() {
        return _year;
    }

    public void setYear(int year) {
        _year = year;
    }

    @Override
    protected Period getPeriod() {
        return Period.YEARLY;
    }

    @Override
    protected LocalDate getEntryDate() {
        return YearMonth.of(getYear(), Month.DECEMBER).atEndOfMonth();
    }
}
