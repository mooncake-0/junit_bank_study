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


###