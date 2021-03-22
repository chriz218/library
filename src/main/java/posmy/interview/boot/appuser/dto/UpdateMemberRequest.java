package posmy.interview.boot.appuser.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class UpdateMemberRequest {
    private final String firstName;
    private final String lastName;
    private final Integer membershipLevel;
}
