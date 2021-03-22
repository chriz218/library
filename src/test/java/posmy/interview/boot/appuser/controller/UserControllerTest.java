package posmy.interview.boot.appuser.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import posmy.interview.boot.appuser.dto.UpdateMemberRequest;
import posmy.interview.boot.appuser.dto.UpdateUserPasswordRequest;
import posmy.interview.boot.security.jwt.config.JwtConfig;
import posmy.interview.boot.security.jwt.entity.Jwt;
import posmy.interview.boot.security.jwt.repository.JwtRepository;

import javax.crypto.SecretKey;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtRepository jwtRepository;

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private SecretKey secretKey;

    private String objectToJson(Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            fail("Failed to convert object to json.");
            return null;
        }
    }

    private String prepareToken(String role, String username) {
        Map<String, String> authority = new HashMap<>();
        authority.put("authority", "ROLE_" + role);
        List<Map<String, String>> listOfAuthorities = new ArrayList<>();
        listOfAuthorities.add(authority);
        String jwt = Jwts.builder()
                .setSubject(username)
                .claim("authorities", listOfAuthorities)
                .setIssuedAt(new Date())
                .setExpiration(java.sql.Date.valueOf(LocalDate.now().plusDays(this.jwtConfig.getTokenExpirationAfterDays())))
                .signWith(this.secretKey)
                .compact();
        this.jwtRepository.save(new Jwt(jwt, username));
        return jwt;
    }

    @Test
    @WithMockUser(username = "captainamerica", roles = {"MEMBER"})
    void itShouldGetCurrentProfile() throws Exception {
        ResultActions resultActions = this.mockMvc.perform(get("/api/v1/users/me"));
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("captainamerica"));
    }

    @Test
    @WithMockUser(username = "unknownuser", roles = {"MEMBER"})
    void itShouldNotGetCurrentProfile() throws Exception {
        ResultActions resultActions = this.mockMvc.perform(get("/api/v1/users/me"));
        String error = resultActions.andExpect(status().isNotFound()).andReturn().getResolvedException().getMessage();
        assertThat(error).contains("User unknownuser cannot be found");
    }

    @Test
    @WithMockUser(username = "captainamerica", roles = {"MEMBER"})
    void itShouldChangePassword() throws Exception {
        UpdateUserPasswordRequest request = new UpdateUserPasswordRequest("12345678", "12345678");
        ResultActions resultActions = this.mockMvc.perform(put("/api/v1/users/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectToJson(request)));
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$").value("User captainamerica has successfully changed password"));
    }

    @Test
    @WithMockUser(username = "captainamerica", roles = {"MEMBER"})
    void itShouldThrowNullPointerExceptionWhileChangingPassword() throws Exception {
        UpdateUserPasswordRequest request = new UpdateUserPasswordRequest(null, null);
        ResultActions resultActions = this.mockMvc.perform(put("/api/v1/users/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectToJson(request)));
        String error = resultActions.andExpect(status().isInternalServerError()).andReturn().getResolvedException().getMessage();
        assertThat(error).contains("Please enter current password and new password");
    }

    @Test
    @WithMockUser(username = "unknownuser", roles = {"MEMBER"})
    void itShouldThrowNotFoundExceptionWhileChangingPassword() throws Exception {
        UpdateUserPasswordRequest request = new UpdateUserPasswordRequest("12345678", "12345678");
        ResultActions resultActions = this.mockMvc.perform(put("/api/v1/users/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectToJson(request)));
        String error = resultActions.andExpect(status().isNotFound()).andReturn().getResolvedException().getMessage();
        assertThat(error).contains("User unknownuser cannot be found");
    }

    @Test
    @WithMockUser(username = "captainamerica", roles = {"MEMBER"})
    void itShouldThrowIllegalStateExceptionWhileChangingPassword() throws Exception {
        UpdateUserPasswordRequest request = new UpdateUserPasswordRequest("87654321", "12345678");
        ResultActions resultActions = this.mockMvc.perform(put("/api/v1/users/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectToJson(request)));
        String error = resultActions.andExpect(status().isForbidden()).andReturn().getResolvedException().getMessage();
        assertThat(error).contains("Wrong Password");;
    }

    @Test
    @WithMockUser(roles = {"MEMBER"})
    void itShouldNotFindAllUsersBecauseOfMemberRole() throws Exception {
        ResultActions resultActions = this.mockMvc.perform(get("/api/v1/users"));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN"})
    void itShouldFindAllUsers() throws Exception {
        ResultActions resultActions = this.mockMvc.perform(get("/api/v1/users"));
        resultActions.andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"MEMBER"})
    void itShouldNotFindUserBecauseOfMemberRole() throws Exception {
        String ironmanId = "a25df130-aaed-4ba8-9aee-a7062d662da0";
        ResultActions resultActions = this.mockMvc.perform(get("/api/v1/users/" + ironmanId));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN"})
    void itShouldFindUser() throws Exception {
        String ironmanId = "a25df130-aaed-4ba8-9aee-a7062d662da0";
        ResultActions resultActions = this.mockMvc.perform(get("/api/v1/users/" + ironmanId));
        resultActions.andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN"})
    void itShouldThrowNotFoundExceptionFindUser() throws Exception {
        String unknownId = "unknownId";
        ResultActions resultActions = this.mockMvc.perform(get("/api/v1/users/" + unknownId));
        String error = resultActions.andExpect(status().isNotFound()).andReturn().getResolvedException().getMessage();
        assertThat(error).contains("User with id " + unknownId + " cannot be found");
    }

    @Test
    @WithMockUser(roles = {"MEMBER"})
    void itShouldNotDeleteMemberBecauseOfMemberRole() throws Exception {
        String starlordId = "a25df130-aaed-4ba8-9aee-a7062d662d11";
        ResultActions resultActions = this.mockMvc.perform(delete("/api/v1/users/delete/" + starlordId));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN"})
    void itShouldDeleteMember() throws Exception {
        String starlordId = "a25df130-aaed-4ba8-9aee-a7062d662d11";
        ResultActions resultActions = this.mockMvc.perform(delete("/api/v1/users/delete/" + starlordId));
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Member with id " + starlordId + " has been deleted successfully"));
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN"})
    void itShouldThrowNotFoundExceptionWhenDeletingUnknownMember() throws Exception {
        String unknownId = "unknownId";
        ResultActions resultActions = this.mockMvc.perform(delete("/api/v1/users/delete/" + unknownId));
        String error = resultActions.andExpect(status().isNotFound()).andReturn().getResolvedException().getMessage();
        assertThat(error).contains("User with id " + unknownId + " cannot be found");
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN"})
    void itShouldPreventDeletionOfMembersWithBooks() throws Exception {
        String thorId = "a25df130-aaed-4ba8-9aee-a7062d662d10";
        ResultActions resultActions = this.mockMvc.perform(delete("/api/v1/users/delete/" + thorId));
        String error = resultActions.andExpect(status().isConflict()).andReturn().getResolvedException().getMessage();
        assertThat(error).contains("Member with id " + thorId + " has borrowed some books. Books need to be returned first");
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN"})
    void itShouldPreventDeletionOfLibrarians() throws Exception {
        String rescueId = "a25df130-aaed-4ba8-9aee-a7062d662da5";
        ResultActions resultActions = this.mockMvc.perform(delete("/api/v1/users/delete/" + rescueId));
        String error = resultActions.andExpect(status().isConflict()).andReturn().getResolvedException().getMessage();
        assertThat(error).contains("User with id " + rescueId + " is a librarian.");
    }

    @Test
    @WithMockUser(roles = {"MEMBER"})
    void itShouldNotUpdateMemberBecauseOfMemberRole() throws Exception {
        UpdateMemberRequest request = new UpdateMemberRequest("Rocket","Raccoon", 2);
        String rocketId = "a25df130-aaed-4ba8-9aee-a7062d662d12";
        ResultActions resultActions = this.mockMvc.perform(put("/api/v1/users/update/" + rocketId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectToJson(request)));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN"})
    void itShouldUpdateMember() throws Exception {
        UpdateMemberRequest request = new UpdateMemberRequest("Rocket","Raccoon", 2);
        String rocketId = "a25df130-aaed-4ba8-9aee-a7062d662d12";
        ResultActions resultActions = this.mockMvc.perform(put("/api/v1/users/update/" + rocketId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectToJson(request)));
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("rocket"))
                .andExpect(jsonPath("$.firstName").value("Rocket"))
                .andExpect(jsonPath("$.lastName").value("Raccoon"))
                .andExpect(jsonPath("$.membershipLevel").value(2));
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN"})
    void itShouldThrowNotFoundExceptionWhenUpdatingMember() throws Exception {
        UpdateMemberRequest request = new UpdateMemberRequest("Rocket","Raccoon", 2);
        String unknownId = "unknownId";
        ResultActions resultActions = this.mockMvc.perform(put("/api/v1/users/update/" + unknownId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectToJson(request)));
        String error = resultActions.andExpect(status().isNotFound()).andReturn().getResolvedException().getMessage();
        assertThat(error).contains("User with id " + unknownId + " cannot be found");
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN"})
    void itShouldThrowIllegalStateExceptionWhenUpdatingLibrarian() throws Exception {
        UpdateMemberRequest request = new UpdateMemberRequest("Rocket","Raccoon", 2);
        String ironmanId = "a25df130-aaed-4ba8-9aee-a7062d662da0";
        ResultActions resultActions = this.mockMvc.perform(put("/api/v1/users/update/" + ironmanId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectToJson(request)));
        String error = resultActions.andExpect(status().isConflict()).andReturn().getResolvedException().getMessage();
        assertThat(error).contains("User with id " + ironmanId + " is a librarian.");
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN"})
    void itShouldThrowNumberFormatExceptionWhenUpdatingMember() throws Exception {
        UpdateMemberRequest request = new UpdateMemberRequest("Rocket","Raccoon", 0);
        String rocketId = "a25df130-aaed-4ba8-9aee-a7062d662d12";
        ResultActions resultActions = this.mockMvc.perform(put("/api/v1/users/update/" + rocketId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectToJson(request)));
        String error = resultActions.andExpect(status().isConflict()).andReturn().getResolvedException().getMessage();
        assertThat(error).contains("Membership Level should be at least 1");
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN"})
    void itShouldThrowIllegalStateExceptionWhenReducingMembershipLevelToBelowCurrentBorrowedBooksNumber() throws Exception {
        UpdateMemberRequest request = new UpdateMemberRequest("Thor","Odinson", 1);
        String thorId = "a25df130-aaed-4ba8-9aee-a7062d662d10";
        ResultActions resultActions = this.mockMvc.perform(put("/api/v1/users/update/" + thorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectToJson(request)));
        String error = resultActions.andExpect(status().isConflict()).andReturn().getResolvedException().getMessage();
        assertThat(error).contains("cannot be given a membership level of " + request.getMembershipLevel());
    }

    @Test
    @WithMockUser(username = "rescue", roles = {"LIBRARIAN"})
    void itShouldPreventSelfDeleteIfRoleIsLibrarian() throws Exception {
        String jwt = this.prepareToken("LIBRARIAN", "rescue");
        String token = "Bearer " + jwt;
        ResultActions resultActions = this.mockMvc.perform(delete("/api/v1/users/deleteself")
                .header("Authorization", token));
        resultActions.andExpect(status().isForbidden());
        this.jwtRepository.deleteById(jwt);
    }

    @Test
    @WithMockUser(username = "wintersoldier", roles = {"MEMBER"})
    void itShouldDeleteSelfIfMemberAndHasNoBorrowedBooks() throws Exception {
        String jwt = this.prepareToken("MEMBER", "wintersoldier");
        String token = "Bearer " + jwt;
        ResultActions resultActions = this.mockMvc.perform(delete("/api/v1/users/deleteself")
                .header("Authorization", token));
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$").value("wintersoldier\'s account has been deleted"));
    }

    @Test
    @WithMockUser(username = "unknownuser", roles = {"MEMBER"})
    void itShouldThrowNotFoundExceptionWhenUnableToFindSelfWhileDeletingSelf() throws Exception {
        String jwt = this.prepareToken("MEMBER", "unknownuser");
        String token = "Bearer " + jwt;
        ResultActions resultActions = this.mockMvc.perform(delete("/api/v1/users/deleteself")
                .header("Authorization", token));
        String error = resultActions.andExpect(status().isNotFound()).andReturn().getResolvedException().getMessage();
        assertThat(error).contains("User unknownuser cannot be found");
        this.jwtRepository.deleteById(jwt);
    }

    @Test
    @WithMockUser(username = "thor", roles = {"MEMBER"})
    void itShouldThrowIllegalStateExceptionWhenTryingToDeleteSelfWhileHavingBorrowedBooks() throws Exception {
        String jwt = this.prepareToken("MEMBER", "thor");
        String token = "Bearer " + jwt;
        ResultActions resultActions = this.mockMvc.perform(delete("/api/v1/users/deleteself")
                .header("Authorization", token));
        String error = resultActions.andExpect(status().isConflict()).andReturn().getResolvedException().getMessage();
        assertThat(error).contains("User thor has borrowed some books. Books need to be returned first");
        this.jwtRepository.deleteById(jwt);
    }

}