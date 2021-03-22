package posmy.interview.boot.security.controller;

import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import posmy.interview.boot.security.jwt.config.JwtConfig;
import posmy.interview.boot.security.jwt.service.JwtService;

@RestController
@RequestMapping(path = "api/v1/auth")
@AllArgsConstructor
public class AuthController {
    private final JwtConfig jwtConfig;
    private final JwtService jwtService;

    @PostMapping(path = "/logout")
    @PreAuthorize("hasAnyRole('ROLE_LIBRARIAN', 'ROLE_MEMBER')")
    public String logout(@RequestHeader(value = "Authorization") String bearerString) {
        String token = bearerString.replace(this.jwtConfig.getTokenPrefix(), "");
        this.jwtService.deactivateJwt(token);
        return "Logged out successfully";
    }
}
