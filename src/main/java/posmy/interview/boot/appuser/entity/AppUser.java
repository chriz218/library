package posmy.interview.boot.appuser.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import posmy.interview.boot.appuser.AppUserRole;
import posmy.interview.boot.book.entity.Book;

import javax.persistence.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@Entity
@NoArgsConstructor
@Table(name = "APP_USER")
public class AppUser implements UserDetails {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "ID", columnDefinition = "CHAR(50)", unique = true, updatable = false)
    private String id;

    @Column(name = "USERNAME", unique = true, nullable = false, updatable = false)
    private String username;

    @Column(name = "FIRST_NAME", nullable = false)
    private String firstName;

    @Column(name = "LAST_NAME", nullable = false)
    private String lastName;

    @Column(name = "PASSWORD", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "APP_USER_ROLE", nullable = false)
    private AppUserRole appUserRole;

    @JsonManagedReference
    @OneToMany(mappedBy = "borrower", fetch = FetchType.EAGER)
    private List<Book> borrowedBooks;

    @Column(name = "MEMBERSHIP_LEVEL", nullable = false)
    private Integer membershipLevel;

    @Column(name = "LOCKED", nullable = false)
    private Boolean locked = false;

    @Column(name = "ENABLED", nullable = false)
    private Boolean enabled = true;

    public AppUser(String id,
                   String username,
                   String firstName,
                   String lastName,
                   String password,
                   AppUserRole appUserRole,
                   Integer membershipLevel) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.appUserRole = appUserRole;
        this.membershipLevel = membershipLevel;
    }

    public AppUser(String username,
                   String firstName,
                   String lastName,
                   String password,
                   AppUserRole appUserRole,
                   Integer membershipLevel) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.appUserRole = appUserRole;
        this.membershipLevel = membershipLevel;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + this.appUserRole.name());
        return Collections.singletonList(authority);
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !this.locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}
