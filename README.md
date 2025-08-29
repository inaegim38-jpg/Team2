# Team2

도서관리 시스템 (Library Management System)
개요

이 프로젝트는 Java 기반의 간단한 도서관리 시스템입니다.

도서 목록 관리

휴일 지정 기능

MySQL 데이터베이스와 연동된 정보 처리

주요 기능은 **Library.java**를 중심으로 구성되어 있으며, 데이터베이스와 직접 소통하며 도서 데이터를 조작합니다.
휴일 지정과 같은 정책은 별도의 클래스에서 정의하고, Library에 전달 및 적용하는 방식으로 구현됩니다.

프로젝트 구조
src/
│
├── Library.java          // DB와 직접 소통 (도서 등록, 삭제, 조회, 휴일 확인 등)
├── HolidayPolicy.java    // 인터페이스 or 추상 클래스 (휴일 지정용)
├── WeekendHoliday.java   // 예: 주말을 휴일로 지정하는 구현 클래스
├── CustomHoliday.java    // 사용자 정의 휴일 설정 클래스
├── Main.java             // 프로그램 시작점 (테스트 및 CLI 실행)
└── db.properties         // DB 연결 정보

