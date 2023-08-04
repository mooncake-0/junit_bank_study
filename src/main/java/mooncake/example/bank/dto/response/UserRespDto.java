package mooncake.example.bank.dto.response;

import lombok.Getter;
import lombok.Setter;
import mooncake.example.bank.domain.user.User;

public class UserRespDto {

    @Getter
    @Setter
    public static class UserJoinRespDto{
        private Long id;
        private String username;
        private String fullName;

        public UserJoinRespDto(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.fullName = user.getFullName();
        }
    }


    @Getter
    @Setter
    public static class LoginRespDto{
        private Long id;
        private String username;
        private String createdAt;

        public LoginRespDto(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.createdAt = user.getCreatedAt() + "";
        }
    }
}
