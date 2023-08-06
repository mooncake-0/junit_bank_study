package mooncake.example.bank.service;

import mooncake.example.bank.config.dummy.DummyObjectCreator;
import mooncake.example.bank.domain.user.User;
import mooncake.example.bank.domain.user.UserRepository;
import mooncake.example.bank.dto.request.UserReqDto;
import mooncake.example.bank.dto.response.UserRespDto;
import mooncake.example.bank.handler.exception.CustomApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ActiveProfiles(value = "test")
@ExtendWith(MockitoExtension.class) // 모키토 환경에서 Test 진행한다는 뜻 // 해주는 일 정확히 알면 좋을 듯!
public class UserServiceTest extends DummyObjectCreator {

    // Mockito 환경을 만들어 줬으니 거기에 있는 가짜 객체를 전달해준다
    // 단, Mockito 환경은 가짜 환경이므로 Spring Bean 들이 하나도 등록되어 있지 않다
    @InjectMocks
    private UserService userService;

    // Bean 이 없으므로 Service 는 이게 필요할 것임 // *** 하지만 우린 UserRepo 테스트하는게 아님 ***
    // 그래서 가짜로 진행해준다
    // 이런 Repo 를 주입할 경우 STUB 을 진행해준다 (행위 등록)
    @Mock
    private UserRepository userRepository;

    // 진짜 객체만을 사용하기 위한 Spy --> 의존성주입이 아니라 객체 생성 후 사용하는 방도랑 똑같 (
    // 따라서 주입시키는거랍 시고 Interface 주입하면 동작하지않는다
    // 구현체를 넣어줘야 함
    @Spy
    //private PasswordEncoder passwordEncoder;
    private BCryptPasswordEncoder passwordEncoder;

    @Test
    void test_회원가입() throws Exception {

        // given (보통 들어가는 Param)
        UserReqDto.UserJoinReqDto requestDto = new UserReqDto.UserJoinReqDto(
                "회원이름", "1234", "회원이름@hello.com", "ssar"
        );

        // given - stub1 - stub 이란 가정법을 말함
        // any -> 뭐라도 들어간다면 텅빈 Optional 객체가 전달된다
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());

        // given - stub2 - Dummy Object Creator 활용하여 직접 생성하지 않고, 조금은 간단하게 Refactor
        User ssar = newMockUser(1L, "회원이름", "ssar");
        when(userRepository.save(any())).thenReturn(ssar);

        //when
        UserRespDto.UserJoinRespDto responseDto = userService.save(requestDto);

        //then
        assertThat(responseDto.getId()).isEqualTo(1L);
        assertThat(responseDto.getUsername()).isEqualTo("회원이름");
        assertThat(responseDto.getFullName()).isEqualTo("ssar");
    }


    @Test
    void test_회원가입_실패() {

        //given
        UserReqDto.UserJoinReqDto requestDto = new UserReqDto.UserJoinReqDto(
                "회원이름", "1234", "hello@a.com", "김상수"
        );

        // given - stub1 - stub 이란 가정법을 말함
        when(userRepository.findByUsername(any())).thenReturn(Optional.of(new User())); // 어떤 상태의 User 가 들어간다

        //when
        //then
        assertThatThrownBy(() -> userService.save(requestDto)).isInstanceOf(CustomApiException.class);
    }
}