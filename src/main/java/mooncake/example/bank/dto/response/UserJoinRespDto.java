package mooncake.example.bank.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mooncake.example.bank.domain.user.User;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserJoinRespDto {

    private Long id;
    private String username;
    private String fullName;

    public UserJoinRespDto(User user) {

        this.id = user.getId();
        this.username = user.getUsername();
        this.fullName = user.getFullName();

    }
}
