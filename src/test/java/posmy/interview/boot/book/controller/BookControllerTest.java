package posmy.interview.boot.book.controller;

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
import posmy.interview.boot.book.BookStatus;
import posmy.interview.boot.book.dto.NewBookRequest;
import posmy.interview.boot.book.dto.UpdateBookRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookControllerTest {

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
    void itShouldGetBook() throws Exception {
        ResultActions resultActions = this.mockMvc.perform(get("/api/v1/books/findone/f6b00e38-9451-4e8f-bfd2-1258105a6ed1"));
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("The Fault in Our Stars"));
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN"})
    void itShouldNotFindBook() throws Exception {
        ResultActions resultActions = this.mockMvc.perform(get("/api/v1/books/findone/notvalidbookid"));
        String error = resultActions.andExpect(status().isNotFound()).andReturn().getResolvedException().getMessage();
        assertThat(error).contains("Book with id notvalidbookid cannot be found");
    }

    @Test
    @WithMockUser(roles = {"MEMBER"})
    void itShouldFindAllBooks() throws Exception {
        ResultActions resultActions = this.mockMvc.perform(get("/api/v1/books"));
        resultActions.andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"MEMBER"})
    void itShouldFindAvailableBooks() throws Exception {
        ResultActions resultActions = this.mockMvc.perform(get("/api/v1/books/available"));
        resultActions.andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"MEMBER"})
    void itShouldFindExistingBooks() throws Exception {
        ResultActions resultActions = this.mockMvc.perform(get("/api/v1/books/existing"));
        resultActions.andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"MEMBER"})
    void itShouldFailToAddBooksIfMember() throws Exception {
        NewBookRequest book = new NewBookRequest("Divergent", "Veronica Roth", 300, "1234");
        ResultActions resultActions = this.mockMvc.perform(post("/api/v1/books/addbook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectToJson(book)));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN"})
    void itShouldAddBooks() throws Exception {
        NewBookRequest book = new NewBookRequest("Divergent", "Veronica Roth", 300, "1234");
        ResultActions resultActions = this.mockMvc.perform(post("/api/v1/books/addbook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectToJson(book)));
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Divergent"));
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN"})
    void itShouldCatchNumberFormatExceptionForAddBooks() throws Exception {
        Integer forbiddenNumberOfPages = 0;
        NewBookRequest book = new NewBookRequest("Divergent", "Veronica Roth", forbiddenNumberOfPages, "1234");
        ResultActions resultActions = this.mockMvc.perform(post("/api/v1/books/addbook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectToJson(book)));
        String error = resultActions.andExpect(status().isConflict()).andReturn().getResolvedException().getMessage();
        assertThat(error).contains("Book cannot have 0 or negative number of pages");
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN"})
    void itShouldCatchIllegalStateExceptionForAddBooks() throws Exception {
        String existingIsbn = "978-0-141-35367-8";
        NewBookRequest book = new NewBookRequest("Divergent", "Veronica Roth", 100, existingIsbn);
        ResultActions resultActions = this.mockMvc.perform(post("/api/v1/books/addbook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectToJson(book)));
        String error = resultActions.andExpect(status().isConflict()).andReturn().getResolvedException().getMessage();
        assertThat(error).contains("Book with ISBN: " + existingIsbn + " exists!");
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN"})
    void itShouldCatchNullPointerExceptionForAddBooks() throws Exception {
        String author = null;
        NewBookRequest book = new NewBookRequest("Divergent", author, 100, "XXXX");
        ResultActions resultActions = this.mockMvc.perform(post("/api/v1/books/addbook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectToJson(book)));
        String error = resultActions.andExpect(status().isInternalServerError()).andReturn().getResolvedException().getMessage();
        assertThat(error).contains("Book must have a title, author, ISBN and number of pages");
    }

    @Test
    @WithMockUser(roles = {"MEMBER"})
    void itShouldFailToUpdateBookIfMember() throws Exception {
        String bookId = "f6b00e38-9451-4e8f-bfd2-1258105a6e10";
        UpdateBookRequest book = new UpdateBookRequest("Divergent", "Veronica Roth", 300, "1234");
        ResultActions resultActions = this.mockMvc.perform(put("/api/v1/books/update/" + bookId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectToJson(book)));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN"})
    void itShouldCatchNotFoundExceptionForUpdateBook() throws Exception {
        String bookId = "f6b00e38-9451-4e8f-bfd2-1258105a6341";
        UpdateBookRequest book = new UpdateBookRequest("Divergent", "Veronica Roth", 300, "1234");
        ResultActions resultActions = this.mockMvc.perform(put("/api/v1/books/update/" + bookId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectToJson(book)));
        String error = resultActions.andExpect(status().isNotFound()).andReturn().getResolvedException().getMessage();
        assertThat(error).contains("Book with id " + bookId + " cannot be found");
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN"})
    void itShouldCatchNumberFormatExceptionForUpdateBook() throws Exception {
        Integer forbiddenNumberOfPages = 0;
        String bookId = "f6b00e38-9451-4e8f-bfd2-1258105a6e10";
        UpdateBookRequest book = new UpdateBookRequest("Divergent", "Veronica Roth", forbiddenNumberOfPages, "1234");
        ResultActions resultActions = this.mockMvc.perform(put("/api/v1/books/update/" + bookId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectToJson(book)));
        String error = resultActions.andExpect(status().isConflict()).andReturn().getResolvedException().getMessage();
        assertThat(error).contains("Book cannot have 0 or negative number of pages");
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN"})
    void itShouldCatchIllegalStateExceptionForUpdateBook() throws Exception {
        String existingIsbn = "978-0-141-35367-8";
        String bookId = "f6b00e38-9451-4e8f-bfd2-1258105a6e10";
        UpdateBookRequest book = new UpdateBookRequest("Divergent", "Veronica Roth", 100, existingIsbn);
        ResultActions resultActions = this.mockMvc.perform(put("/api/v1/books/update/" + bookId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectToJson(book)));
        String error = resultActions.andExpect(status().isConflict()).andReturn().getResolvedException().getMessage();
        assertThat(error).contains("ISBN of " + existingIsbn + " already exists!");
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN"})
    void itShouldUpdateBook() throws Exception {
        String bookId = "f6b00e38-9451-4e8f-bfd2-1258105a6e10";
        UpdateBookRequest book = new UpdateBookRequest("Divergent", "Veronica Roth", 300, "1234");
        ResultActions resultActions = this.mockMvc.perform(put("/api/v1/books/update/" + bookId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectToJson(book)));
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Divergent"));
    }

    @Test
    @WithMockUser(roles = {"MEMBER"})
    void itShouldFailToDiscontinueBookIfMember() throws Exception {
        String bookId = "f6b00e38-9451-4e8f-bfd2-1258105a6e10";
        ResultActions resultActions = this.mockMvc.perform(put("/api/v1/books/discontinue/" + bookId));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN"})
    void itShouldCatchNotFoundExceptionForDiscontinueBook() throws Exception {
        String bookId = "f6b00e38-9451-4e8f-bfd2-1258105a6eee";
        ResultActions resultActions = this.mockMvc.perform(put("/api/v1/books/discontinue/" + bookId));
        String error = resultActions.andExpect(status().isNotFound()).andReturn().getResolvedException().getMessage();
        assertThat(error).contains("Book with id " + bookId + " cannot be found");
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN"})
    void itShouldIllegalStateExceptionForDiscontinueBook() throws Exception {
        String bookIdForBookThatIsNotAvailable = "f6b00e38-9451-4e8f-bfd2-1258105a6ed7";
        ResultActions resultActions = this.mockMvc.perform(put("/api/v1/books/discontinue/" + bookIdForBookThatIsNotAvailable));
        String error = resultActions.andExpect(status().isConflict()).andReturn().getResolvedException().getMessage();
        assertThat(error).contains("Book with id " + bookIdForBookThatIsNotAvailable + " is currently being borrowed or has discontinued.");
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN"})
    void itShouldDiscontinueBook() throws Exception {
        String bookIdForBookThatIsAvailable = "f6b00e38-9451-4e8f-bfd2-1258105a6e12";
        ResultActions resultActions = this.mockMvc.perform(put("/api/v1/books/discontinue/" + bookIdForBookThatIsAvailable));
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("The Death Cure"))
                .andExpect(jsonPath("$.bookStatus").value(BookStatus.DISCONTINUED.name()));
    }

    @Test
    @WithMockUser(roles = {"MEMBER"})
    void itShouldFailToDeleteBookIfMember() throws Exception {
        String bookId = "f6b00e38-9451-4e8f-bfd2-1258105a6e10";
        ResultActions resultActions = this.mockMvc.perform(delete("/api/v1/books/delete/" + bookId));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN"})
    void itShouldCatchNotFoundExceptionForDeleteBook() throws Exception {
        String bookId = "f6b00e38-9451-4e8f-bfd2-1258105a6eee";
        ResultActions resultActions = this.mockMvc.perform(delete("/api/v1/books/delete/" + bookId));
        String error = resultActions.andExpect(status().isNotFound()).andReturn().getResolvedException().getMessage();
        assertThat(error).contains("Book with id " + bookId + " cannot be found");
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN"})
    void itShouldIllegalStateExceptionForDeleteBook() throws Exception {
        String bookIdForBookThatIsBorrowed = "f6b00e38-9451-4e8f-bfd2-1258105a6ed1";
        ResultActions resultActions = this.mockMvc.perform(delete("/api/v1/books/delete/" + bookIdForBookThatIsBorrowed));
        String error = resultActions.andExpect(status().isConflict()).andReturn().getResolvedException().getMessage();
        assertThat(error).contains("Book with id " + bookIdForBookThatIsBorrowed + " is currently being borrowed.");
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN"})
    void itShouldDeleteBook() throws Exception {
        String bookIdForBookThatIsAvailable = "f6b00e38-9451-4e8f-bfd2-1258105a6e10";
        ResultActions resultActions = this.mockMvc.perform(delete("/api/v1/books/delete/" + bookIdForBookThatIsAvailable));
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Book with id " + bookIdForBookThatIsAvailable + " has been deleted"));
    }

    @Test
    @WithMockUser(username = "captainamerica", roles = {"MEMBER"})
    void itShouldBorrowBook() throws Exception {
        String availableBookId = "f6b00e38-9451-4e8f-bfd2-1258105a6e11";
        ResultActions resultActions = this.mockMvc.perform(put("/api/v1/books/borrow/" + availableBookId));
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("The Scorch Trials"))
                .andExpect(jsonPath("$.bookStatus").value(BookStatus.BORROWED.name()));
    }

    @Test
    @WithMockUser(username = "ironman", roles = {"LIBRARIAN"})
    void itShouldPreventBorrowBookIfLibrarian() throws Exception {
        String availableBookId = "f6b00e38-9451-4e8f-bfd2-1258105a6e11";
        ResultActions resultActions = this.mockMvc.perform(put("/api/v1/books/borrow/" + availableBookId));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "captainamerica", roles = {"MEMBER"})
    void itShouldThrowNotFoundExceptionForBorrowBook() throws Exception {
        String nonExistentId = "f6b00e38-9451-4e8f-bfd2-1258105a6eee";
        ResultActions resultActions = this.mockMvc.perform(put("/api/v1/books/borrow/" + nonExistentId));
        String error = resultActions.andExpect(status().isNotFound()).andReturn().getResolvedException().getMessage();
        assertThat(error).contains("Book with id " + nonExistentId + " cannot be found");
    }

    @Test
    @WithMockUser(username = "captainamerica", roles = {"MEMBER"})
    void itShouldThrowIllegalStateExceptionForBorrowingNotAvailableBook() throws Exception {
        String discontinuedBookId = "f6b00e38-9451-4e8f-bfd2-1258105a6ed6";
        ResultActions resultActions = this.mockMvc.perform(put("/api/v1/books/borrow/" + discontinuedBookId));
        String error = resultActions.andExpect(status().isConflict()).andReturn().getResolvedException().getMessage();
        assertThat(error).contains("Book with id " + discontinuedBookId + " is currently being borrowed or has discontinued.");
    }

    @Test
    @WithMockUser(username = "shazam", roles = {"MEMBER"})
    void itShouldThrowIllegalStateExceptionForBorrowingMoreThanAllowed() throws Exception {
        String availableBookChamberOfSecretsId = "f6b00e38-9451-4e8f-bfd2-1258105a6e14";
        ResultActions resultActions = this.mockMvc.perform(put("/api/v1/books/borrow/" + availableBookChamberOfSecretsId));
        String error = resultActions.andExpect(status().isConflict()).andReturn().getResolvedException().getMessage();
        assertThat(error).contains("User shazam has borrowed the maximum allowable number of books");
    }

    @Test
    @WithMockUser(username = "thor", roles = {"MEMBER"})
    void itShouldReturnBook() throws Exception {
        String thorBorrowedBookNorthernLightsId = "f6b00e38-9451-4e8f-bfd2-1258105a6e23";
        ResultActions resultActions = this.mockMvc.perform(put("/api/v1/books/return/" + thorBorrowedBookNorthernLightsId));
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Northern Lights"))
                .andExpect(jsonPath("$.bookStatus").value(BookStatus.AVAILABLE.name()));
    }

    @Test
    @WithMockUser(username = "ironman", roles = {"LIBRARIAN"})
    void itShouldPreventReturnBookIfLibrarian() throws Exception {
        String availableBookId = "f6b00e38-9451-4e8f-bfd2-1258105a6e11";
        ResultActions resultActions = this.mockMvc.perform(put("/api/v1/books/return/" + availableBookId));
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "thor", roles = {"MEMBER"})
    void itShouldThrowNotFoundExceptionForReturnBook() throws Exception {
        String nonExistentId = "f6b00e38-9451-4e8f-bfd2-1258105a6eee";
        ResultActions resultActions = this.mockMvc.perform(put("/api/v1/books/return/" + nonExistentId));
        String error = resultActions.andExpect(status().isNotFound()).andReturn().getResolvedException().getMessage();
        assertThat(error).contains("Book with id " + nonExistentId + " cannot be found");
    }

    @Test
    @WithMockUser(username = "thor", roles = {"MEMBER"})
    void itShouldThrowIllegalStateExceptionForReturningNotBorrowedBook() throws Exception {
        String discontinuedBookId = "f6b00e38-9451-4e8f-bfd2-1258105a6ed6";
        ResultActions resultActions = this.mockMvc.perform(put("/api/v1/books/return/" + discontinuedBookId));
        String error = resultActions.andExpect(status().isConflict()).andReturn().getResolvedException().getMessage();
        assertThat(error).contains("Book with id " + discontinuedBookId + " is not being borrowed by anyone");
    }

    @Test
    @WithMockUser(username = "shazam", roles = {"MEMBER"})
    void itShouldThrowIllegalStateExceptionForReturningBookNotBeingBorrowedByLoggedInUser() throws Exception {
        String thorBorrowedBookTheSubtleKnifeId = "f6b00e38-9451-4e8f-bfd2-1258105a6e24";
        ResultActions resultActions = this.mockMvc.perform(put("/api/v1/books/return/" + thorBorrowedBookTheSubtleKnifeId));
        String error = resultActions.andExpect(status().isConflict()).andReturn().getResolvedException().getMessage();
        assertThat(error).contains("User shazam is not the borrower of book with id " + thorBorrowedBookTheSubtleKnifeId);
    }
}
