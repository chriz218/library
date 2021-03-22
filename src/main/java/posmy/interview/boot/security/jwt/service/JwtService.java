package posmy.interview.boot.security.jwt.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import posmy.interview.boot.security.jwt.entity.Jwt;
import posmy.interview.boot.security.jwt.repository.JwtRepository;

import java.util.Optional;

@Service
@AllArgsConstructor
public class JwtService {
    private final JwtRepository jwtRepository;

    @Transactional
    public void addJwt(String token, String username) {
        Jwt jwt = new Jwt(token, username);
        this.jwtRepository.save(jwt);
    }

    public Boolean tokenIsActive(String token) {
        if (this.jwtRepository.findById(token).isPresent()) {
            return true;
        }
        return false;
    }

    @Transactional
    public void deactivateJwt(String token) {
        Optional<Jwt> optionalJwt = this.jwtRepository.findById(token);
        Jwt jwt = optionalJwt.get();
        this.jwtRepository.delete(jwt);
    }
}
