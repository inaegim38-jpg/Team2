import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
        import java.time.LocalDate;
import java.util.Properties;
import java.util.Scanner;

public class Library {
    public static void main(String[] args) {
        Properties props = new Properties();
        Weekend weekend = new Weekend();
        Anniversary anniversary = new Anniversary();

        // 예시 휴일 추가 (기념일)
        anniversary.addHoliday(LocalDate.of(2025, 9, 1)); // 9월 1일 휴일 예시

        LocalDate today = LocalDate.now();
        boolean isHoliday = weekend.isHoliday(today) || anniversary.isHoliday(today);

        if (isHoliday) {
            System.out.println("오늘은 휴일입니다. 조회만 가능합니다.");
        }

        try {
            // 1. 환경파일(db.properties) 로드
            FileInputStream fis = new FileInputStream("src1/db.properties");
            props.load(fis);

            String url = props.getProperty("db.url");
            String user = props.getProperty("db.username");
            String password = props.getProperty("db.password");

            // 2. DB 연결
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("MySQL 연결 성공!");

            Scanner scanner = new Scanner(System.in);
            boolean running = true;

            while (running) {
                System.out.println("\n===== 도서관리 시스템 =====");
                System.out.println("1. 도서 검색 (SELECT)");

                if (!isHoliday) { // 평일일 때만 수정/삭제/추가 가능
                    System.out.println("2. 도서 추가 (INSERT)");
                    System.out.println("3. 도서 삭제 (DELETE)");
                    System.out.println("4. 도서 수정 (UPDATE)");
                }

                System.out.println("5. 종료");
                System.out.print("선택 >>> ");

                int choice = scanner.nextInt();
                scanner.nextLine(); // 개행 문자 처리

                switch (choice) {
                    case 1: // 검색은 항상 가능
                        System.out.print("검색할 도서명 입력: ");
                        String searchName = scanner.nextLine();
                        selectBook(conn, searchName);
                        break;

                    case 2: // 추가
                    case 3: // 삭제
                    case 4: // 수정
                        if (isHoliday) {
                            System.out.println("오늘은 휴일이므로 수정/삭제/추가가 불가능합니다.");
                        } else {
                            if (choice == 2) {
                                System.out.print("추가할 도서명: ");
                                String insertName = scanner.nextLine();
                                System.out.print("수량 입력: ");
                                int insertCount = scanner.nextInt();
                                scanner.nextLine();
                                insertBook(conn, insertName, insertCount);
                            } else if (choice == 3) {
                                System.out.print("삭제할 도서ID 입력: ");
                                int deleteId = scanner.nextInt();
                                scanner.nextLine();
                                deleteBook(conn, deleteId);
                            } else if (choice == 4) {
                                System.out.print("수정할 도서ID 입력: ");
                                int updateId = scanner.nextInt();
                                scanner.nextLine();
                                System.out.print("새 도서명: ");
                                String updateName = scanner.nextLine();
                                System.out.print("새 수량: ");
                                int updateCount = scanner.nextInt();
                                scanner.nextLine();
                                updateBook(conn, updateId, updateName, updateCount);
                            }
                        }
                        break;

                    case 5:
                        running = false;
                        break;

                    default:
                        System.out.println("잘못된 입력입니다.");
                }
            }

            conn.close();
            scanner.close();
            System.out.println("MySQL 연결 종료.");

        } catch (IOException e) {
            System.err.println("환경파일 로드 실패: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("MySQL 연결 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void selectBook(Connection conn, String name) throws SQLException {
        String sql = "SELECT * FROM books WHERE bookName LIKE ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + name + "%");
            ResultSet rs = pstmt.executeQuery();
            boolean found = false;
            while (rs.next()) {
                found = true;
                int id = rs.getInt("bookID");
                String bookName = rs.getString("bookName");
                int bookCnt = rs.getInt("bookcnt");
                System.out.printf("%d | %s | 수량: %d%n", id, bookName, bookCnt);
            }
            if (!found) {
                System.out.println("검색 결과가 없습니다.");
            }
        }
    }

    private static void insertBook(Connection conn, String name, int count) throws SQLException {
        String sql = "INSERT INTO books (bookName, bookcnt) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, count);
            int rows = pstmt.executeUpdate();
            System.out.println(rows + "건이 추가되었습니다.");
        }
    }

    private static void deleteBook(Connection conn, int id) throws SQLException {
        String sql = "DELETE FROM books WHERE bookID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();
            System.out.println(rows + "건이 삭제되었습니다.");
        }
    }

    private static void updateBook(Connection conn, int id, String name, int count) throws SQLException {
        String sql = "UPDATE books SET bookName = ?, bookcnt = ? WHERE bookID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, count);
            pstmt.setInt(3, id);
            int rows = pstmt.executeUpdate();
            System.out.println(rows + "건이 수정되었습니다.");
        }
    }
}
