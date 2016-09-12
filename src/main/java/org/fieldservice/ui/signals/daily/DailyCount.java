package org.fieldservice.ui.signals.daily;

import java.util.Date;
import java.util.Objects;

public class DailyCount {

    private final int _count;
    private final Date _entryDate;

    public DailyCount(int count, Date entryDate) {
        _count = count;
        _entryDate = entryDate;
    }

    public int getCount() {
        return _count;
    }

    public Date getEntryDate() {
        return _entryDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DailyCount that = (DailyCount) o;
        return _count == that._count &&
                Objects.equals(_entryDate, that._entryDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_count, _entryDate);
    }
}
