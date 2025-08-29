import java.time.DayOfWeek;
import java.time.LocalDate;

public class Weekend implements Holiday { // 인터페이스 이름이 Holiday라면 거기에 맞춰도 돼
    @Override
    public boolean isHoliday(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }
}
