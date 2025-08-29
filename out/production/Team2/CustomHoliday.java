import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class CustomHoliday implements HolidayPolicy {
    private List<LocalDate> customHolidays;
    private Properties dbProps;

    public CustomHoliday(Properties dbProps) {
        this.dbProps = dbProps;
        this.customHolidays = loadHolidaysFromDatabase();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbProps.getProperty("db.url"),
                dbProps.getProperty("db.username"),
                dbProps.getProperty("db.password"));
    }

    private List<LocalDate> loadHolidaysFromDatabase() {
        List<LocalDate> holidays = new ArrayList<>();
        String sql = "SELECT holiday_date FROM holidays";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                holidays.add(rs.getDate("holiday_date").toLocalDate());
            }
        } catch (SQLException e) {
            System.err.println("데이터베이스에서 휴일 정보를 로드하는 중 오류 발생: " + e.getMessage());
            // 에러를 던지거나 다른 방식으로 처리할 수 있습니다.
        }
        return holidays;
    }

    /**
     * 새로운 휴일을 데이터베이스에 추가합니다.
     * @param date 휴일 날짜
     * @param description 휴일 설명
     */
    public void addHoliday(LocalDate date, String description) {
        String sql = "INSERT INTO holidays (holiday_date, description) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(date));
            pstmt.setString(2, description);
            pstmt.executeUpdate();
            this.customHolidays.add(date); // 로컬 캐시 업데이트
            System.out.println("휴일 '" + description + "' (" + date + ") 이(가) 추가되었습니다.");
        } catch (SQLException e) {
            System.err.println("휴일 추가 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * 데이터베이스에서 휴일을 삭제합니다.
     * @param date 삭제할 휴일 날짜
     */
    public void removeHoliday(LocalDate date) {
        String sql = "DELETE FROM holidays WHERE holiday_date = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(date));
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                this.customHolidays.remove(date); // 로컬 캐시 업데이트
                System.out.println("휴일 (" + date + ") 이(가) 삭제되었습니다.");
            } else {
                System.out.println("해당 날짜의 휴일이 존재하지 않습니다.");
            }
        } catch (SQLException e) {
            System.err.println("휴일 삭제 중 오류 발생: " + e.getMessage());
        }
    }

    @Override
    public boolean isHoliday(LocalDate date) {
        return customHolidays.contains(date);
    }
}