package posmy.interview.boot.security.dto;

import lombok.*;

@Getter
@NoArgsConstructor
public class LoginRequest {
    private String username;
    private String password;
}
