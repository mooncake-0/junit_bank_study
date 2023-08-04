package mooncake.example.bank.dto.request;

import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class UserReqDto {

    @Getter
    @Setter
    public static class LoginReqDto {
        private String username;
        private String password;
    }


    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class UserJoinReqDto {

        // 영어, 숫자 되고, 길이 2~20
        @Pattern(regexp = "^[a-zA-Z0-9]{2,20}$", message = "영문 / 숫자 2~20자 이내로 작성")
        @NotEmpty
        private String username;

        // 길이 4~20 (String 일 경우 편하게 가능)
        // 단, 비밀번호도 조합을 검증해야할 수도 있음
        @NotEmpty
        @Size(min = 4, max = 20)
        private String password;


        // ㅋㅋㅋ 그냥 받고 AOP에서 includes 로 거르면 안됨? (Validation 적용해서 편하게 하려고 하는거..ㅜㅠ)
        // 이메일 형식이여야 한다
        @Pattern(regexp = "^[a-zA-Z0-9]{2,10}@[a-zA-Z0-9]{2,6}\\.[a-zA-Z]{1,10}$", message = "ㅈㄹㅈㄹ")
        @NotEmpty
        private String email;

        //영어 한글만 되고, 20자 이내이다
        @Pattern(regexp = "^[a-zA-Z가-힣]{1,20}$", message = "ㅈㄹㅈㄹ")
        @NotEmpty
        private String fullName;

    }
}
