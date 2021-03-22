package posmy.interview.boot.appuser.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class NewMemberRequest {
    private final String username;
    private final String firstName;
    private final String lastName;
    private final String password;
    private final Integer membershipLevel;
}
