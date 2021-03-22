package posmy.interview.boot.appuser.service;

import javassist.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import posmy.interview.boot.appuser.AppUserRole;
import posmy.interview.boot.appuser.dto.NewLibrarianRequest;
import posmy.interview.boot.appuser.dto.NewMemberRequest;
import posmy.interview.boot.appuser.dto.UpdateMemberRequest;
import posmy.interview.boot.appuser.dto.UpdateUserPasswordRequest;
import posmy.interview.boot.appuser.entity.AppUser;
import posmy.interview.boot.appuser.repository.AppUserRepository;
import posmy.interview.boot.book.BookStatus;
import posmy.interview.boot.book.entity.Book;
import posmy.interview.boot.security.jwt.entity.Jwt;
import posmy.interview.boot.security.jwt.repository.JwtRepository;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

class AppUserServiceTest {
    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private JwtRepository jwtRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Captor
    private ArgumentCaptor<AppUser> appUserArgumentCaptor;
    private AppUserService appUserService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.appUserService = new AppUserService(this.appUserRepository, this.jwtRepository, this.passwordEncoder);
    }

    @Test
    void itShouldLoadByUsername() {
        String id = UUID.randomUUID().toString();
        String username = "john";
        AppUser member = new AppUser(id, username, "John", "Wang", "12345678", AppUserRole.MEMBER, 2);
        given(this.appUserRepository.findByUsername(username)).willReturn(Optional.of(member));
        // Check if Equal by comparing field to field
        assertThat(this.appUserService.loadUserByUsername(username)).usingRecursiveComparison().isEqualTo(member);
    }

    @Test
    void itShouldThrowUserNotFountException() {
        given(this.appUserRepository.findByUsername(anyString())).willReturn(Optional.empty());
        assertThatThrownBy(() -> this.appUserService.loadUserByUsername("eric"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User: eric cannot be found");
    }

    @Test
    void itShouldThrowNullPointerExceptionForRegisterMember() {
        NewMemberRequest request = new NewMemberRequest("john", "John", "Wang", null, 1);
        assertThatThrownBy(() -> this.appUserService.registerMember(request))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Member must have a username, first name, last name, password and membership level");
    }

    @Test
    void itShouldThrowNumberFormatExceptionForRegisterMember() {
        NewMemberRequest request = new NewMemberRequest("john", "John", "Wang", "123", 0);
        assertThatThrownBy(() -> this.appUserService.registerMember(request))
                .isInstanceOf(NumberFormatException.class)
                .hasMessageContaining("Membership Level should be at least 1");
    }

    @Test
    void itShouldThrowIllegalStateExceptionForRegisterMember() {
        String id = UUID.randomUUID().toString();
        String username = "john";
        AppUser member = new AppUser(id, username, "John", "Wang", "123", AppUserRole.MEMBER, 2);
        NewMemberRequest request = new NewMemberRequest(username, "John", "Wang", "123", 2);
        given(this.appUserRepository.findByUsername("john")).willReturn(Optional.of(member));
        assertThatThrownBy(() -> this.appUserService.registerMember(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(request.getUsername().toLowerCase() + " is already taken");
    }

    @Test
    void itShouldSucceedForRegisterMember() {
        String id = UUID.randomUUID().toString();
        String username = "john";
        AppUser member = new AppUser(id, username, "John", "Wang", "123", AppUserRole.MEMBER, 2);
        NewMemberRequest request = new NewMemberRequest(username, "John", "Wang", "123", 2);
        given(this.appUserRepository.findByUsername("john")).willReturn(Optional.empty());
        given(this.passwordEncoder.encode(request.getPassword())).willReturn(request.getPassword());
        this.appUserService.registerMember(request);
        then(this.appUserRepository).should().save(this.appUserArgumentCaptor.capture());
        assertThat(this.appUserArgumentCaptor.getValue())
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(member);
    }

    @Test
    void itShouldThrowNullPointerExceptionForRegisterLibrarian() {
        NewLibrarianRequest request = new NewLibrarianRequest("johnwang", "john", "wang", null);
        assertThatThrownBy(() -> this.appUserService.registerLibrarian(request))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Librarian must have a username, first name, last name, and password");
    }

    @Test
    void itShouldThrowIllegalStateExceptionForRegisterLibrarian() {
        String id = UUID.randomUUID().toString();
        String username = "johnwang";
        AppUser librarian = new AppUser(id, username, "John", "Wang", "123", AppUserRole.LIBRARIAN, 0);
        NewLibrarianRequest request = new NewLibrarianRequest(username, "John", "Wang", "123");
        given(this.appUserRepository.findByUsername(username)).willReturn(Optional.of(librarian));
        assertThatThrownBy(() -> this.appUserService.registerLibrarian(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(request.getUsername().toLowerCase() + " is already taken");
    }

    @Test
    void itShouldSucceedForRegisterLibrarian() {
        String id = UUID.randomUUID().toString();
        String username = "johnwang";
        AppUser librarian = new AppUser(id, username, "John", "Wang", "123", AppUserRole.LIBRARIAN, 0);
        NewLibrarianRequest request = new NewLibrarianRequest(username, "John", "Wang", "123");
        given(this.appUserRepository.findByUsername(username)).willReturn(Optional.empty());
        given(this.passwordEncoder.encode(request.getPassword())).willReturn(request.getPassword());
        this.appUserService.registerLibrarian(request);
        then(this.appUserRepository).should().save(this.appUserArgumentCaptor.capture());
        assertThat(this.appUserArgumentCaptor.getValue())
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(librarian);
    }

    @Test
    void itShouldThrowNotFoundExceptionForRetrieveLoggedInUser() {
        String username = "johnwang";
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        Principal principal = mock(Principal.class);
        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.getPrincipal()).willReturn(principal);
        given(principal.toString()).willReturn(username);
        SecurityContextHolder.setContext(securityContext);

        assertThatThrownBy(() -> this.appUserService.retrieveLoggedInUser())
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User " + username + " cannot be found");
    }

    @Test
    void itShouldRetrieveLoggedInUser() throws NotFoundException {
        String username = "johnwang";
        String id = UUID.randomUUID().toString();
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        Principal principal = mock(Principal.class);
        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.getPrincipal()).willReturn(principal);
        given(principal.toString()).willReturn(username);
        SecurityContextHolder.setContext(securityContext);
        AppUser librarian = new AppUser(id, username, "John", "Wang", "123", AppUserRole.LIBRARIAN, 0);
        given(this.appUserRepository.findByUsername(username)).willReturn(Optional.of(librarian));
        assertThat(this.appUserService.retrieveLoggedInUser())
                .usingRecursiveComparison()
                .isEqualTo(librarian);
    }

    @Test
    void itShouldRetrieveLoggedInUserToo() throws NotFoundException {
        String username = "johnwang";
        String id = UUID.randomUUID().toString();
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);
        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.getPrincipal()).willReturn(userDetails);
        given(userDetails.getUsername()).willReturn(username);
        SecurityContextHolder.setContext(securityContext);
        AppUser librarian = new AppUser(id, username, "John", "Wang", "123", AppUserRole.LIBRARIAN, 0);
        given(this.appUserRepository.findByUsername(username)).willReturn(Optional.of(librarian));
        assertThat(this.appUserService.retrieveLoggedInUser())
                .usingRecursiveComparison()
                .isEqualTo(librarian);
    }

    @Test
    void itShouldThrowNotFoundExceptionForRetrieveUser() {
        String id = UUID.randomUUID().toString();
        given(this.appUserRepository.findById(id)).willReturn(Optional.empty());
        assertThatThrownBy(() -> this.appUserService.retrieveUser(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User with id " + id + " cannot be found");
    }

    @Test
    void itShouldRetrieveUser() throws NotFoundException {
        String username = "johnwang";
        String id = UUID.randomUUID().toString();
        AppUser librarian = new AppUser(id, username, "John", "Wang", "123", AppUserRole.LIBRARIAN, 0);
        given(this.appUserRepository.findById(id)).willReturn(Optional.of(librarian));
        assertThat(this.appUserService.retrieveUser(id))
                .usingRecursiveComparison()
                .isEqualTo(librarian);
    }

    @Test
    void itShouldThrowNullPointerExceptionForChangePassword() {
        UpdateUserPasswordRequest request = new UpdateUserPasswordRequest("123", null);
        assertThatThrownBy(() -> this.appUserService.changePassword(request))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Please enter current password and new password");
    }

    @Test
    void itShouldThrowIllegalStateExceptionForChangePassword() {
        String username = "johnwang";
        String id = UUID.randomUUID().toString();
        String password = "123";
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        Principal principal = mock(Principal.class);
        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.getPrincipal()).willReturn(principal);
        given(principal.toString()).willReturn(username);
        SecurityContextHolder.setContext(securityContext);
        AppUser librarian = new AppUser(id, username, "John", "Wang", password, AppUserRole.LIBRARIAN, 0);
        given(this.appUserRepository.findByUsername(username)).willReturn(Optional.of(librarian));

        UpdateUserPasswordRequest request = new UpdateUserPasswordRequest("qwe", "1234");
        given(this.passwordEncoder.matches(request.getCurrentPassword(), librarian.getPassword()))
                .willReturn(request.getCurrentPassword().equals(librarian.getPassword()));

        assertThatThrownBy(() -> this.appUserService.changePassword(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Wrong Password");
    }

    @Test
    void itShouldChangePassword() throws NotFoundException {
        String username = "johnwang";
        String id = UUID.randomUUID().toString();
        String password = "123";
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        Principal principal = mock(Principal.class);
        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.getPrincipal()).willReturn(principal);
        given(principal.toString()).willReturn(username);
        SecurityContextHolder.setContext(securityContext);
        AppUser librarian = new AppUser(id, username, "John", "Wang", password, AppUserRole.LIBRARIAN, 0);
        given(this.appUserRepository.findByUsername(username)).willReturn(Optional.of(librarian));

        UpdateUserPasswordRequest request = new UpdateUserPasswordRequest("123", "1234");
        given(this.passwordEncoder.matches(request.getCurrentPassword(), librarian.getPassword()))
                .willReturn(request.getCurrentPassword().equals(librarian.getPassword()));
        given(this.passwordEncoder.encode(request.getNewPassword())).willReturn(request.getNewPassword());

        assertThat(this.appUserService.changePassword(request)).isEqualTo("User " + librarian.getUsername() + " has successfully changed password");

        then(this.appUserRepository).should().save(this.appUserArgumentCaptor.capture());
        assertThat(this.appUserArgumentCaptor.getValue().getPassword()).isEqualTo(request.getNewPassword());
        assertThat(this.appUserArgumentCaptor.getValue()).usingRecursiveComparison().isEqualTo(librarian);
    }

    @Test
    void itShouldThrowIllegalStateExceptionForDeleteSelf() {
        String token = "sampleToken";
        String username = "johnwang";
        String id = UUID.randomUUID().toString();
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        Principal principal = mock(Principal.class);
        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.getPrincipal()).willReturn(principal);
        given(principal.toString()).willReturn(username);
        SecurityContextHolder.setContext(securityContext);
        AppUser member = new AppUser(id, username, "John", "Wang", "123", AppUserRole.MEMBER, 5);
        Book book1 = new Book(
                UUID.randomUUID().toString(),
                "book1",
                "author1",
                BookStatus.BORROWED,
                100,
                "XXXX",
                member);
        List<Book> books = new ArrayList<>();
        books.add(book1);
        member.setBorrowedBooks(books);
        given(this.appUserRepository.findByUsername(username)).willReturn(Optional.of(member));

        assertThatThrownBy(() -> this.appUserService.deleteSelf(token))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("User " + member.getUsername() + " has borrowed some books. Books need to be returned first");
    }

    @Test
    void itShouldDeleteSelf() throws NotFoundException {
        String token = "sampleToken";
        String username = "johnwang";
        String id = UUID.randomUUID().toString();
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        Principal principal = mock(Principal.class);
        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.getPrincipal()).willReturn(principal);
        given(principal.toString()).willReturn(username);
        SecurityContextHolder.setContext(securityContext);
        AppUser member = new AppUser(id, username, "John", "Wang", "123", AppUserRole.MEMBER, 5);
        List<Book> books = new ArrayList<>();
        member.setBorrowedBooks(books);
        given(this.appUserRepository.findByUsername(username)).willReturn(Optional.of(member));

        assertThat(this.appUserService.deleteSelf(token)).isEqualTo(member.getUsername() + "\'s account has been deleted");

        then(this.jwtRepository).should().deleteById(token);
        then(this.appUserRepository).should().deleteById(member.getId());
    }

    @Test
    void itShouldThrowIllegalStateException1WhenDeleteMember() {
        String username = "johnwang";
        String id = UUID.randomUUID().toString();
        AppUser librarian = new AppUser(id, username, "John", "Wang", "123", AppUserRole.LIBRARIAN, 0);
        given(this.appUserRepository.findById(id)).willReturn(Optional.of(librarian));
        assertThatThrownBy(() -> this.appUserService.deleteMember(id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("User with id " + id + " is a librarian.");
    }

    @Test
    void itShouldThrowIllegalStateException2WhenDeleteMember() {
        String username = "johnwang";
        String id = UUID.randomUUID().toString();
        AppUser member = new AppUser(id, username, "John", "Wang", "123", AppUserRole.MEMBER, 2);
        Book book1 = new Book(
                UUID.randomUUID().toString(),
                "book1",
                "author1",
                BookStatus.BORROWED,
                100,
                "XXXX",
                member);
        List<Book> books = new ArrayList<>();
        books.add(book1);
        member.setBorrowedBooks(books);
        given(this.appUserRepository.findById(id)).willReturn(Optional.of(member));
        assertThatThrownBy(() -> this.appUserService.deleteMember(id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Member with id " + id + " has borrowed some books. Books need to be returned first");
    }

    @Test
    void itShouldDeleteMember() throws NotFoundException {
        String username = "johnwang";
        String id = UUID.randomUUID().toString();
        AppUser member = new AppUser(id, username, "John", "Wang", "123", AppUserRole.MEMBER, 2);
        List<Book> books = new ArrayList<>();
        member.setBorrowedBooks(books);
        given(this.appUserRepository.findById(id)).willReturn(Optional.of(member));

        Jwt jwtToken1 = new Jwt("token1", username);
        Jwt jwtToken2 = new Jwt("token2", username);
        List<Jwt> jwtList = new ArrayList<>();
        jwtList.add(jwtToken1);
        jwtList.add(jwtToken2);

        given(this.jwtRepository.findJwtsByUsername(username)).willReturn(jwtList);
        assertThat(this.appUserService.deleteMember(id)).isEqualTo("Member with id " + id + " has been deleted successfully");
        then(this.jwtRepository).should().deleteAll(jwtList);
        then(this.appUserRepository).should().deleteById(member.getId());
    }

    @Test
    void itShouldThrowIllegalStateExceptionWhenUpdatingLibrarian() {
        String username = "johnwang";
        String id = UUID.randomUUID().toString();
        AppUser librarian = new AppUser(id, username, "John", "Wang", "123", AppUserRole.LIBRARIAN, 0);
        given(this.appUserRepository.findById(id)).willReturn(Optional.of(librarian));
        UpdateMemberRequest request = new UpdateMemberRequest("Jack", "Wong", 5);
        assertThatThrownBy(() -> this.appUserService.updateMember(id, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("User with id " + id + " is a librarian.");
    }

    @Test
    void itShouldThrowNumberFormatExceptionWhenUpdatingMember() {
        String username = "johnwang";
        String id = UUID.randomUUID().toString();
        AppUser member = new AppUser(id, username, "John", "Wang", "123", AppUserRole.MEMBER, 7);
        given(this.appUserRepository.findById(id)).willReturn(Optional.of(member));
        UpdateMemberRequest request = new UpdateMemberRequest("Jack", "Wong", 0);
        assertThatThrownBy(() -> this.appUserService.updateMember(id, request))
                .isInstanceOf(NumberFormatException.class)
                .hasMessageContaining("Membership Level should be at least 1");
    }

    @Test
    void itShouldThrowIllegalStateExceptionWhenUpdatingMember() {
        String username = "johnwang";
        String id = UUID.randomUUID().toString();
        AppUser member = new AppUser(id, username, "John", "Wang", "123", AppUserRole.MEMBER, 2);
        Book book1 = new Book(
                UUID.randomUUID().toString(),
                "book1",
                "author1",
                BookStatus.BORROWED,
                100,
                "XXXX",
                member);
        Book book2 = new Book(
                UUID.randomUUID().toString(),
                "book2",
                "author2",
                BookStatus.BORROWED,
                100,
                "YYYY",
                member);
        List<Book> books = new ArrayList<>();
        books.add(book1);
        books.add(book2);
        member.setBorrowedBooks(books);
        given(this.appUserRepository.findById(id)).willReturn(Optional.of(member));
        UpdateMemberRequest request = new UpdateMemberRequest("Jack", "Wong", 1);
        assertThatThrownBy(() -> this.appUserService.updateMember(id, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(
                        "User with id " + id + " currently has " +
                                member.getBorrowedBooks().size() + " books and cannot be given a membership level of "
                                + request.getMembershipLevel()
                );
    }

    @Test
    void itShouldUpdateMember() throws NotFoundException {
        String username = "johnwang";
        String id = UUID.randomUUID().toString();
        AppUser member = new AppUser(id, username, "John", "Wang", "123", AppUserRole.MEMBER, 7);
        Book book1 = new Book(
                UUID.randomUUID().toString(),
                "book1",
                "author1",
                BookStatus.BORROWED,
                100,
                "XXXX",
                member);
        List<Book> books = new ArrayList<>();
        books.add(book1);
        member.setBorrowedBooks(books);
        given(this.appUserRepository.findById(id)).willReturn(Optional.of(member));
        UpdateMemberRequest request = new UpdateMemberRequest("Jack", "Wong", 5);
        assertThat(this.appUserService.updateMember(id, request)).usingRecursiveComparison().isEqualTo(member);
        then(this.appUserRepository).should().save(this.appUserArgumentCaptor.capture());
    }

    @Test
    void itShouldFindAllUsers() {
        List<AppUser> appUsers = new ArrayList<>();
        String username = "johnwang";
        String id = UUID.randomUUID().toString();
        AppUser member = new AppUser(id, username, "John", "Wang", "123", AppUserRole.MEMBER, 7);
        appUsers.add(member);
        given(this.appUserRepository.findAll()).willReturn(appUsers);
        assertThat(this.appUserService.findAllUsers().size()).isEqualTo(1);
    }
}