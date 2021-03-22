package posmy.interview.boot.appuser.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class UpdateUserPasswordRequest {
    private final String currentPassword;
    private final String newPassword;
}
