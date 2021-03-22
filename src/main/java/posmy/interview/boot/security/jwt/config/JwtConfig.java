package posmy.interview.boot.security.jwt.config;

import com.google.common.net.HttpHeaders;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class JwtConfig {
    @Value("${jwt.secretkey}")
    private String secretKey;
    @Value("${jwt.tokenprefix}")
    private String tokenPrefix;
    @Value("${jwt.tokenexpirationafterdays}")
    private Integer tokenExpirationAfterDays;

    public String getAuthorizationHeader() {
        return HttpHeaders.AUTHORIZATION;
    }
}
