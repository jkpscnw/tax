# jpsoft_1

금융보안원

ID : O2310251 (앞에 O는 대문자 영문)
PW : 금보원1! (영타)
-> (DLP접속시 변경함) jkpark1!
-> (윈도우접속시 변경함) jkpark12!@ -> 금보원1!
기본비번 : 박정관1! or 금보원1! 

GitLab
username : jkpark
email : jkpark@jpsoft.co.kr
pw : 금보원1!

Yuna
jkpark / jkpark12!@

VPN URL
운영 sslvpn-vpc-105.fin-ncloud.com
개발 sslvpn-vpc-131.fin-ncloud.com
fsec_3007 / fsi3495!@

DBSafer
ID : O2310251
PW : 박정관1!

DBeaver
Server Host : 10.50.30.132
ID : azureadmin
PW: 1q2w3e4r!@#$
Drive Settings 클릭 > Libraries 클릭 > 기존파일삭제 > Add file - mariadb -java-client-2.7.1.jar

Hiware
ID : O2310251
PW : 박정관1!
SSH / SFTP 접속
data - 데이터허브
findx - 데이터거래소
DBeaver 계정동일


Office
setup.exe 실행 (관리자권한실행 x)
주문번호 입력 : MSO35190522001
인증 : 설치후 정품인증의 경우 KMS_OFFICE2021_PRO.bat 우클릭 후 관리자권한으로 실행

알집설치정보
제품번호 : 4KN2-WELE-87C3-QNLC

git

bash 실행

cd C/Users/박정관/dev/fsec-user
git checkout feature/[기능명]  <-  기존 branch 가 있는 경우
또는 
git checkout -b feature/[기능명] <- 새로운 branch 를 생성하는 경우
git status
git add .
git commit -m "[추가] 추가기능  [수정] 기존기능수정  [삭제] 기존기능삭제" (줄바꿈은 \n 적용)
git pull origin staging
git push origin feature/[기능명]
gitlab 사이트에서 fsec-user repository 로 이동
LNB에서 Merge Request 클릭
new merge request 클릭
Souce branch 와 Target branch 확인
- source branch : 본인이 push 한 branch 선택
- target branch : Merge Request를 보낼 branch 선택 (staging)
Compare branches and comtinue 클릭
- title : 본인이 마지막으로 커밋한 메세지 입력
- description : 필요한 경우 입력
- Assignee : 미선택 또는 Administrator
- Merge Optiion  : Delete source branch when merge request is accepted 체크
- create merge request 클릭
이후 관리자(갬태영부장님) 확인 후 Accept or Reject 처리





20231102

결제하기 화면 > 상품정보 > 결제마감 데이터
- DB 설계 예정 -> 설계 완료 후 전달 줄 예정

20231103

결제완료 화면
- 화면설계 없음
- 상품구매 화면으로 적용 예정
- 현재 미개발 상태로 임의 페이지 처리 필요 -> 개발완료 후 returen url 변경 적용 필요
- return 페이지로 넘길 parameter 없음 (sesstion 값으로 처리함)


