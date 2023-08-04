package mooncake.example.bank.config.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import mooncake.example.bank.config.security.auth.LoginUser;
import mooncake.example.bank.domain.user.User;
import mooncake.example.bank.domain.user.UserEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class JwtProcessor {

    private final Logger log = LoggerFactory.getLogger(getClass());

    public static String create(LoginUser loginUser) {
        String jwtToken = JWT.create()
                .withSubject("BANK")
                .withExpiresAt(new Date(System.currentTimeMillis() + JwtParams.EXPIRES_AT))
                .withClaim("id", loginUser.getUser().getId())
                .withClaim("role", loginUser.getUser().getRole() + "")
                .sign(Algorithm.HMAC512(JwtParams.SECRET));// JWT 는 대칭키로도 충분함 > 공개키를 줘봤자 사용할 일이 없기 때문. 그냥 버스 티켓 같은거임
        return JwtParams.PREFIX + jwtToken;
    }

    public static LoginUser verify(String token) {

        DecodedJWT decoded = JWT.require(Algorithm.HMAC512(JwtParams.SECRET)).build().verify(token);

        Long id = decoded.getClaim("id").asLong();
        String role = decoded.getClaim("role").asString();

        // 단순히 Authentication 객체 생성 용으로 쓰인다 ( role 을 통한 인가 확인 용)
        // 더 부가적인 정보를 위해선 id 를 가지고 Spring 단에서 호출해서 사용해야 한다
        User onlyForAuthentication = User.builder().id(id).role(UserEnum.valueOf(role)).build();

        return new LoginUser(onlyForAuthentication);
    }
}
