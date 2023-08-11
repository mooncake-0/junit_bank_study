## JUNIT BANK APP

### JPA LocalDateTime 자동 생성 방법

1. @EnableJpaAuditing (Main 클래스)
2. @EntityListeners(AuditingEntityListener.class) (Entity 클래스)


    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;


### 실행 후 run 한 Context 를 가져와서 Bean 들을 확인할 수 있다
    /*
    RUN 한 Context 를 가져와 BEAN 들을 보는 법
    */
    ConfigurableApplicationContext context = SpringApplication.run(BankApplication.class, args);
    String[] iocNames = context.getBeanDefinitionNames();
    for (String iocName : iocNames) {
    // context.getBean(iocName);
        System.out.println(iocName);
    }

<br>

---
<br>

## Service 단 테스트에 대한 고찰



* 항상 생각해야 하는거 : 지금 이 TEST를 짜는 이유가 뭘까? ex : 내가 계좌 입금을 하는 동작이 잘 동작하는지?
* DB 같은건 어차피 DB 있어야 하니까 관심 대상이 아니다 -> stubbing 하는거임
* save 되든, 조회가 되든, 이런거 관심없음 
* 오직 관심있는건 비즈니스 로직의 구현 : **계좌 이체가 잘 되는가**, **Domain 단에서 잘 더해**졌는가, **DTO 가 문제없이 생성**되는가 등


- 일반적인 순서를 생각해보자 
1) given data 가 뭐일지 확인 (보통 PARAMS)
2) Controller 단에서 Validation 다 되어 있어야 하지만, **Service 에서 해야할 필요가 있을시** Validation 진행
3) 계속 돌려보면서 발생하는 에러 확인 -> 일반적으로 Repository 단에서 하는 일들
4) Repository 에는 관심 없음 -> Stubbing 진행
5) 가끔 헷갈리는건, Repository 에서 반환한 객체의 역할이다. 아니면 반환을 안하거나. 
6) Repository 가 반환하는지, 실패하는지는 알바가 아니다 > 나는 우선 이 로직을 통과했음을 가정해야한다
7) Stubbing 으로 통과시켜주거나, doNothing 을 하거나, verify 를 통해 호출 여부를 확인한다
8) 필요한 stub 마다 따로따로 나눠주는게 좋음
9) 일일이 DTO 필드 다 할 필요 없이, 그냥 내가 원하는 값만 확인해도 됨 솔직히 ㅇㅇ
10) 그리고 null 값이 아닌지? System.out.println 해서 해도 됨

- 근데 또 생각해봐야 할 것 -> DTO Test 는 Controller 단에서도 충분히 할 수 있음
- 굳이 Service 에서도 해야할까? - 맞음, 안해도 됨
- Test >> 내가 뭘 Test 하는가? 목적이 뭔가? 필요한 것들이 뭔가? 하나하나 하면서 해야함
- 서비스 테스트를 지금까지 교육한건 기술적인 테크닉임
- 진짜 서비스를 테스트하고 싶으면, 내가 지금 무엇을 여기서 할지 명확히 구분해야함 (그래야 역할분리가 잘됨)
- DTO 만드는 책임은 물론 서비스에 있지만, Controller 에 이 역할을 위임해도 됨. 왜냐면 DTO 자체가 통신용이니까
- DB 관련된 것도 서비스 책임 아님 > 일절 할 필요 없음
- 비즈니스 로직에 DB 관련된 것이 흘러가면, 그냥 stub 하는게 기본



<br>

---
<br>

### 스프링부트 JUNIT TEST - 시큐리티를 활용한 BANK 애플리케이션

- 최주호 강사님 - https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81%EB%B6%80%ED%8A%B8-junit-%ED%85%8C%EC%8A%A4%ED%8A%B8
