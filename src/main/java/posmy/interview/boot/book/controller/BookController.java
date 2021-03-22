package posmy.interview.boot.book.controller;

import javassist.NotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import posmy.interview.boot.book.entity.Book;
import posmy.interview.boot.book.dto.NewBookRequest;
import posmy.interview.boot.book.dto.UpdateBookRequest;
import posmy.interview.boot.book.service.BookService;

import java.util.List;

@RestController
@RequestMapping(path = "api/v1/books")
@AllArgsConstructor
public class BookController {
    private final BookService bookService;

    @PostMapping(path = "/addbook")
    @PreAuthorize("hasRole('ROLE_LIBRARIAN')")
    public Book addBook(@RequestBody NewBookRequest request) {
        try {
            return this.bookService.addBook(request);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (NullPointerException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_LIBRARIAN', 'ROLE_MEMBER')")
    public List<Book> getAllBooks() {
        return this.bookService.findAllBooks();
    }

    @GetMapping(path = "/available")
    @PreAuthorize("hasAnyRole('ROLE_LIBRARIAN', 'ROLE_MEMBER')")
    public List<Book> getAvailableBooks() {
        return this.bookService.findAvailableBooks();
    }

    @GetMapping(path = "/existing")
    @PreAuthorize("hasAnyRole('ROLE_LIBRARIAN', 'ROLE_MEMBER')")
    public List<Book> getExistingBooks() {
        return this.bookService.findExistingBooks();
    }

    @GetMapping(path = "/findone/{bookId}")
    @PreAuthorize("hasAnyRole('ROLE_LIBRARIAN', 'ROLE_MEMBER')")
    public Book getBook(@PathVariable("bookId") String bookId) {
        try {
            return this.bookService.findBook(bookId);
        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
    
    @PutMapping(path = "/update/{bookId}")
    @PreAuthorize("hasRole('ROLE_LIBRARIAN')")
    public Book updateBook(@PathVariable("bookId") String bookId, @RequestBody UpdateBookRequest request) {
        try {
            return this.bookService.updateBook(bookId, request);
        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }
    
    @PutMapping(path = "/discontinue/{bookId}")
    @PreAuthorize("hasRole('ROLE_LIBRARIAN')")
    public Book discontinueBook(@PathVariable("bookId") String bookId) {
        try {
            return this.bookService.discontinueBook(bookId);
        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @DeleteMapping(path = "/delete/{bookId}")
    @PreAuthorize("hasRole('ROLE_LIBRARIAN')")
    public String deleteBook(@PathVariable("bookId") String bookId) {
        try {
            return this.bookService.deleteBook(bookId);
        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @PutMapping(path = "/borrow/{bookId}")
    @PreAuthorize("hasRole('ROLE_MEMBER')")
    public Book borrowBook(@PathVariable("bookId") String bookId) {
        try {
            return this.bookService.borrowBook(bookId);
        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @PutMapping(path = "/return/{bookId}")
    @PreAuthorize("hasRole('ROLE_MEMBER')")
    public Book returnBook(@PathVariable("bookId") String bookId) {
        try {
            return this.bookService.returnBook(bookId);
        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }
}
