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

    // --- 회원 관리 기능 (CRUD) ---

    /**
     * 새로운 회원을 데이터베이스에 등록합니다.
     * @param name 회원 이름
     * @param phoneNumber 회원 전화번호
     * @return 성공 여부
     */
    public boolean addMember(String name, String phoneNumber) {
        String sql = "INSERT INTO members (name, phone_number) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, phoneNumber);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("회원 '" + name + "' (전화번호: " + phoneNumber + ") 이(가) 등록되었습니다.");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("회원 등록 중 오류 발생: " + e.getMessage());
        }
        return false;
    }

    /**
     * 모든 회원 목록을 조회합니다.
     */
    public void viewAllMembers() {
        String sql = "SELECT member_id, name, phone_number FROM members";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("--- 모든 회원 목록 ---");
            while (rs.next()) {
                System.out.printf("ID: %d, 이름: %s, 전화번호: %s%n",
                        rs.getInt("member_id"),
                        rs.getString("name"),
                        rs.getString("phone_number"));
            }
        } catch (SQLException e) {
            System.err.println("회원 목록 조회 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * 특정 회원의 전화번호를 수정합니다.
     * @param memberId 수정할 회원 ID
     * @param newPhoneNumber 새 전화번호
     * @return 성공 여부
     */
    public boolean updateMemberPhoneNumber(int memberId, String newPhoneNumber) {
        String sql = "UPDATE members SET phone_number = ? WHERE member_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPhoneNumber);
            pstmt.setInt(2, memberId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("회원 ID " + memberId + " 의 전화번호가 " + newPhoneNumber + " (으)로 업데이트되었습니다.");
                return true;
            } else {
                System.out.println("회원 ID " + memberId + " 를 찾을 수 없거나 전화번호 변경이 필요 없습니다.");
            }
        } catch (SQLException e) {
            System.err.println("회원 전화번호 수정 중 오류 발생: " + e.getMessage());
        }
        return false;
    }

    /**
     * 특정 회원을 데이터베이스에서 삭제합니다.
     * @param memberId 삭제할 회원 ID
     * @return 성공 여부
     */
    public boolean deleteMember(int memberId) {
        // 먼저 해당 회원의 대출 기록이 있는지 확인
        if (hasActiveLoansByMember(memberId)) {
            System.out.println("회원 ID " + memberId + " 는 현재 대출 중인 도서가 있어 탈퇴할 수 없습니다.");
            return false;
        }

        String sql = "DELETE FROM members WHERE member_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, memberId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("회원 ID " + memberId + " 이(가) 삭제되었습니다.");
                return true;
            } else {
                System.out.println("회원 ID " + memberId + " 를 찾을 수 없습니다.");
            }
        } catch (SQLException e) {
            System.err.println("회원 삭제 중 오류 발생: " + e.getMessage());
        }
        return false;
    }


    // --- 대출/반납 관리 기능 ---

    /**
     * 도서를 대출합니다.
     * @param bookId 대출할 도서 ID
     * @param memberId 대출하는 회원 ID
     * @return 성공 여부
     */
    public boolean borrowBook(int bookId, int memberId) {
        // 1. 도서 재고 확인 및 감소
        if (!checkAndDecreaseBookStock(bookId)) {
            return false;
        }

        // 2. 대출 예정일 계산 (휴일 고려)
        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = calculateDueDate(borrowDate, 7); // 기본 7일 대출 기간

        // 3. 대출 기록 추가
        String sql = "INSERT INTO loans (book_id, member_id, borrow_date, due_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            pstmt.setInt(2, memberId);
            pstmt.setDate(3, Date.valueOf(borrowDate));
            pstmt.setDate(4, Date.valueOf(dueDate));
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("도서 ID " + bookId + " (이)가 회원 ID " + memberId + " 에게 대출되었습니다. 반납 예정일: " + dueDate);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("도서 대출 중 오류 발생: " + e.getMessage());
            // 대출 실패 시 재고를 다시 증가시키는 롤백 로직 필요
            increaseBookStock(bookId);
        }
        return false;
    }

    /**
     * 도서를 반납합니다.
     * @param loanId 반납할 대출 기록 ID
     * @return 성공 여부
     */
    public boolean returnBook(int loanId) {
        // 1. 대출 기록 조회하여 book_id 가져오기
        int bookId = -1;
        String selectSql = "SELECT book_id FROM loans WHERE loan_id = ? AND return_date IS NULL";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
            pstmt.setInt(1, loanId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    bookId = rs.getInt("book_id");
                } else {
                    System.out.println("유효한 대출 기록 ID " + loanId + " 를 찾을 수 없거나 이미 반납된 도서입니다.");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("대출 기록 조회 중 오류 발생: " + e.getMessage());
            return false;
        }

        // 2. 재고 증가
        if (bookId != -1 && !increaseBookStock(bookId)) {
            return false; // 재고 증가 실패
        }

        // 3. 대출 기록에 반납 날짜 업데이트
        String updateSql = "UPDATE loans SET return_date = ? WHERE loan_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
            pstmt.setDate(1, Date.valueOf(LocalDate.now()));
            pstmt.setInt(2, loanId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("대출 기록 ID " + loanId + " 에 해당하는 도서가 반납되었습니다.");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("도서 반납 중 오류 발생: " + e.getMessage());
        }
        return false;
    }

    /**
     * 대출 도서의 반납일을 연장합니다. (최대 1회 연장 가능으로 가정)
     * @param loanId 연장할 대출 기록 ID
     * @param daysToExtend 연장할 일수 (예: 7일)
     * @return 성공 여부
     */
    public boolean extendDueDate(int loanId, int daysToExtend) {
        String selectSql = "SELECT due_date, extended_count FROM loans WHERE loan_id = ? AND return_date IS NULL";
        String updateSql = "UPDATE loans SET due_date = ?, extended_count = ? WHERE loan_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement selectPstmt = conn.prepareStatement(selectSql);
             PreparedStatement updatePstmt = conn.prepareStatement(updateSql)) {

            selectPstmt.setInt(1, loanId);
            try (ResultSet rs = selectPstmt.executeQuery()) {
                if (rs.next()) {
                    LocalDate currentDueDate = rs.getDate("due_date").toLocalDate();
                    int extendedCount = rs.getInt("extended_count");

                    if (extendedCount >= 1) { // 이미 연장한 경우 (정책에 따라 변경 가능)
                        System.out.println("대출 기록 ID " + loanId + " 는 이미 1회 연장되어 추가 연장이 불가능합니다.");
                        return false;
                    }

                    LocalDate newDueDate = calculateDueDate(currentDueDate, daysToExtend);

                    updatePstmt.setDate(1, Date.valueOf(newDueDate));
                    updatePstmt.setInt(2, extendedCount + 1);
                    updatePstmt.setInt(3, loanId);

                    int affectedRows = updatePstmt.executeUpdate();
                    if (affectedRows > 0) {
                        System.out.println("대출 기록 ID " + loanId + " 의 반납 예정일이 " + newDueDate + " (으)로 연장되었습니다.");
                        return true;
                    }
                } else {
                    System.out.println("유효한 대출 기록 ID " + loanId + " 를 찾을 수 없거나 이미 반납된 도서입니다.");
                }
            }
        } catch (SQLException e) {
            System.err.println("반납일 연장 중 오류 발생: " + e.getMessage());
        }
        return false;
    }


    // --- 헬퍼 메서드 ---

    // 도서 재고 확인 및 감소
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

    // 대출 중인 도서가 있는지 확인
    private boolean hasActiveLoans(int bookId) {
        String sql = "SELECT COUNT(*) FROM loans WHERE book_id = ? AND return_date IS NULL";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("도서 대출 기록 확인 중 오류 발생: " + e.getMessage());
        }
        return false;
    }

    // 회원이 대출 중인 도서가 있는지 확인
    private boolean hasActiveLoansByMember(int memberId) {
        String sql = "SELECT COUNT(*) FROM loans WHERE member_id = ? AND return_date IS NULL";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, memberId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("회원 대출 기록 확인 중 오류 발생: " + e.getMessage());
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
