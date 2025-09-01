import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Anniversary implements Holiday {
    List<LocalDate> holidays = new ArrayList<>();

    public void addHoliday(LocalDate date){
        holidays.add(date);
    }

    @Override
    public boolean isHoliday(LocalDate date) {
        return holidays.contains(date);
    }
}