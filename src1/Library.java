import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Date;
import java.util.Properties;

public class Library {
    public static void main(String[] args) {
        Properties props = new Properties();

        try {
            // 1. 환경파일(db.properties) 로드
            FileInputStream fis = new FileInputStream("src1/db.properties");
            props.load(fis);

            String url = props.getProperty("db.url");
            String user = props.getProperty("db.user");
            String password = props.getProperty("db.password");

            // 2. DB 연결
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("MySQL 연결 성공!");

            // 3. 쿼리 실행
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM books");
            //test

            // 결과 처리
            while (rs.next()) {
                int id = rs.getInt("bookID");          // 컬럼명으로 접근
                String name = rs.getString("bookName");
                String cnt = rs.getString("bookcnt");

                System.out.println(id + " | " + name + " | " + cnt);
            }

            rs.close();
            stmt.close();
            conn.close();
            System.out.println("MySQL 연결 종료.");

        } catch (IOException e) {
            System.err.println("환경파일 로드 실패: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("MySQL 연결 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
