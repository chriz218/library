package posmy.interview.boot.appuser.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import posmy.interview.boot.appuser.AppUserRole;
import posmy.interview.boot.appuser.entity.AppUser;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(
        properties = {
                "spring.jpa.properties.javax.persistence.validation.mode=none"
        }
)
class AppUserRepositoryTest {
    @Autowired
    private AppUserRepository appUserRepository;

    @Test
    void itShouldBeAbleToSaveAppUserAndFindByUsernameAndId() {
        String username = "johnwang";
        AppUser appUser = new AppUser(username, "John", "Wang", "12345678", AppUserRole.MEMBER, 3);
        this.appUserRepository.save(appUser);
        Optional<AppUser> optionalAppUserByUsername = this.appUserRepository.findByUsername(username);
        assertThat(optionalAppUserByUsername)
                .isPresent()
                .hasValueSatisfying(u-> {
                    assertThat(u).usingRecursiveComparison().isEqualTo(appUser);
                });
        Optional<AppUser> optionalAppUserById = this.appUserRepository.findById(optionalAppUserByUsername.get().getId());
        assertThat(optionalAppUserById)
                .isPresent()
                .hasValueSatisfying(u-> {
                    assertThat(u).usingRecursiveComparison().isEqualTo(appUser);
                });
    }

    @Test
    void itShouldBeAbleToFindAllAppUsers() {
        String memberUsername = "johnwang";
        AppUser member = new AppUser(memberUsername, "John", "Wang", "12345678", AppUserRole.MEMBER, 3);
        this.appUserRepository.save(member);
        String librarianUsername = "johnwong";
        AppUser librarian = new AppUser(librarianUsername, "John", "Wong", "12345678", AppUserRole.LIBRARIAN, 0);
        this.appUserRepository.save(librarian);
        String librarian2Username = "sarahwong";
        AppUser librarian2 = new AppUser(librarian2Username, "Sarah", "Wong", "12345678", AppUserRole.LIBRARIAN, 0);
        List<AppUser> appUsers = this.appUserRepository.findAll();
        assertThat(appUsers)
                .contains(member, librarian)
                .doesNotContain(librarian2);
    }

}