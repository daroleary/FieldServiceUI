package org.fieldservice.ui.response.signals;

import org.fieldservice.ui.Period;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;

public class MonthlySignalResponse extends SignalResponse {

    private Month _month;
    private int _year;

    public Month getMonth() {
        return _month;
    }

    public void setMonth(String month) {
        _month = Month.valueOf(month);
    }

    public int getYear() {
        return _year;
    }

    public void setYear(int year) {
        _year = year;
    }

    @Override
    protected Period getPeriod() {
        return Period.MONTHLY;
    }

    @Override
    protected LocalDate getEntryDate() {
        return YearMonth.of(getYear(), getMonth()).atEndOfMonth();
    }
}
