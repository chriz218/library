package posmy.interview.boot.appuser.service;

import com.google.common.base.Strings;
import javassist.NotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import posmy.interview.boot.appuser.entity.AppUser;
import posmy.interview.boot.appuser.AppUserRole;
import posmy.interview.boot.appuser.dto.NewLibrarianRequest;
import posmy.interview.boot.appuser.dto.NewMemberRequest;
import posmy.interview.boot.appuser.dto.UpdateMemberRequest;
import posmy.interview.boot.appuser.dto.UpdateUserPasswordRequest;
import posmy.interview.boot.appuser.repository.AppUserRepository;
import posmy.interview.boot.security.jwt.entity.Jwt;
import posmy.interview.boot.security.jwt.repository.JwtRepository;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AppUserService implements UserDetailsService {
    private final AppUserRepository appUserRepository;
    private final JwtRepository jwtRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("User: %s cannot be found.", username)));
    }

    private boolean usernameIsTaken(String username) {
        return this.appUserRepository.findByUsername(username).isPresent();
    }

    private boolean memberIsInvalid(NewMemberRequest member) {
        if (Strings.isNullOrEmpty(member.getUsername()) ||
            Strings.isNullOrEmpty(member.getFirstName()) ||
            Strings.isNullOrEmpty(member.getLastName()) ||
            Strings.isNullOrEmpty(member.getPassword()) ||
            member.getMembershipLevel() == null) {
            return true;
        }
        return false;
    }

    private boolean librarianIsInvalid(NewLibrarianRequest librarian) {
        if (Strings.isNullOrEmpty(librarian.getUsername()) ||
            Strings.isNullOrEmpty(librarian.getFirstName()) ||
            Strings.isNullOrEmpty(librarian.getLastName()) ||
            Strings.isNullOrEmpty(librarian.getPassword())) {
            return true;
        }
        return false;
    }

    @Transactional
    public String registerMember(NewMemberRequest request) {
        if (this.memberIsInvalid(request)) {
            throw new NullPointerException("Member must have a username, first name, last name, password and membership level");
        }
        if (request.getMembershipLevel() <= 0) {
            throw new NumberFormatException("Membership Level should be at least 1");
        }
        if (this.usernameIsTaken(request.getUsername().toLowerCase())) {
            throw new IllegalStateException(request.getUsername().toLowerCase() + " is already taken");
        }
        AppUser newAppUser = new AppUser(
                request.getUsername().toLowerCase(),
                request.getFirstName(),
                request.getLastName(),
                this.passwordEncoder.encode(request.getPassword()),
                AppUserRole.MEMBER,
                request.getMembershipLevel()
        );
        this.appUserRepository.save(newAppUser);
        return "Successfully created member: " + newAppUser.getUsername() + "!";
    }

    @Transactional
    public String registerLibrarian(NewLibrarianRequest request) {
        if (this.librarianIsInvalid(request)) {
            throw new NullPointerException("Librarian must have a username, first name, last name, and password");
        }
        if (this.usernameIsTaken(request.getUsername().toLowerCase())) {
            throw new IllegalStateException(request.getUsername().toLowerCase() + " is already taken");
        }
        AppUser newAppUser = new AppUser(
                request.getUsername().toLowerCase(),
                request.getFirstName(),
                request.getLastName(),
                this.passwordEncoder.encode(request.getPassword()),
                AppUserRole.LIBRARIAN,
                0
        );
        this.appUserRepository.save(newAppUser);
        return "Successfully created librarian: " + newAppUser.getUsername() + "!";
    }

    private boolean updatePasswordRequestIsInValid(UpdateUserPasswordRequest updateUserPasswordRequest) {
        if (Strings.isNullOrEmpty(updateUserPasswordRequest.getCurrentPassword()) ||
            Strings.isNullOrEmpty(updateUserPasswordRequest.getNewPassword())) {
            return true;
        }
        return false;
    }

    @Transactional
    public String changePassword(UpdateUserPasswordRequest updateUserPasswordRequest) throws NotFoundException {
        if (this.updatePasswordRequestIsInValid(updateUserPasswordRequest)) {
            throw new NullPointerException("Please enter current password and new password");
        }
        AppUser currentUser = this.retrieveLoggedInUser();
        if (this.passwordEncoder.matches(updateUserPasswordRequest.getCurrentPassword(), currentUser.getPassword())) {
            currentUser.setPassword(this.passwordEncoder.encode(updateUserPasswordRequest.getNewPassword()));
            this.appUserRepository.save(currentUser);
            return "User " + currentUser.getUsername() + " has successfully changed password";
        } else {
            throw new IllegalStateException("Wrong Password");
        }
    }

    @Transactional
    public String deleteMember(String id) throws NotFoundException {
        AppUser appUser = this.retrieveUser(id);
        if (appUser.getAppUserRole().equals(AppUserRole.LIBRARIAN)) {
            throw new IllegalStateException("User with id " + id + " is a librarian.");
        }
        if (appUser.getBorrowedBooks().size() != 0) {
            throw new IllegalStateException(
                "Member with id " + id + " has borrowed some books. Books need to be returned first"
            );
        }
        List<Jwt> jwtList = this.jwtRepository.findJwtsByUsername(appUser.getUsername());
        if (jwtList.size() != 0) {
            this.jwtRepository.deleteAll(jwtList);
        }
        this.appUserRepository.deleteById(id);
        return "Member with id " + id + " has been deleted successfully";
    }

    @Transactional
    public String deleteSelf(String token) throws NotFoundException {
        AppUser currentUser = this.retrieveLoggedInUser();
        if (currentUser.getBorrowedBooks().size() != 0) {
            throw new IllegalStateException(
                    "User " + currentUser.getUsername() + " has borrowed some books. Books need to be returned first"
            );
        }
        this.jwtRepository.deleteById(token);
        this.appUserRepository.deleteById(currentUser.getId());
        return currentUser.getUsername() + "\'s account has been deleted";
    }

    @Transactional
    public AppUser updateMember(String id, UpdateMemberRequest request) throws NotFoundException {
        AppUser appUser = this.retrieveUser(id);
        if (appUser.getAppUserRole().equals(AppUserRole.LIBRARIAN)) {
            throw new IllegalStateException("User with id " + id + " is a librarian.");
        }
        if (request.getMembershipLevel() != null) {
            if (request.getMembershipLevel() <= 0) {
                throw new NumberFormatException("Membership Level should be at least 1");
            }
            if (appUser.getBorrowedBooks().size() > request.getMembershipLevel()) {
                throw new IllegalStateException(
                        "User with id " + id + " currently has " +
                        appUser.getBorrowedBooks().size() + " books and cannot be given a membership level of "
                        + request.getMembershipLevel()
                );
            }
            appUser.setMembershipLevel(request.getMembershipLevel());
        }
        if (!Strings.isNullOrEmpty(request.getFirstName())) {
            appUser.setFirstName(request.getFirstName());
        }
        if (!Strings.isNullOrEmpty(request.getLastName())) {
            appUser.setLastName(request.getLastName());
        }
        this.appUserRepository.save(appUser);
        return appUser;
    }

    public List<AppUser> findAllUsers() {
        return this.appUserRepository.findAll();
    }

    private String retrieveLoggedInUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails)principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    public AppUser retrieveUser(String id) throws NotFoundException {
        Optional<AppUser> optionalAppUser = Optional.ofNullable(this.appUserRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id " + id + " cannot be found")));
        return optionalAppUser.get();
    }

    public AppUser retrieveLoggedInUser() throws NotFoundException {
        String username = this.retrieveLoggedInUsername();
        Optional<AppUser> optionalAppUser = Optional.ofNullable(this.appUserRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User " + username + " cannot be found")));
        return optionalAppUser.get();
    }
}
