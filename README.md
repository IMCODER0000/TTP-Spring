
<div align="center">

![header](https://capsule-render.vercel.app/api?type=waving&color=6B66FF&height=250&section=header&text=TTP%20(Time%20To%20Play)&fontSize=70&animation=fadeIn&fontAlignY=35&desc=Play%20Together,%20Anytime,%20Anywhere&descAlignY=51&descAlign=50)

### 🎮 함께 즐기는 실시간 멀티플레이 게임 플랫폼

<img width="1490" alt="스크린샷 2025-02-11 오후 2 41 51" src="https://github.com/user-attachments/assets/c8948e8d-2e17-475f-9075-9d04c5ac72b7" />
<img width="1477" alt="liar" src="https://github.com/user-attachments/assets/69f2f84d-4074-40a0-b8b9-04190a35ca9f" />


<img width="1486" alt="스크린샷 2025-02-11 오전 11 15 09" src="https://github.com/user-attachments/assets/c9bd1b0d-6419-4c98-9ff8-c182ab006ff0" />
<img width="1486" alt="스크린샷 2025-02-11 오전 11 22 16" src="https://github.com/user-attachments/assets/f9008809-ee08-4b92-a831-0dcbcf37a826" />
<img width="1480" alt="speed" src="https://github.com/user-attachments/assets/38d0db9f-f0aa-4960-bcc7-00fd6b7a26e4" />

<img width="1443" alt="스크린샷 2025-02-10 오전 7 14 26" src="https://github.com/user-attachments/assets/01b63ac0-bd04-4c47-8633-175926c0ea08" />



[![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-61DAFB?style=for-the-badge&logo=react&logoColor=black)](https://reactjs.org/)
[![Node.js](https://img.shields.io/badge/Node.js-339933?style=for-the-badge&logo=nodedotjs&logoColor=white)](https://nodejs.org/)
[![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)](https://redis.io/)
[![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![AWS](https://img.shields.io/badge/AWS-232F3E?style=for-the-badge&logo=amazonaws&logoColor=white)](https://aws.amazon.com/)
[![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)

</div>

## 📖 프로젝트 소개

TTP(Time To Play)는 웹소켓 기반의 실시간 멀티플레이 게임 플랫폼입니다. PC, 모바일, 태블릿 등 다양한 디바이스에서 친구들과 함께 즐길 수 있는 게임 서비스를 제공합니다.

### 💝 프로젝트의 시작

이 프로젝트는 특별한 의미를 가지고 시작되었습니다. 3년간 병원에 입원 중인 한 어린 친구와의 만남이 계기가 되었습니다. "친구들과 함께 놀고 싶다"는 그 친구의 소망을 듣고, 거리와 상황에 상관없이 누구나 함께 즐길 수 있는 플랫폼을 만들기로 결심했습니다.

### 🎯 주요 기능

- **라이어 게임**: 거짓말쟁이를 찾아내는 추리 게임
- **스피드 퀴즈**: 친구들과 함께 즐기는 빠른 답변 게임
- **심리 테스트**: 재미있는 심리 테스트로 서로를 알아가기
- **실시간 채팅**: 게임 중 실시간 소통 가능

## 🛠 기술 스택

### Backend
- **Spring Boot**: 메인 게임 서버
- **Node.js & Express**: 보조 게임 서버
- **WebSocket & STOMP**: 실시간 양방향 통신
- **JPA**: 데이터 영속성 관리
- **Spring Security**: 보안 관리

### Frontend
- **React**: 웹 클라이언트
- **WebSocket**: 실시간 통신
- **Styled-components**: 스타일링

### Database & Cache
- **MySQL**: 주 데이터베이스
- **Redis**: 세션 및 실시간 데이터 관리
- **Caffeine**: 로컬 캐시

### Infrastructure
- **AWS EC2**: 서버 호스팅
- **AWS RDS**: 데이터베이스 관리
- **CloudFront**: CDN 서비스
- **Docker**: 컨테이너화
- **Nginx**: 웹 서버 및 로드 밸런싱

## 🚀 성능 최적화

### 비동기 처리
- AsyncConfig를 통한 커스텀 스레드 풀 구성
- @Async 어노테이션으로 비동기 처리 구현
- WebSocket을 통한 실시간 게임 상태 동기화

### 동시성 관리
- ConcurrentHashMap으로 게임 상태 관리
- ReentrantLock으로 동시성 제어
- 분산 락으로 다중 서버 환경 동기화

### 캐시 최적화
- Redis를 활용한 세션 관리
- Caffeine 캐시로 로컬 캐싱 구현
- 계층형 캐시 아키텍처 적용

## 📈 성능 지표

- 서버 응답 시간: 평균 50ms 이하
- 동시 접속자: 최대 3000명 수용
- 메모리 사용 효율: 2배 향상
- CPU 사용률: 29% 감소

## 🎯 개발 현황 및 계획

### 현재 진행 상황 (45% 구현)
- [x] 기본 게임 로직 구현
- [x] 실시간 통신 기반 마련
- [x] 기본 인프라 구축
- [x] 동시성 관리 시스템 구축

### 단기 목표 (2주)
- [ ] 핵심 게임 로직 비동기 전환
- [ ] 락 메커니즘 최적화
- [ ] 캐시 시스템 고도화

### 중기 목표 (1개월)
- [ ] WebFlux 도입
- [ ] 트랜잭션 관리 개선
- [ ] 모니터링 시스템 구축

## 🏗 시스템 아키텍처


### 초기 아키텍처 (Current)
기본적인 기능 구현과 서비스 안정화에 초점을 맞춘 아키텍처입니다.

<img width="574" alt="스크린샷 2025-02-17 오후 3 19 04" src="https://github.com/user-attachments/assets/fe7daa36-e9d8-4691-8d29-2ae73f645ff9" />


### 향후 아키텍처 (To-Be, ing)
서비스 확장성과 가용성을 고려한 고도화된 아키텍처입니다.

<img width="793" alt="스크린샷 2025-02-17 오후 3 19 12" src="https://github.com/user-attachments/assets/998c10ca-6b1b-44e1-bed1-173095692251" />


## 🤝 기여 방법

1. 이 저장소를 포크합니다
2. 새로운 브랜치를 생성합니다
3. 변경사항을 커밋합니다
4. 브랜치에 푸시합니다
5. Pull Request를 생성합니다

## 📝 라이센스

이 프로젝트는 MIT 라이센스를 따릅니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

---

<div align="center">

### 💌 연락처

[![Email](https://img.shields.io/badge/Email-gustn9025%40naver.com-blue?style=for-the-badge&logo=gmail&logoColor=white)](mailto:gustn9025@naver.com)
[![GitHub](https://img.shields.io/badge/GitHub-IMCODER0000-black?style=for-the-badge&logo=github&logoColor=white)](https://github.com/IMCODER0000)

</div>

<br/>

## 🎓 Education

### 가천대학교 (2023.03 ~ 2025.02)
- 컴퓨터공학과 학사

</div>
