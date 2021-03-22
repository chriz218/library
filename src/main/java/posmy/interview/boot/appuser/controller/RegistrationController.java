package posmy.interview.boot.appuser.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import posmy.interview.boot.appuser.dto.NewLibrarianRequest;
import posmy.interview.boot.appuser.dto.NewMemberRequest;
import posmy.interview.boot.appuser.service.AppUserService;

@RestController
@RequestMapping(path = "api/v1/registration")
@AllArgsConstructor
public class RegistrationController {
    private final AppUserService appUserService;
    
    @PostMapping(path ="/member")
    @PreAuthorize("hasRole('ROLE_LIBRARIAN')")
    public String registerMember(@RequestBody NewMemberRequest request) {
        try {
            return this.appUserService.registerMember(request);
        } catch (NullPointerException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @PostMapping(path ="/librarian")
    @PreAuthorize("hasRole('ROLE_LIBRARIAN')")
    public String registerLibrarian(@RequestBody NewLibrarianRequest request) {
        try {
            return this.appUserService.registerLibrarian(request);
        } catch (NullPointerException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }
}
