package mooncake.example.bank.config.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import mooncake.example.bank.config.security.auth.LoginUser;
import mooncake.example.bank.config.security.jwt.JwtParams;
import mooncake.example.bank.config.security.jwt.JwtProcessor;
import mooncake.example.bank.dto.request.UserReqDto;
import mooncake.example.bank.utils.CustomResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static mooncake.example.bank.dto.request.UserReqDto.*;
import static mooncake.example.bank.dto.response.UserRespDto.*;

// Extend 하는 녀석 용도가 Login Filter 임 > 기본 Path /login 으로 잡는다
// MEMO : 이건 Login 전담하는 Filter 라 JwtLoginFilter 란 이름이 더 적합해보인다
//      : JwtAuthenticationFilter -> 토큰의 여부를 파악하고 verify 를 하고 Authentication 객체를 생성하거나, exception 을 날리는 곳
//      : JwtAuthorizationFilter -> 들어온 Authentication 을 통해서 Role 의 적합성 여부를 판단하고 넘기거나, exception 을 날리는 곳
//      : 이게 내가 생각하는 정확한 필터들의 이름이나, 그냥.. 대충하자 수업이니까.
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final AuthenticationManager authenticationManager;
    private ObjectMapper om = new ObjectMapper();

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {

        super(authenticationManager);
        setFilterProcessesUrl("/api/login");
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        log.debug("CHECK : {}", "attemptAuthentication 진입");

        // RequestDto 가 같이 왔을 것입니다. 없으면 바로 Exception
        try {

            LoginReqDto requestDto = om.readValue(request.getInputStream(), LoginReqDto.class);

            // 로그인 시도 (AuthenticationProvider 가 받아가서 인증을 거칠 수 있는 토큰을 줘야함)
            // 내부 인증용 토큰 (객체) 를 만들어준다
            // JWT 를 쓴다고 하더라도, controller 진입시 시큐리티의 권한 체크, 인증 체크의 도움을 받을 수 있게 세션을 만든다
            // 또한 일시적으로 SecurityContext 에 저장을 해야, 뒷단에서 요청자를 받아올 수가 있음
            // 이 세션의 유효기간은 request 하고 response 하면 바로 끝난다
            // 왜냐하면 JSESSIONID 를 만들어서 서버에 저장해두지 않기 때문이다!! (이걸 가지고 온들 뭐 안해줌!!)
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(requestDto.getUsername(), requestDto.getPassword());
            Authentication authenticated = authenticationManager.authenticate(authToken);

            return authenticated;

        } catch (Exception e) {
            // 에러 발생했으니 unsuccessful 이 시스템 상 자동으로 타진다 // 이쪽으로 타서, LoginFailure 쪽 Listener 가 반응한다
            throw new InternalAuthenticationServiceException("인증 중 에러: " + e.getMessage());
        }
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {

        // 이증 실패기 때문에 원래 있던 401 을 날려준다
        // 이곳은 특별히 "로그인 시도를 실패했을 경우" 를 알려주기 위해 제어한다
        // 이거 안하면 당연히 그냥 Security 가 해주는 에러 처리로 나간다
        CustomResponseUtils.errResponse(response, "로그인 실패", HttpStatus.UNAUTHORIZED);

    }

    // ** return authentication 이 정상적으로 동작하였을 때 수행된다 **
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain, Authentication authResult) throws IOException, ServletException, IOException {

        log.debug("CHECK {}", "successfulAuthentication 호출됨: 로그인 성공");

        LoginUser loginUser = (LoginUser) authResult.getPrincipal();
        String jwtToken = JwtProcessor.create(loginUser);
        response.addHeader(JwtParams.HEADER, jwtToken);

        LoginRespDto loginRespDto = new LoginRespDto(loginUser.getUser());
        String responseBody = om.writeValueAsString(loginRespDto);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(responseBody);

    }
}
