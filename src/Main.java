import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Properties;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // db.properties 파일 로드
        Properties dbProps = new Properties();
        try (InputStream input = Main.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                System.err.println("db.properties 파일을 찾을 수 없습니다. 경로를 확인해주세요.");
                return;
            }
            dbProps.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        // 휴일 정책 설정 (예: 주말 휴일 + 사용자 정의 휴일)
        WeekendHoliday weekendPolicy = new WeekendHoliday();
        CustomHoliday customHolidayPolicy = new CustomHoliday(dbProps); // DB 연동 CustomHoliday

        // HolidayPolicy를 조합하여 사용하거나, 필요에 따라 하나만 사용할 수 있습니다.
        // 여기서는 CustomHoliday만 사용하겠습니다.
        // Library library = new Library(weekendPolicy); // 주말만 휴일로 지정할 경우
        Library library = new Library(customHolidayPolicy); // 사용자 정의 휴일 + 주말 (CustomHoliday 내부적으로 처리 가능)

        Scanner scanner = new Scanner(System.in);

        System.out.println("도서 관리 시스템에 오신 것을 환영합니다!");

        while (true) {
            System.out.println("\n--- 메뉴 ---");
            System.out.println("1. 도서 관리");
            System.out.println("2. 회원 관리");
            System.out.println("3. 대출/반납 관리");
            System.out.println("4. 휴일 관리");
            System.out.println("0. 종료");
            System.out.print("메뉴를 선택하세요: ");

            int menuChoice = scanner.nextInt();
            scanner.nextLine(); // 버퍼 비우기

            switch (menuChoice) {
                case 1: // 도서 관리
                    handleBookManagement(library, scanner);
                    break;
                case 2: // 회원 관리
                    handleMemberManagement(library, scanner);
                    break;
                case 3: // 대출/반납 관리
                    handleLoanManagement(library, scanner);
                    break;
                case 4: // 휴일 관리
                    handleHolidayManagement(customHolidayPolicy, scanner);
                    break;
                case 0:
                    System.out.println("프로그램을 종료합니다.");
                    scanner.close();
                    return;
                default:
                    System.out.println("잘못된 메뉴 선택입니다. 다시 시도해주세요.");
            }
        }
    }

    private static void handleBookManagement(Library library, Scanner scanner) {
        while (true) {
            System.out.println("\n--- 도서 관리 ---");
            System.out.println("1. 도서 추가");
            System.out.println("2. 모든 도서 보기");
            System.out.println("3. 도서 제목으로 검색");
            System.out.println("4. 도서 재고 수정");
            System.out.println("5. 도서 삭제");
            System.out.println("0. 뒤로가기");
            System.out.print("메뉴를 선택하세요: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("제목: "); String title = scanner.nextLine();
                    System.out.print("저자: "); String author = scanner.nextLine();
                    System.out.print("ISBN: "); String isbn = scanner.nextLine();
                    System.out.print("출판사: "); String publisher = scanner.nextLine();
                    System.out.print("재고: "); int stock = scanner.nextInt();
                    scanner.nextLine();
                    library.addBook(title, author, isbn, publisher, stock);
                    break;
                case 2:
                    library.viewAllBooks();
                    break;
                case 3:
                    System.out.print("검색할 도서 제목 키워드: "); String keyword = scanner.nextLine();
                    library.searchBooksByTitle(keyword);
                    break;
                case 4:
                    System.out.print("수정할 도서 ID: "); int bookId = scanner.nextInt();
                    System.out.print("새 재고 수량: "); int newStock = scanner.nextInt();
                    scanner.nextLine();
                    library.updateBookStock(bookId, newStock);
                    break;
                case 5:
                    System.out.print("삭제할 도서 ID: "); int deleteBookId = scanner.nextInt();
                    scanner.nextLine();
                    library.deleteBook(deleteBookId);
                    break;
                case 0:
                    return;
                default:
                    System.out.println("잘못된 선택입니다.");
            }
        }
    }

    private static void handleMemberManagement(Library library, Scanner scanner) {
        while (true) {
            System.out.println("\n--- 회원 관리 ---");
            System.out.println("1. 회원 등록");
            System.out.println("2. 모든 회원 보기");
            System.out.println("3. 회원 전화번호 수정");
            System.out.println("4. 회원 삭제");
            System.out.println("0. 뒤로가기");
            System.out.print("메뉴를 선택하세요: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("이름: "); String name = scanner.nextLine();
                    System.out.print("전화번호: "); String phone = scanner.nextLine();
                    library.addMember(name, phone);
                    break;
                case 2:
                    library.viewAllMembers();
                    break;
                case 3:
                    System.out.print("수정할 회원 ID: "); int memberId = scanner.nextInt();
                    System.out.print("새 전화번호: "); String newPhone = scanner.nextLine(); // Consume newline
                    newPhone = scanner.nextLine(); // Read actual phone number
                    library.updateMemberPhoneNumber(memberId, newPhone);
                    break;
                case 4:
                    System.out.print("삭제할 회원 ID: "); int deleteMemberId = scanner.nextInt();
                    scanner.nextLine();
                    library.deleteMember(deleteMemberId);
                    break;
                case 0:
                    return;
                default:
                    System.out.println("잘못된 선택입니다.");
            }
        }
    }

    private static void handleLoanManagement(Library library, Scanner scanner) {
        while (true) {
            System.out.println("\n--- 대출/반납 관리 ---");
            System.out.println("1. 도서 대출");
            System.out.println("2. 도서 반납");
            System.out.println("3. 반납일 연장");
            // System.out.println("4. 현재 대출 목록 조회"); // 필요시 구현
            System.out.println("0. 뒤로가기");
            System.out.print("메뉴를 선택하세요: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("대출할 도서 ID: "); int bookId = scanner.nextInt();
                    System.out.print("대출하는 회원 ID: "); int memberId = scanner.nextInt();
                    scanner.nextLine();
                    library.borrowBook(bookId, memberId);
                    break;
                case 2:
                    System.out.print("반납할 대출 기록 ID: "); int loanId = scanner.nextInt();
                    scanner.nextLine();
                    library.returnBook(loanId);
                    break;
                case 3:
                    System.out.print("연장할 대출 기록 ID: "); int extendLoanId = scanner.nextInt();
                    System.out.print("연장할 일수 (예: 7): "); int days = scanner.nextInt();
                    scanner.nextLine();
                    library.extendDueDate(extendLoanId, days);
                    break;
                case 0:
                    return;
                default:
                    System.out.println("잘못된 선택입니다.");
            }
        }
    }

    private static void handleHolidayManagement(CustomHoliday customHolidayPolicy, Scanner scanner) {
        while (true) {
            System.out.println("\n--- 휴일 관리 ---");
            System.out.println("1. 휴일 추가");
            System.out.println("2. 휴일 삭제");
            System.out.println("3. 특정 날짜가 휴일인지 확인");
            System.out.println("0. 뒤로가기");
            System.out.print("메뉴를 선택하세요: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("추가할 휴일 날짜 (YYYY-MM-DD): "); String dateStr = scanner.nextLine();
                    System.out.print("휴일 설명: "); String description = scanner.nextLine();
                    customHolidayPolicy.addHoliday(LocalDate.parse(dateStr), description);
                    break;
                case 2:
                    System.out.print("삭제할 휴일 날짜 (YYYY-MM-DD): "); String deleteDateStr = scanner.nextLine();
                    customHolidayPolicy.removeHoliday(LocalDate.parse(deleteDateStr));
                    break;
                case 3:
                    System.out.print("확인할 날짜 (YYYY-MM-DD): "); String checkDateStr = scanner.nextLine();
                    LocalDate checkDate = LocalDate.parse(checkDateStr);
                    if (customHolidayPolicy.isHoliday(checkDate)) {
                        System.out.println(checkDate + " 은(는) 휴일입니다.");
                    } else {
                        System.out.println(checkDate + " 은(는) 휴일이 아닙니다.");
                    }
                    break;
                case 0:
                    return;
                default:
                    System.out.println("잘못된 선택입니다.");
            }
        }
    }
}

