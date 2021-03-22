package posmy.interview.boot.book.service;

import com.google.common.base.Strings;
import javassist.NotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import posmy.interview.boot.appuser.entity.AppUser;
import posmy.interview.boot.appuser.service.AppUserService;
import posmy.interview.boot.book.entity.Book;
import posmy.interview.boot.book.BookStatus;
import posmy.interview.boot.book.dto.NewBookRequest;
import posmy.interview.boot.book.dto.UpdateBookRequest;
import posmy.interview.boot.book.repository.BookRepository;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final AppUserService appUserService;

    private boolean bookIsInvalid(NewBookRequest book) {
        if (Strings.isNullOrEmpty(book.getTitle()) ||
            Strings.isNullOrEmpty(book.getIsbn()) ||
            Strings.isNullOrEmpty(book.getAuthor()) ||
            book.getNumberOfPages() == null) {
            return true;
        }
        return false;
    }

    private boolean isbnExists(String isbn) {
        return this.bookRepository.findByIsbn(isbn).isPresent();
    }

    public Book addBook(NewBookRequest request) {
        if (this.bookIsInvalid(request)) {
            throw new NullPointerException("Book must have a title, author, ISBN and number of pages");
        }
        if (request.getNumberOfPages() <= 0) {
            throw new NumberFormatException("Book cannot have 0 or negative number of pages");
        }
        if (this.isbnExists(request.getIsbn())) {
            throw new IllegalStateException("Book with ISBN: " + request.getIsbn() + " exists!");
        }
        Book newBook = new Book(
                request.getTitle(),
                request.getAuthor(),
                BookStatus.AVAILABLE,
                request.getNumberOfPages(),
                request.getIsbn(),
                null
        );
        this.bookRepository.save(newBook);
        return newBook;
    }

    public List<Book> findAllBooks() {
        return this.bookRepository.findAll();
    }

    public List<Book> findAvailableBooks() {
        return this.bookRepository.getBooksByStatus(BookStatus.AVAILABLE.name());
    }

    public List<Book> findExistingBooks() {
        return this.bookRepository.getBooksButExcludeStatus(BookStatus.DISCONTINUED.name());
    }

    public Book findBook(String id) throws NotFoundException {
        return this.retrieveBook(id);
    }

    @Transactional
    public Book updateBook(String id, UpdateBookRequest newBookRequest) throws NotFoundException {
        Book book = this.retrieveBook(id);
        if (newBookRequest.getNumberOfPages() != null) {
            if (newBookRequest.getNumberOfPages() <= 0) {
                throw new NumberFormatException("Book cannot have 0 or negative number of pages");
            }
            book.setNumberOfPages(newBookRequest.getNumberOfPages());
        }
        if (!Strings.isNullOrEmpty(newBookRequest.getIsbn())) {
            if (this.isbnExists(newBookRequest.getIsbn())) {
                throw new IllegalStateException("ISBN of " + newBookRequest.getIsbn() + " already exists!");
            }
            book.setIsbn(newBookRequest.getIsbn());
        }
        if (!Strings.isNullOrEmpty(newBookRequest.getTitle())) {
            book.setTitle(newBookRequest.getTitle());
        }
        if (!Strings.isNullOrEmpty(newBookRequest.getAuthor())) {
            book.setAuthor(newBookRequest.getAuthor());
        }
        this.bookRepository.save(book);
        return book;
    }

    @Transactional
    public Book discontinueBook(String id) throws NotFoundException {
        Book book = this.retrieveBook(id);
        if (!book.getBookStatus().equals(BookStatus.AVAILABLE)) {
            throw new IllegalStateException("Book with id " + id + " is currently being borrowed or has discontinued.");
        }
        book.setBookStatus(BookStatus.DISCONTINUED);
        this.bookRepository.save(book);
        return book;
    }

    @Transactional
    public String deleteBook(String id) throws NotFoundException {
        Book book = this.retrieveBook(id);
        if (book.getBookStatus().equals(BookStatus.BORROWED)) {
            throw new IllegalStateException("Book with id " + id + " is currently being borrowed.");
        }
        this.bookRepository.deleteById(book.getId());
        return "Book with id " + id + " has been deleted";
    }

    @Transactional
    public Book borrowBook(String bookId) throws NotFoundException {
        Book book = this.retrieveBook(bookId);
        if (!book.getBookStatus().equals(BookStatus.AVAILABLE)) {
            throw new IllegalStateException("Book with id " + bookId + " is currently being borrowed or has discontinued.");
        }
        AppUser appUser = this.appUserService.retrieveLoggedInUser();
        if (appUser.getBorrowedBooks().size() == appUser.getMembershipLevel()) {
            throw new IllegalStateException("User " + appUser.getUsername() + " has borrowed the maximum allowable number of books");
        }
        book.setBookStatus(BookStatus.BORROWED);
        book.setBorrower(appUser);
        this.bookRepository.save(book);
        return book;
    }

    @Transactional
    public Book returnBook(String bookId) throws NotFoundException {
        Book book = this.retrieveBook(bookId);
        if (!book.getBookStatus().equals(BookStatus.BORROWED)) {
            throw new IllegalStateException("Book with id " + bookId + " is not being borrowed by anyone");
        }
        AppUser appUser = this.appUserService.retrieveLoggedInUser();
        if (!appUser.getId().equals(book.getBorrower().getId())) {
            throw new IllegalStateException("User " + appUser.getUsername() + " is not the borrower of book with id " + bookId);
        }
        book.setBookStatus(BookStatus.AVAILABLE);
        book.setBorrower(null);
        this.bookRepository.save(book);
        return book;
    }

    private Book retrieveBook(String id) throws NotFoundException {
        Optional<Book> optionalBook = Optional.ofNullable(this.bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book with id " + id + " cannot be found")));
        return optionalBook.get();
    }
}
