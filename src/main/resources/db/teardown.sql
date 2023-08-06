// 개요 :: TEST 시 한번에 수행할 때마다 ROLLBACK 으로 데이터를 초기화 해주고 BE 에서 더미데이터를 넣는 과정을 반복한다
// 이 때 Rollback 을 통해 Data 초기화는 진행되지만, PK 값은 auto-increment 로 초기화되지 않고 계속적으로 증가하는 모습을 볼 수 있음
// 예상하지 못한 PK (보통 PK 값도 제어하기 때문에 .. 그럴 일은.. 없어보이긴 하지만) 의 발생을 막기 위해, @Transactional 말고 이렇게 SQL 을 Each Test 마다 실행되게끔 하면
// 더 깔끔한 test 를 수행할 수 있다
SET REFERENTIAL_INTEGRITY FALSE; // 연관관계 모두 해지
truncate table transaction_tb; // CREATE DB 도 매번 또 해야하는걸 방지하기 위해 데이터만 삭제하는 TRUNCATE 사용
truncate table account_tb;
truncate table user_tb;
SET REFERENTIAL_INTEGRITY TRUE; // 다시 등록