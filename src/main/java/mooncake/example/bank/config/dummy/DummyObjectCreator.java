package mooncake.example.bank.config.dummy;

import mooncake.example.bank.domain.user.User;
import mooncake.example.bank.domain.user.UserEnum;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class DummyObjectCreator {

    protected User newMockUser(Long id, String username, String fullName) {

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPw = passwordEncoder.encode("1234");

        return User.builder()
                .id(id)
                .username(username)
                .password(encodedPw)
                .email(username + "@hello.com")
                .fullName(fullName)
                .role(UserEnum.CUSTOMER)
                .build();

    }
}
