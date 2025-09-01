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
├── Library.java          // 프로그램 시작점 & DB와 직접 소통 (도서 등록, 삭제, 조회 등)
├── Holiday.java    // 인터페이스 (휴일을 확인하는 isHoliday 메소드 보유)
├── Weekend.java   // 인터페이스에서 상속받는 isHoliday를 구현(주말만 확인하도록)
└── Anniversary.java   // 인터페이스에서 상속받는 isHoliday를 구현(속성으로 공휴일을 저장하는 리스트를 가지고, 공휴일 추가 기능)

