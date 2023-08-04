package mooncake.example.bank.config.security.auth;

import mooncake.example.bank.domain.user.User;
import mooncake.example.bank.domain.user.UserRepository;
import mooncake.example.bank.dto.response.UserRespDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/*
 Login 요청정보를 통해 실제 DB 에서 User 정보가
 있는지 확인하는 공간
 */
@Service
public class LoginService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User userPs = userRepository.findByUsername(username).orElseThrow(
                // 제어권은 기본적으로 자신이 가져오는게 더 좋음
                () -> new InternalAuthenticationServiceException(username + "의 유저를 찾지 못했습니다") // 401
        );

        return new LoginUser(userPs);
    }
}
