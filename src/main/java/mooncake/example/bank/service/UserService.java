package mooncake.example.bank.service;

import lombok.RequiredArgsConstructor;
import mooncake.example.bank.domain.user.User;
import mooncake.example.bank.domain.user.UserEnum;
import mooncake.example.bank.domain.user.UserRepository;
import mooncake.example.bank.dto.request.UserReqDto;
import mooncake.example.bank.dto.response.UserRespDto;
import mooncake.example.bank.handler.exception.CustomApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserRespDto.UserJoinRespDto save(UserReqDto.UserJoinReqDto requestDto) {

        // 동일 유저 검사
        Optional<User> userOp = userRepository.findByUsername(requestDto.getUsername());
        if (userOp.isPresent()) { // 유저 네임 중복
            throw new CustomApiException("중복되는 이름의 유저가 있습니다");
        }

        User saveUser = User.builder()
                .username(requestDto.getUsername())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .email(requestDto.getEmail())
                .fullName(requestDto.getFullName())
                .role(UserEnum.CUSTOMER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 영속화
         User userPs = userRepository.save(saveUser);

        return new UserRespDto.UserJoinRespDto(userPs);

    }
}
