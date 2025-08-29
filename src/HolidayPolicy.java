import java.time.LocalDate;

public interface HolidayPolicy {
    /**
     * 특정 날짜가 휴일인지 여부를 확인합니다.
     * @param date 확인할 날짜
     * @return 휴일이면 true, 아니면 false
     */
    boolean isHoliday(LocalDate date);

    /**
     * 휴일 목록을 가져옵니다.
     * @return 휴일 날짜 목록
     */
    // List<LocalDate> getHolidays(); // 필요시 추가 가능
}
