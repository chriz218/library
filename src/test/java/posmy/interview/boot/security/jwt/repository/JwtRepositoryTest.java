package posmy.interview.boot.security.jwt.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import posmy.interview.boot.security.jwt.entity.Jwt;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(
        properties = {
                "spring.jpa.properties.javax.persistence.validation.mode=none"
        }
)
class JwtRepositoryTest {
    @Autowired
    private JwtRepository jwtRepository;

    @Test
    void itShouldSaveJwtAndAllowSearchByUsername() {
        Jwt token1 = new Jwt("johnToken1", "johnwang");
        Jwt token2 = new Jwt("johnToken2", "johnwang");
        Jwt token3 = new Jwt("sarahToken", "sarahwang");

        this.jwtRepository.save(token1);
        this.jwtRepository.save(token2);
        this.jwtRepository.save(token3);

        List<Jwt> jwtsForJohn = this.jwtRepository.findJwtsByUsername("johnwang");

        jwtsForJohn.forEach(jwt -> {
            assertThat(jwt.getUsername()).isEqualTo("johnwang");
            assertThat(jwt.getUsername()).isNotEqualTo("sarahwang");
        });
    }
}