package posmy.interview.boot.book.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import posmy.interview.boot.book.BookStatus;
import posmy.interview.boot.book.entity.Book;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(
        properties = {
                "spring.jpa.properties.javax.persistence.validation.mode=none"
        }
)
class BookRepositoryTest {
    @Autowired
    private BookRepository bookRepository;

    @Test
    void itShouldBeAbleToSaveBookAndFindByIsbnAndId() {
        String isbn = "Sample ISBN";
        Book book = new Book("Sample Book", "Sample Author", BookStatus.AVAILABLE, 100, isbn, null);
        this.bookRepository.save(book);
        Optional<Book> optionalBookByUsername = this.bookRepository.findByIsbn(isbn);
        assertThat(optionalBookByUsername)
                .isPresent()
                .hasValueSatisfying(u-> {
                    assertThat(u).usingRecursiveComparison().isEqualTo(book);
                });
        Optional<Book> optionalBookById = this.bookRepository.findById(optionalBookByUsername.get().getId());
        assertThat(optionalBookById)
                .isPresent()
                .hasValueSatisfying(u-> {
                    assertThat(u).usingRecursiveComparison().isEqualTo(book);
                });
    }

    @Test
    void itShouldFindBooksByStatus() {
        Book availableBook = new Book("Available Book", "Sample Author", BookStatus.AVAILABLE, 100, "XX", null);
        Book discontinuedBook = new Book("Discontinued Book", "Sample Author", BookStatus.DISCONTINUED, 100, "YY", null);
        this.bookRepository.save(availableBook);
        this.bookRepository.save(discontinuedBook);
        List<Book> books = this.bookRepository.getBooksByStatus(BookStatus.AVAILABLE.name());
        assertThat(books).contains(availableBook).doesNotContain(discontinuedBook);
    }

    @Test
    void itShouldFindBooksButExcludeStatus() {
        Book availableBook = new Book("Available Book", "Sample Author", BookStatus.AVAILABLE, 100, "XX", null);
        Book discontinuedBook = new Book("Discontinued Book", "Sample Author", BookStatus.DISCONTINUED, 100, "YY", null);
        this.bookRepository.save(availableBook);
        this.bookRepository.save(discontinuedBook);
        List<Book> books = this.bookRepository.getBooksButExcludeStatus(BookStatus.DISCONTINUED.name());
        assertThat(books).contains(availableBook).doesNotContain(discontinuedBook);
    }
}