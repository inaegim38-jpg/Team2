import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Library {
    private Properties dbProps;
    private HolidayPolicy holidayPolicy;

    public Library(HolidayPolicy holidayPolicy) {
        this.holidayPolicy = holidayPolicy;
        this.dbProps = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                System.err.println("Sorry, unable to find db.properties");
                return;
            }
            dbProps.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbProps.getProperty("db.url"),
                dbProps.getProperty("db.username"),
                dbProps.getProperty("db.password"));
    }

    // --- 도서 관리 기능 (CRUD) ---

    /**
     * 새로운 도서를 데이터베이스에 추가합니다.
     * @param title 도서 제목
     * @param author 저자
     * @param isbn ISBN
     * @param publisher 출판사
     * @param stock 재고 수량
     * @return 성공 여부
     */
    public boolean addBook(String title, String author, String isbn, String publisher, int stock) {
        String sql = "INSERT INTO books (title, author, isbn, publisher, stock) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, author);
            pstmt.setString(3, isbn);
            pstmt.setString(4, publisher);
            pstmt.setInt(5, stock);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("도서 '" + title + "' (ISBN: " + isbn + ") 이(가) 추가되었습니다.");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("도서 추가 중 오류 발생: " + e.getMessage());
        }
        return false;
    }

    /**
     * 모든 도서 목록을 조회합니다.
     */
    public void viewAllBooks() {
        String sql = "SELECT book_id, title, author, isbn, publisher, stock FROM books";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("--- 모든 도서 목록 ---");
            while (rs.next()) {
                System.out.printf("ID: %d, 제목: %s, 저자: %s, ISBN: %s, 출판사: %s, 재고: %d%n",
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("isbn"),
                        rs.getString("publisher"),
                        rs.getInt("stock"));
            }
        } catch (SQLException e) {
            System.err.println("도서 목록 조회 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * 제목으로 도서를 검색합니다.
     * @param keyword 검색할 제목 키워드
     */
    public void searchBooksByTitle(String keyword) {
        String sql = "SELECT book_id, title, author, isbn, publisher, stock FROM books WHERE title LIKE ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + keyword + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("--- '" + keyword + "' (으)로 검색된 도서 ---");
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.printf("ID: %d, 제목: %s, 저자: %s, ISBN: %s, 출판사: %s, 재고: %d%n",
                            rs.getInt("book_id"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getString("isbn"),
                            rs.getString("publisher"),
                            rs.getInt("stock"));
                }
                if (!found) {
                    System.out.println("검색 결과가 없습니다.");
                }
            }
        } catch (SQLException e) {
            System.err.println("도서 검색 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * 특정 도서의 정보를 수정합니다. (예: 재고 수량 변경)
     * @param bookId 수정할 도서 ID
     * @param newStock 새 재고 수량
     * @return 성공 여부
     */
    public boolean updateBookStock(int bookId, int newStock) {
        String sql = "UPDATE books SET stock = ? WHERE book_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newStock);
            pstmt.setInt(2, bookId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("도서 ID " + bookId + " 의 재고가 " + newStock + " (으)로 업데이트되었습니다.");
                return true;
            } else {
                System.out.println("도서 ID " + bookId + " 를 찾을 수 없거나 재고 변경이 필요 없습니다.");
            }
        } catch (SQLException e) {
            System.err.println("도서 재고 수정 중 오류 발생: " + e.getMessage());
        }
        return false;
    }

    /**
     * 특정 도서를 데이터베이스에서 삭제합니다.
     * @param bookId 삭제할 도서 ID
     * @return 성공 여부
     */
    public boolean deleteBook(int bookId) {
        // 먼저 해당 도서의 대출 기록이 있는지 확인
        if (hasActiveLoans(bookId)) {
            System.out.println("도서 ID " + bookId + " 는 현재 대출 중인 기록이 있어 삭제할 수 없습니다.");
            return false;
        }

        String sql = "DELETE FROM books WHERE book_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("도서 ID " + bookId + " 이(가) 삭제되었습니다.");
                return true;
            } else {
                System.out.println("도서 ID " + bookId + " 를 찾을 수 없습니다.");
            }
        } catch (SQLException e) {
            System.err.println("도서 삭제 중 오류 발생: " + e.getMessage());
        }
        return false;
    }

    private boolean checkAndDecreaseBookStock(int bookId) {
        String selectSql = "SELECT stock FROM books WHERE book_id = ?";
        String updateSql = "UPDATE books SET stock = stock - 1 WHERE book_id = ? AND stock > 0";

        try (Connection conn = getConnection();
             PreparedStatement selectPstmt = conn.prepareStatement(selectSql);
             PreparedStatement updatePstmt = conn.prepareStatement(updateSql)) {

            selectPstmt.setInt(1, bookId);
            try (ResultSet rs = selectPstmt.executeQuery()) {
                if (rs.next()) {
                    int currentStock = rs.getInt("stock");
                    if (currentStock > 0) {
                        updatePstmt.setInt(1, bookId);
                        int affectedRows = updatePstmt.executeUpdate();
                        return affectedRows > 0;
                    } else {
                        System.out.println("도서 ID " + bookId + " 의 재고가 없습니다.");
                        return false;
                    }
                } else {
                    System.out.println("도서 ID " + bookId + " 를 찾을 수 없습니다.");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("도서 재고 확인/감소 중 오류 발생: " + e.getMessage());
        }
        return false;
    }

    // 도서 재고 증가 (대출 실패 시 롤백용)
    private boolean increaseBookStock(int bookId) {
        String sql = "UPDATE books SET stock = stock + 1 WHERE book_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("도서 재고 증가 중 오류 발생: " + e.getMessage());
        }
        return false;
    }

    /**
     * 휴일을 고려하여 반납 예정일을 계산합니다.
     * @param startDate 시작 날짜
     * @param daysToAdd 추가할 일수
     * @return 계산된 반납 예정일
     */
    private LocalDate calculateDueDate(LocalDate startDate, int daysToAdd) {
        LocalDate currentDate = startDate;
        int addedDays = 0;

        while (addedDays < daysToAdd) {
            currentDate = currentDate.plusDays(1);
            if (!holidayPolicy.isHoliday(currentDate)) {
                addedDays++;
            }
        }
        return currentDate;
    }
}
