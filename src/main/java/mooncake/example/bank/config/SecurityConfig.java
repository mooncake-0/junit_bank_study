package mooncake.example.bank.config;

import mooncake.example.bank.config.security.JwtAuthenticationFilter;
import mooncake.example.bank.config.security.JwtAuthorizationFilter;
import mooncake.example.bank.domain.user.UserEnum;
import mooncake.example.bank.utils.CustomResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/*
 Security 단은 그냥 상기해보면서
 저런 설정들을 사용했었지, 이런 것도 있구나 하면 됨
 매번 새롭기 때문..
 */
@Configuration
public class SecurityConfig {

    private final Logger log = LoggerFactory.getLogger(getClass());

    public class CustomSecurityFilterManager extends AbstractHttpConfigurer<CustomSecurityFilterManager, HttpSecurity> {
        @Override
        public void configure(HttpSecurity builder) throws Exception {
            AuthenticationManager authenticationManager = builder.getSharedObject(AuthenticationManager.class);
            builder.addFilter(new JwtAuthenticationFilter(authenticationManager));
            builder.addFilter(new JwtAuthorizationFilter(authenticationManager));
            super.configure(builder);
        }
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        log.debug("디버그 : PasswordEncoder 빈 등록 완료");
        return new BCryptPasswordEncoder();
    }

    // TODO :: JWT 필터들 등록 필요

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        log.debug("디버그 : FilterChain 빈 등록 완료");

        http.headers().frameOptions().disable(); // iframe 허용 안한다
        http.csrf().disable(); // POSTMAN 을 위함 // CSRF 뭔지 알아보기
        http.cors().configurationSource(configurationSource());


        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS); // JSESSION 을 사용하지 않겠다
        http.formLogin().disable(); // CSR 방식임
        http.httpBasic().disable(); // 브라우저 팝업 인증 금지

        http.apply(new CustomSecurityFilterManager());


        // MEMO :: Security 에서 걸리는 건지, 아니면 Spring 에서 잡아내는건지에 따라 에러 처리가 매우 다름
        //         이 부분은 Security 에서 걸려지는 부분들인 것
        //         1. 인증되지 않은채로 /s 경로로 들어가려 한다 > CustomAuthenticationException 발생시킴
        //         2. 인가되지 않은채로 인가경로로 들어가려 한다 > CustomForbiddenException 발생시킴
        //         @RestControllerAdvice 는 Spring 에서 에러 발생시 처리를 도와주는 공간이다.
        //         따라서 Forbidden 에 대해서는 저기에 등록할 필요가 없음 > 왜냐하면 Security 에서 Response 제어해줄거니까

        // Exception 가로채기
        // AuthenticationEntryPoint 가 들어와야한다
        // - AEP 는 ExceptionTranslationFilter 가 사용하게 된다
        // - 우리가 AEP 를 가로챈다
        // - 이유: Web 에서, API Tester 에서, Junit Code Test 에서 각각 ExceptionFilter 가 응답을 보내는 방식이 다르다 (Config Test 1 에서 확인함)
        // - Web 은 View 를 보내주고, API Tester 는 JSON 을 보내주고, Junit Code 는 status 만 보내주고 body 가 없음 (사전 확인함)
        // - 쉬운 Testing 을 위해 제어 (이렇게 하면 일괄 데이터로 전송된다)
        // - 이걸 위해 Response Generic Class 를 만들었음
        // - 그리고 에러 발생마다 제어해주기 위한 CustomResponseUtil 클래스를 만들었음
        http.exceptionHandling().authenticationEntryPoint((request, response, authException) -> {
            CustomResponseUtils.errResponse(response, "로그인을 진행해주세요", HttpStatus.UNAUTHORIZED);
        }); // 람다로 직접 생성한다

        // 이건 인가에 대한 예외처리
        http.exceptionHandling().accessDeniedHandler(((request, response, accessDeniedException) -> {
            CustomResponseUtils.errResponse(response, "권한이 없는 경로로 진입하려 합니다", HttpStatus.FORBIDDEN);
        }));

        http.authorizeRequests()
                .antMatchers("/api/s/**").authenticated()  // /api/s/~ 경로로 들어오는건 로그인이 된 상태여야 한다
                .antMatchers("/api/admin/**").hasRole("" + UserEnum.ADMIN) // /api/admin/~ 은 로그인도 되어있고, ADMIN 인가 확인되어야 한다
                .anyRequest().permitAll();

        return http.build();
    }


    public CorsConfigurationSource configurationSource() {

        log.debug("디버그 : configurationSource cors 설정이 SecurityFilterChain 에 등록됨!");
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedHeader("*");
        config.addAllowedMethod("*"); // 모든 통신 방식 허용 (JS 요청, CRUD)
        config.addAllowedOriginPattern("*"); // 모든 IP 주소 허용 (원랜 FE 프록시 서버 IP 만 허용한다든지 하면 좋음)
        config.setAllowCredentials(true); // 클라이언트에서 쿠기 요청 허용

        // 모든 주소, 어떤 주소의 요청으로 들어와도, 위 config 대로 세팅이 걸린다는 뜻
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

}

