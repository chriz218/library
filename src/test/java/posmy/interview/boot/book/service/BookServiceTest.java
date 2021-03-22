package posmy.interview.boot.book.service;

import javassist.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import posmy.interview.boot.appuser.AppUserRole;
import posmy.interview.boot.appuser.entity.AppUser;
import posmy.interview.boot.appuser.service.AppUserService;
import posmy.interview.boot.book.BookStatus;
import posmy.interview.boot.book.dto.NewBookRequest;
import posmy.interview.boot.book.dto.UpdateBookRequest;
import posmy.interview.boot.book.entity.Book;
import posmy.interview.boot.book.repository.BookRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

class BookServiceTest {
    @Mock
    private BookRepository bookRepository;
    @Mock
    private AppUserService appUserService;
    @Captor
    private ArgumentCaptor<Book> bookArgumentCaptor;
    private BookService bookService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.bookService = new BookService(bookRepository, appUserService);
    }

    @Test
    void itShouldThrowNullPointerExceptionWhenAddingBook() {
        NewBookRequest request = new NewBookRequest("Sample Book", "Sample Author", null, "XX");
        assertThatThrownBy(() -> this.bookService.addBook(request))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Book must have a title, author, ISBN and number of pages");
        then(this.bookRepository).should(never()).save(any(Book.class));
    }

    @Test
    void itShouldThrowNumberFormatExceptionWhenAddingBook() {
        NewBookRequest request = new NewBookRequest("Sample Book", "Sample Author", 0, "XX");
        assertThatThrownBy(() -> this.bookService.addBook(request))
                .isInstanceOf(NumberFormatException.class)
                .hasMessageContaining("Book cannot have 0 or negative number of pages");
        then(this.bookRepository).should(never()).save(any(Book.class));
    }

    @Test
    void itShouldThrowIllegalStateExceptionWhenAddingBook() {
        String isbn = "XX";
        NewBookRequest request = new NewBookRequest("Sample Book", "Sample Author", 100, isbn);
        Book book = new Book(UUID.randomUUID().toString(), "Sample Book", "Sample Author", BookStatus.AVAILABLE, 100, isbn, null);
        given(this.bookRepository.findByIsbn(isbn)).willReturn(Optional.of(book));
        assertThatThrownBy(() -> this.bookService.addBook(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Book with ISBN: " + request.getIsbn() + " exists!");
        then(this.bookRepository).should(never()).save(any(Book.class));
    }

    @Test
    void itShouldAddBook() {
        String isbn = "XX";
        NewBookRequest request = new NewBookRequest("Sample Book", "Sample Author", 100, isbn);
        Book book = new Book(UUID.randomUUID().toString(), "Sample Book", "Sample Author", BookStatus.AVAILABLE, 100, isbn, null);
        given(this.bookRepository.findByIsbn(isbn)).willReturn(Optional.empty());
        this.bookService.addBook(request);
        then(this.bookRepository).should().save(this.bookArgumentCaptor.capture());
        assertThat(this.bookArgumentCaptor.getValue())
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(book);
    }

    @Test
    void itShouldFindAllBooks() {
        Book availableBook = new Book(UUID.randomUUID().toString(), "Available Book", "Sample Author", BookStatus.AVAILABLE, 100, "XX", null);
        Book discontinuedBook = new Book(UUID.randomUUID().toString(), "Discontinued Book", "Sample Author", BookStatus.DISCONTINUED, 100, "YY", null);
        List<Book> books = new ArrayList<>();
        books.add(availableBook);
        books.add(discontinuedBook);
        given(this.bookRepository.findAll()).willReturn(books);
        assertThat(this.bookService.findAllBooks()).contains(availableBook, discontinuedBook);
    }

    @Test
    void itShouldFindAvailableBooks() {
        Book availableBook = new Book(UUID.randomUUID().toString(), "Available Book", "Sample Author", BookStatus.AVAILABLE, 100, "XX", null);
        Book discontinuedBook = new Book(UUID.randomUUID().toString(), "Discontinued Book", "Sample Author", BookStatus.DISCONTINUED, 100, "YY", null);
        List<Book> books = new ArrayList<>();
        books.add(availableBook);
        books.add(discontinuedBook);
        given(this.bookRepository.getBooksByStatus(BookStatus.AVAILABLE.name())).willReturn(books.parallelStream().filter(c -> c.getBookStatus().equals(BookStatus.AVAILABLE)).collect(Collectors.toList()));
        assertThat(this.bookService.findAvailableBooks()).contains(availableBook).doesNotContainSequence(discontinuedBook);
    }

    @Test
    void itShouldFindExistingBooks() {
        Book availableBook = new Book(UUID.randomUUID().toString(), "Available Book", "Sample Author", BookStatus.AVAILABLE, 100, "XX", null);
        Book discontinuedBook = new Book(UUID.randomUUID().toString(), "Discontinued Book", "Sample Author", BookStatus.DISCONTINUED, 100, "YY", null);
        List<Book> books = new ArrayList<>();
        books.add(availableBook);
        books.add(discontinuedBook);
        given(this.bookRepository.getBooksButExcludeStatus(BookStatus.DISCONTINUED.name())).willReturn(books.parallelStream().filter(c -> !c.getBookStatus().equals(BookStatus.DISCONTINUED)).collect(Collectors.toList()));
        assertThat(this.bookService.findExistingBooks()).contains(availableBook).doesNotContainSequence(discontinuedBook);
    }

    @Test
    void itShouldThrowNotFoundExceptionWhenFindingBook() {
        String id = "XXX";
        given(this.bookRepository.findById(anyString())).willReturn(Optional.empty());
        assertThatThrownBy(() -> this.bookService.findBook(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Book with id " + id + " cannot be found");
    }

    @Test
    void itShouldFindBook() throws NotFoundException {
        String id = UUID.randomUUID().toString();
        Book availableBook = new Book(id, "Available Book", "Sample Author", BookStatus.AVAILABLE, 100, "XX", null);
        given(this.bookRepository.findById(id)).willReturn(Optional.of(availableBook));
        assertThat(this.bookService.findBook(id)).usingRecursiveComparison().isEqualTo(availableBook);
    }

    @Test
    void itShouldThrowNotFoundExceptionWhenUpdatingBook() {
        String id = UUID.randomUUID().toString();
        given(this.bookRepository.findById(anyString())).willReturn(Optional.empty());
        UpdateBookRequest updateBookRequest = new UpdateBookRequest("Sample Title", "Sample Author", 100, "XX");
        assertThatThrownBy(() -> this.bookService.updateBook(id, updateBookRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Book with id " + id + " cannot be found");
        then(this.bookRepository).should(never()).save(any(Book.class));
    }

    @Test
    void itShouldThrowNumberFormatExceptionWhenUpdatingBook() {
        String id = UUID.randomUUID().toString();
        Book book = new Book(id, "Available Book", "Sample Author", BookStatus.AVAILABLE, 100, "XX", null);
        given(this.bookRepository.findById(id)).willReturn(Optional.of(book));
        UpdateBookRequest updateBookRequest = new UpdateBookRequest("Sample Title", "Sample Author", 0, "KK");
        assertThatThrownBy(() -> this.bookService.updateBook(id, updateBookRequest))
                .isInstanceOf(NumberFormatException.class)
                .hasMessageContaining("Book cannot have 0 or negative number of pages");
        then(this.bookRepository).should(never()).save(any(Book.class));
    }

    @Test
    void itShouldThrowIllegalStateExceptionWhenUpdatingBook() {
        String id = UUID.randomUUID().toString();
        Book existingBook = new Book(id, "Existing Book", "Sample Author", BookStatus.AVAILABLE, 100, "YY", null);
        Book book = new Book(id, "Available Book", "Sample Author", BookStatus.AVAILABLE, 100, "XX", null);
        given(this.bookRepository.findById(id)).willReturn(Optional.of(book));
        UpdateBookRequest updateBookRequest = new UpdateBookRequest("Sample Title", "Sample Author", 200, "YY");
        given(this.bookRepository.findByIsbn(updateBookRequest.getIsbn())).willReturn(Optional.of(existingBook));
        assertThatThrownBy(() -> this.bookService.updateBook(id, updateBookRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ISBN of " + updateBookRequest.getIsbn() + " already exists!");
        then(this.bookRepository).should(never()).save(any(Book.class));
    }

    @Test
    void itShouldUpdateBook() throws NotFoundException {
        String id = UUID.randomUUID().toString();
        Book book = new Book(id, "Available Book", "Sample Author", BookStatus.AVAILABLE, 100, "XX", null);
        given(this.bookRepository.findById(id)).willReturn(Optional.of(book));
        UpdateBookRequest updateBookRequest = new UpdateBookRequest("Sample Title", "Sample Author", 200, "YY");
        given(this.bookRepository.findByIsbn(updateBookRequest.getIsbn())).willReturn(Optional.empty());
        this.bookService.updateBook(id, updateBookRequest);
        then(this.bookRepository).should().save(bookArgumentCaptor.capture());
        Book updatedBook = new Book(id,"Sample Title", "Sample Author", BookStatus.AVAILABLE, 200, "YY", null);
        assertThat(this.bookArgumentCaptor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(updatedBook);
    }

    @Test
    void itShouldThrowNotFoundExceptionWhenDiscontinuingBook() {
        String id = UUID.randomUUID().toString();
        given(this.bookRepository.findById(id)).willReturn(Optional.empty());
        assertThatThrownBy(() -> this.bookService.discontinueBook(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Book with id " + id + " cannot be found");
        then(this.bookRepository).should(never()).save(any(Book.class));
    }

    @Test
    void itShouldThrowIllegalStateExceptionWhenDiscontinuingBook() {
        String id = UUID.randomUUID().toString();
        Book discontinuedBook = new Book(id, "Discontinued Book", "Sample Author", BookStatus.DISCONTINUED, 100, "XX", null);
        given(this.bookRepository.findById(id)).willReturn(Optional.of(discontinuedBook));
        assertThatThrownBy(() -> this.bookService.discontinueBook(id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Book with id " + id + " is currently being borrowed or has discontinued.");
        then(this.bookRepository).should(never()).save(any(Book.class));
    }

    @Test
    void itShouldDiscontinueBook() throws NotFoundException {
        String id = UUID.randomUUID().toString();
        Book availableBook = new Book(id, "Sample Title", "Sample Author", BookStatus.AVAILABLE, 100, "XX", null);
        given(this.bookRepository.findById(id)).willReturn(Optional.of(availableBook));
        this.bookService.discontinueBook(id);
        Book updatedBook = new Book(id,"Sample Title", "Sample Author", BookStatus.DISCONTINUED, 100, "XX", null);
        then(this.bookRepository).should().save(bookArgumentCaptor.capture());
        assertThat(this.bookArgumentCaptor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(updatedBook);
    }

    @Test
    void itShouldThrowNotFoundExceptionWhenDeletingBook() {
        String id = UUID.randomUUID().toString();
        given(this.bookRepository.findById(id)).willReturn(Optional.empty());
        assertThatThrownBy(() -> this.bookService.deleteBook(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Book with id " + id + " cannot be found");
        then(this.bookRepository).should(never()).deleteById(id);
    }

    @Test
    void itShouldThrowIllegalStateExceptionWhenDeletingBook() {
        String bookId = UUID.randomUUID().toString();
        String memberId = UUID.randomUUID().toString();
        AppUser member = new AppUser(memberId, "johnwang", "John", "Wang", "12345678", AppUserRole.MEMBER, 5);
        Book borrowedBook = new Book(bookId, "Borrowed Book", "Sample Author", BookStatus.BORROWED, 100, "XX", member);
        given(this.bookRepository.findById(bookId)).willReturn(Optional.of(borrowedBook));
        assertThatThrownBy(() -> this.bookService.deleteBook(bookId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Book with id " + bookId + " is currently being borrowed.");
        then(this.bookRepository).should(never()).deleteById(bookId);
    }

    @Test
    void itShouldDeleteBook() throws NotFoundException {
        String id = UUID.randomUUID().toString();
        Book availableBook = new Book(id, "Sample Title", "Sample Author", BookStatus.AVAILABLE, 100, "XX", null);
        given(this.bookRepository.findById(id)).willReturn(Optional.of(availableBook));
        assertThat(this.bookService.deleteBook(id)).isEqualTo("Book with id " + id + " has been deleted");
        then(this.bookRepository).should().deleteById(id);
    }

    @Test
    void itShouldThrowNotFoundExceptionWhenBorrowingBook() {
        String id = UUID.randomUUID().toString();
        given(this.bookRepository.findById(id)).willReturn(Optional.empty());
        assertThatThrownBy(() -> this.bookService.borrowBook(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Book with id " + id + " cannot be found");
        then(this.bookRepository).should(never()).save(any(Book.class));
    }

    @Test
    void itShouldThrowIllegalStateExceptionWhenBorrowingBookThatIsNotAvailable() {
        String id = UUID.randomUUID().toString();
        Book discontinuedBook = new Book(id, "Discontinued Book", "Sample Author", BookStatus.DISCONTINUED, 100, "XX", null);
        given(this.bookRepository.findById(id)).willReturn(Optional.of(discontinuedBook));
        assertThatThrownBy(() -> this.bookService.borrowBook(id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Book with id " + id + " is currently being borrowed or has discontinued.");
        then(this.bookRepository).should(never()).save(any(Book.class));
    }

    @Test
    void itShouldThrowIllegalStateExceptionWhenBorrowingBookWithMaximumAllowableNumberReached() throws NotFoundException {
        String availableBookId = UUID.randomUUID().toString();
        String memberId = UUID.randomUUID().toString();
        String borrowedBookId = UUID.randomUUID().toString();
        Book availableBook = new Book(availableBookId, "Available Book", "Sample Author", BookStatus.AVAILABLE, 100, "XX", null);
        given(this.bookRepository.findById(availableBookId)).willReturn(Optional.of(availableBook));
        AppUser member = new AppUser(memberId, "johnwang", "John", "Wang", "12345678", AppUserRole.MEMBER, 1);
        Book borrowedBook = new Book(borrowedBookId, "Borrowed Book", "Sample Author", BookStatus.BORROWED, 100, "YY", member);
        List<Book> borrowedBooks = new ArrayList<>();
        borrowedBooks.add(borrowedBook);
        member.setBorrowedBooks(borrowedBooks);
        given(this.appUserService.retrieveLoggedInUser()).willReturn(member);
        assertThatThrownBy(() -> this.bookService.borrowBook(availableBookId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("User " + member.getUsername() + " has borrowed the maximum allowable number of books");
        then(this.bookRepository).should(never()).save(any(Book.class));
    }

    @Test
    void itShouldBorrowBook() throws NotFoundException {
        String availableBookId = UUID.randomUUID().toString();
        String memberId = UUID.randomUUID().toString();
        String borrowedBookId = UUID.randomUUID().toString();
        Book availableBook = new Book(availableBookId, "Available Book", "Sample Author", BookStatus.AVAILABLE, 100, "XX", null);
        given(this.bookRepository.findById(availableBookId)).willReturn(Optional.of(availableBook));
        AppUser member = new AppUser(memberId, "johnwang", "John", "Wang", "12345678", AppUserRole.MEMBER, 4);
        Book borrowedBook = new Book(borrowedBookId, "Borrowed Book", "Sample Author", BookStatus.BORROWED, 100, "YY", member);
        List<Book> borrowedBooks = new ArrayList<>();
        borrowedBooks.add(borrowedBook);
        member.setBorrowedBooks(borrowedBooks);
        given(this.appUserService.retrieveLoggedInUser()).willReturn(member);
        this.bookService.borrowBook(availableBookId);
        then(this.bookRepository).should().save(bookArgumentCaptor.capture());
        Book updatedBook = new Book(availableBookId, "Available Book", "Sample Author", BookStatus.BORROWED, 100, "XX", member);
        assertThat(this.bookArgumentCaptor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(updatedBook);
    }

    @Test
    void itShouldThrowNotFoundExceptionWhenReturningBook() {
        String id = UUID.randomUUID().toString();
        given(this.bookRepository.findById(id)).willReturn(Optional.empty());
        assertThatThrownBy(() -> this.bookService.returnBook(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Book with id " + id + " cannot be found");
        then(this.bookRepository).should(never()).save(any(Book.class));
    }

    @Test
    void itShouldThrowIllegalStateExceptionWhenReturningBookThatIsNotBorrowed() {
        String id = UUID.randomUUID().toString();
        Book discontinuedBook = new Book(id, "Discontinued Book", "Sample Author", BookStatus.DISCONTINUED, 100, "XX", null);
        given(this.bookRepository.findById(id)).willReturn(Optional.of(discontinuedBook));
        assertThatThrownBy(() -> this.bookService.returnBook(id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Book with id " + id + " is not being borrowed by anyone");
        then(this.bookRepository).should(never()).save(any(Book.class));
    }

    @Test
    void itShouldThrowIllegalStateExceptionWhenLoggedInUserIsNotBorrowerOfBorrowedBook() throws NotFoundException {
        String otherMemberId = UUID.randomUUID().toString();
        String borrowedBookId = UUID.randomUUID().toString();
        String loggedInMemberId = UUID.randomUUID().toString();
        AppUser otherMember = new AppUser(otherMemberId, "sarahwang", "Sarah", "Wang", "12345678", AppUserRole.MEMBER, 1);
        Book borrowedBook = new Book(borrowedBookId, "Borrowed Book", "Sample Author", BookStatus.BORROWED, 100, "YY", otherMember);
        List<Book> otherMemberBorrowedBooks = new ArrayList<>();
        otherMemberBorrowedBooks.add(borrowedBook);
        otherMember.setBorrowedBooks(otherMemberBorrowedBooks);
        AppUser loggedInMember = new AppUser(loggedInMemberId, "johnwang", "John", "Wang", "12345678", AppUserRole.MEMBER, 1);
        given(this.bookRepository.findById(borrowedBookId)).willReturn(Optional.of(borrowedBook));
        given(this.appUserService.retrieveLoggedInUser()).willReturn(loggedInMember);
        assertThatThrownBy(() -> this.bookService.returnBook(borrowedBookId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("User " + loggedInMember.getUsername() + " is not the borrower of book with id " + borrowedBookId);
        then(this.bookRepository).should(never()).save(any(Book.class));
    }

    @Test
    void itShouldReturnBook() throws NotFoundException {
        String borrowedBookId = UUID.randomUUID().toString();
        String loggedInMemberId = UUID.randomUUID().toString();
        AppUser loggedInMember = new AppUser(loggedInMemberId, "johnwang", "John", "Wang", "12345678", AppUserRole.MEMBER, 1);
        Book borrowedBook = new Book(borrowedBookId, "Sample Title", "Sample Author", BookStatus.BORROWED, 100, "YY", loggedInMember);
        given(this.bookRepository.findById(borrowedBookId)).willReturn(Optional.of(borrowedBook));
        given(this.appUserService.retrieveLoggedInUser()).willReturn(loggedInMember);
        this.bookService.returnBook(borrowedBookId);
        then(this.bookRepository).should().save(bookArgumentCaptor.capture());
        Book updatedBook = new Book(borrowedBookId, "Sample Title", "Sample Author", BookStatus.AVAILABLE, 100, "YY", null);
        assertThat(this.bookArgumentCaptor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(updatedBook);
    }

}