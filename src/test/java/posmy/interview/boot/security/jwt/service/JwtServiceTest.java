package posmy.interview.boot.security.jwt.service;

import javassist.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import posmy.interview.boot.security.jwt.entity.Jwt;
import posmy.interview.boot.security.jwt.repository.JwtRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

class JwtServiceTest {
    @Mock
    private JwtRepository jwtRepository;
    @Captor
    private ArgumentCaptor<Jwt> jwtArgumentCaptor;
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.jwtService = new JwtService(this.jwtRepository);
    }

    @Test
    void itShouldAddJwt() {
        this.jwtService.addJwt("Token", "johnwang");
        then(this.jwtRepository).should().save(this.jwtArgumentCaptor.capture());
        assertThat(this.jwtArgumentCaptor.getValue().getUsername()).isEqualTo("johnwang");
        assertThat(this.jwtArgumentCaptor.getValue().getId()).isEqualTo("Token");
    }

    @Test
    void itShouldNotBeActive() {
        given(this.jwtRepository.findById(anyString())).willReturn(Optional.empty());
        assertThat(this.jwtService.tokenIsActive("token")).isFalse();
    }

    @Test
    void itShouldBeActive() {
        Jwt jwt = new Jwt("Token", "johnwang");
        given(this.jwtRepository.findById(jwt.getId())).willReturn(Optional.of(jwt));
        assertThat(this.jwtService.tokenIsActive(jwt.getId())).isTrue();
    }

    @Test
    void itShouldDeactivateJwt() {
        String token = "Token";
        Jwt jwt = new Jwt(token, "johnwang");
        given(this.jwtRepository.findById(jwt.getId())).willReturn(Optional.of(jwt));
        this.jwtService.deactivateJwt(token);
        then(this.jwtRepository).should().delete(jwt);
    }

}