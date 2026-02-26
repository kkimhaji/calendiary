<div align="center">

# 🗂️ Calendiary

**팀 기반 협업 게시판 시스템**

팀별 카테고리 구조와 세분화된 권한 시스템을 갖춘 풀스택 협업 플랫폼입니다.  
Rich Text 에디터 기반의 게시글/일기 작성, 이미지 업로드, 댓글 기능을 지원합니다.

<br/>

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![MySQL](https://img.shields.io/badge/MySQL-00000F?style=for-the-badge&logo=mysql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2CA5E0?style=for-the-badge&logo=docker&logoColor=white)

</div>

---

## 📌 프로젝트 소개

Calendiary는 팀 단위로 게시글을 카테고리별로 관리하고, 세분화된 권한 시스템을 통해 팀원 간 협업을 지원하는 웹 애플리케이션입니다.

단순한 CRUD를 넘어 **팀 권한 + 카테고리 권한의 이중 권한 구조**, **CKEditor 5 기반 Rich Text 편집**, **Docker 환경에서의 이미지 URL 처리** 등 실무에 가까운 기술적 고민을 담았습니다.

---

## ✨ 주요 기능

- 🔐 **권한 시스템** — 팀 역할(ADMIN/MEMBER) + 카테고리별 읽기/쓰기 권한 세분화
- 📝 **Rich Text 에디터** — CKEditor 5 기반 게시글/일기 작성, 이미지 업로드 지원
- 📅 **다이어리 기능** — 달력 뷰 + 목록 뷰 전환, 썸네일 미리보기
- 🖼️ **이미지 처리 파이프라인** — 임시 업로드 → 본문 저장 시 영구 이동 자동화
- 🛡️ **보안** — Spring Security + JWT 인증, OWASP HTML Sanitizer 기반 XSS 방어
- 🐳 **Docker 컨테이너화** — 프론트/백엔드 분리 컨테이너 운영

---

## 🏗️ 시스템 아키텍처

```
[React : 3000]  ←──axios──→  [Spring Boot : 8080]  ←──JPA──→  [MySQL]
     │                               │
  DOMPurify                    HtmlSanitizer
  CKEditor 5               ImageService (임시→영구)
  imageUtils.js              Spring Security + JWT
```

---

## 🛠️ 기술 스택

### Backend

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=spring-security&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-00000F?style=for-the-badge&logo=mysql&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-323330?style=for-the-badge&logo=json-web-tokens&logoColor=pink)
![Hibernate](https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=Hibernate&logoColor=white)

### Frontend

![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=JavaScript&logoColor=white)
![Axios](https://img.shields.io/badge/Axios-5A29E4?style=for-the-badge&logo=axios&logoColor=white)
![HTML5](https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white)
![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white)

### DevOps & Tools

![Docker](https://img.shields.io/badge/Docker-2CA5E0?style=for-the-badge&logo=docker&logoColor=white)
![GitHub](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)

### Test

![JUnit5](https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white)

---


## 🔑 핵심 구현 포인트

### 1. 이중 권한 시스템
팀 레벨 권한(ADMIN/MEMBER)과 카테고리 레벨 권한(읽기/쓰기)을 독립적으로 관리하여, 카테고리마다 세밀한 접근 제어가 가능합니다.

### 2. 이미지 처리 파이프라인
```
[업로드] → /post-temp-images/ (임시)
    ↓ 게시글 저장 시 ImageService.processContentImages()
[이동]  → /post-images/ (영구)
    ↓ DB 저장
[조회]  → 상대경로 → imageUtils.convertRelativeToAbsoluteUrls() → 절대 URL
```
DB에는 상대 경로로 저장하고, 프론트에서 조회 시 환경변수 기반으로 절대 URL로 변환합니다.

### 3. XSS 방어 이중 구조
- **Frontend**: DOMPurify로 렌더링 전 HTML 정제
- **Backend**: OWASP Java HTML Sanitizer로 저장 전 서버 측 정제

---

## 📊 엔티티 관계

나중에 추가

---
## 📸 스크린샷

| 게시글 목록 | 게시글 작성 (에디터) | 다이어리 달력 뷰 |
|------------|-------------------|----------------|
| ![list](screenshots/list.png) | ![editor](screenshots/editor.png) | ![diary](screenshots/diary.png) |

- 추가 예정
---

## 🧪 테스트

```bash
# 백엔드 테스트 실행 (H2 인메모리 DB 사용)
./mvnw test
```

운영 환경(MySQL)과 동일한 JPA 로직을 H2로 테스트하여 환경 의존성을 최소화했습니다.

---

## 📜 License

MIT License
