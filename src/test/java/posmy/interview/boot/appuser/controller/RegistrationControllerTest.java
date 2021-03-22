package posmy.interview.boot.appuser.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import posmy.interview.boot.appuser.dto.NewLibrarianRequest;
import posmy.interview.boot.appuser.dto.NewMemberRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RegistrationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private String objectToJson(Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            fail("Failed to convert object to json.");
            return null;
        }
    }

    @Test
    @WithMockUser(roles = {"MEMBER"})
    void itShouldNotRegisterMemberBecauseOfMemberRole() throws Exception {
        NewMemberRequest request = new NewMemberRequest("newmember", "New", "Member", "12345678", 1);
        ResultActions resultActions = this.mockMvc.perform(post("/api/v1/registration/member")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectToJson(request)));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN"})
    void itShouldRegisterMember() throws Exception {
        NewMemberRequest request = new NewMemberRequest("newmember", "New", "Member", "12345678", 1);
        ResultActions resultActions = this.mockMvc.perform(post("/api/v1/registration/member")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectToJson(request)));
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$")
                .value("Successfully created member: " + request.getUsername() + "!"));
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN"})
    void itShouldThrowNullPointerExceptionDuringMemberCreation() throws Exception {
        NewMemberRequest request = new NewMemberRequest(null, "New", "Member", "12345678", 1);
        ResultActions resultActions = this.mockMvc.perform(post("/api/v1/registration/member")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectToJson(request)));
        String error = resultActions.andExpect(status().isInternalServerError()).andReturn().getResolvedException().getMessage();
        assertThat(error).contains("Member must have a username, first name, last name, password and membership level");
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN"})
    void itShouldThrowNumberFormatExceptionDuringMemberCreation() throws Exception {
        NewMemberRequest request = new NewMemberRequest("newmember", "New", "Member", "12345678", 0);
        ResultActions resultActions = this.mockMvc.perform(post("/api/v1/registration/member")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectToJson(request)));
        String error = resultActions.andExpect(status().isConflict()).andReturn().getResolvedException().getMessage();
        assertThat(error).contains("Membership Level should be at least 1");
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN"})
    void itShouldThrowIllegalStateExceptionDuringMemberCreation() throws Exception {
        NewMemberRequest request = new NewMemberRequest("ironman", "New", "Member", "12345678", 1);
        ResultActions resultActions = this.mockMvc.perform(post("/api/v1/registration/member")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectToJson(request)));
        String error = resultActions.andExpect(status().isConflict()).andReturn().getResolvedException().getMessage();
        assertThat(error).contains(request.getUsername().toLowerCase() + " is already taken");
    }

    @Test
    @WithMockUser(roles = {"MEMBER"})
    void itShouldNotRegisterLibrarianBecauseOfMemberRole() throws Exception {
        NewLibrarianRequest request = new NewLibrarianRequest("newlibrarian", "New", "Librarian", "12345678");
        ResultActions resultActions = this.mockMvc.perform(post("/api/v1/registration/librarian")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectToJson(request)));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN"})
    void itShouldRegisterLibrarian() throws Exception {
        NewLibrarianRequest request = new NewLibrarianRequest("newlibrarian", "New", "Librarian", "12345678");
        ResultActions resultActions = this.mockMvc.perform(post("/api/v1/registration/librarian")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectToJson(request)));
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$")
                .value("Successfully created librarian: " + request.getUsername() + "!"));
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN"})
    void itShouldThrowNullPointerExceptionDuringLibrarianCreation() throws Exception {
        NewLibrarianRequest request = new NewLibrarianRequest(null, "New", "Librarian", "12345678");
        ResultActions resultActions = this.mockMvc.perform(post("/api/v1/registration/librarian")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectToJson(request)));
        String error = resultActions.andExpect(status().isInternalServerError()).andReturn().getResolvedException().getMessage();
        assertThat(error).contains("Librarian must have a username, first name, last name, and password");
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN"})
    void itShouldThrowIllegalStateExceptionDuringLibrarianCreation() throws Exception {
        NewLibrarianRequest request = new NewLibrarianRequest("ironman", "New", "Librarian", "12345678");
        ResultActions resultActions = this.mockMvc.perform(post("/api/v1/registration/librarian")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectToJson(request)));
        String error = resultActions.andExpect(status().isConflict()).andReturn().getResolvedException().getMessage();
        assertThat(error).contains(request.getUsername().toLowerCase() + " is already taken");
    }
}