import java.time.DayOfWeek;
import java.time.LocalDate;

public class WeekendHoliday implements HolidayPolicy {
    @Override
    public boolean isHoliday(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }
}
