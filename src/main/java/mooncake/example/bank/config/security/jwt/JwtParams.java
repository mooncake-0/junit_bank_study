package mooncake.example.bank.config.security.jwt;

public abstract class JwtParams {

    public static final String SECRET = "MOONCAKE"; // 절대 공개되면 안됨
    public static final String HEADER = "Authorization";
    public static final String PREFIX = "Bearer ";
    public static final int EXPIRES_AT = 1000 * 60 * 60 * 24 * 7; // 1주일

}
