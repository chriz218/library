package posmy.interview.boot.appuser.controller;

import javassist.NotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import posmy.interview.boot.appuser.entity.AppUser;
import posmy.interview.boot.appuser.dto.UpdateMemberRequest;
import posmy.interview.boot.appuser.dto.UpdateUserPasswordRequest;
import posmy.interview.boot.appuser.service.AppUserService;
import posmy.interview.boot.security.jwt.config.JwtConfig;

import java.util.List;

@RestController
@RequestMapping(path = "api/v1/users")
@AllArgsConstructor
public class UserController {
    private final AppUserService appUserService;
    private final JwtConfig jwtConfig;

    @GetMapping(path = "/me")
    @PreAuthorize("hasAnyRole('ROLE_LIBRARIAN', 'ROLE_MEMBER')")
    public AppUser viewMyProfile() {
        try {
            return this.appUserService.retrieveLoggedInUser();
        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PutMapping(path = "/password")
    @PreAuthorize("hasAnyRole('ROLE_LIBRARIAN', 'ROLE_MEMBER')")
    public String changePassword(@RequestBody UpdateUserPasswordRequest updateUserPasswordRequest) {
        try {
            return this.appUserService.changePassword(updateUserPasswordRequest);
        } catch (NullPointerException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_LIBRARIAN')")
    public List<AppUser> getAllUsers() {
        return this.appUserService.findAllUsers();
    }

    @GetMapping(path = "{userId}")
    @PreAuthorize("hasRole('ROLE_LIBRARIAN')")
    public AppUser getUser(@PathVariable("userId") String userId) {
        try {
            return this.appUserService.retrieveUser(userId);
        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping(path = "/delete/{memberId}")
    @PreAuthorize("hasRole('ROLE_LIBRARIAN')")
    public String deleteMember(@PathVariable("memberId") String memberId) {
        try {
            return this.appUserService.deleteMember(memberId);
        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @DeleteMapping(path = "/deleteself")
    @PreAuthorize("hasRole('ROLE_MEMBER')")
    public String deleteSelf(@RequestHeader(value = "Authorization") String bearerString) {
        try {
            String token = bearerString.replace(this.jwtConfig.getTokenPrefix(), "");
            return this.appUserService.deleteSelf(token);
        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @PutMapping(path = "/update/{memberId}")
    @PreAuthorize("hasRole('ROLE_LIBRARIAN')")
    public AppUser updateMember(@PathVariable("memberId") String memberId, @RequestBody UpdateMemberRequest request) {
        try {
            return this.appUserService.updateMember(memberId, request);
        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }
}
