package mooncake.example.bank.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import mooncake.example.bank.config.dummy.DummyObjectCreator;
import mooncake.example.bank.config.security.auth.LoginUser;
import mooncake.example.bank.config.security.jwt.JwtParams;
import mooncake.example.bank.config.security.jwt.JwtProcessor;
import mooncake.example.bank.domain.user.User;
import mooncake.example.bank.domain.user.UserEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

public class JwtProcessorTest extends DummyObjectCreator {

    private User mockUser;
    private LoginUser loginUser;

    @BeforeEach
    void given_be() {
        mockUser = newMockUser(1L, "mooncake", "문케익");
        loginUser = new LoginUser(mockUser);
    }

    @Test
    @DisplayName("create() 이 정상적으로 동작한다")
    void createTest() throws Exception {

        // given (존재한다고 가정하는 유저)
        //when
        String jwtToken = JwtProcessor.create(loginUser);
        System.out.println("jwtToken = " + jwtToken);

        // 잘 만들어졌으면... Bearer 가 붙어있을 것이다
        assertTrue(jwtToken.startsWith(JwtParams.PREFIX));
    }

    @Test
    @DisplayName("verify() 정상 - 정상적인 유저에게 발급한 토큰으로 정상적인 정보를 받아올 수 있다")
    void verifyTest() throws Exception {

        //given
        String jwtToken = JwtProcessor.create(loginUser);
        jwtToken = jwtToken.replace(JwtParams.PREFIX, "");

        //when
        LoginUser resultUser = JwtProcessor.verify(jwtToken);

        //then
        assertThat(resultUser.getUser().getId()).isEqualTo(1L);
//        org.assertj.core.api.Assertions.assertThat(resultUser.getUser().getUsername()).isEqualTo("mooncake");
        assertThat(resultUser.getUser().getRole()).isEqualTo(UserEnum.CUSTOMER);

    }

    @Test
    @DisplayName("ADMIN ROLE 로 바꿔서 토큰 생성시 정말 Verify 이후 ADMIN 으로 받아온다")
    void verifyTest_is_admin_verified() throws Exception {

        // given
        mockUser.setRole(UserEnum.ADMIN);
        LoginUser loginAdminUser = new LoginUser(mockUser);
        String token = JwtProcessor.create(loginAdminUser);
        token = token.replace(JwtParams.PREFIX, "");

        // when
        LoginUser verify = JwtProcessor.verify(token);

        // then
        assertThat(verify.getUser().getRole()).isEqualTo(UserEnum.ADMIN);
    }


    @Test
    void verifyTest_fail() throws Exception {

        //given
        String jwtToken = JwtProcessor.create(loginUser); // "앞에 BEARER 같은 쓸데없는게 붙어 있음"
        String noToken = "NOT_TOKEN";

        // when
        // then
        // 해보면 알겠지만 둘이 발생시키는 에러 종류는 다름 - 모두 JWTVerificationException 의 Exception 자식들임
        assertThatThrownBy(() -> JwtProcessor.verify(jwtToken)).isInstanceOf(JWTVerificationException.class);
        assertThatThrownBy(() -> JwtProcessor.verify(noToken)).isInstanceOf(JWTVerificationException.class);

    }
}