package posmy.interview.boot.book.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import posmy.interview.boot.appuser.entity.AppUser;
import posmy.interview.boot.book.BookStatus;

import javax.persistence.*;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "BOOK")
public class Book {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "ID", columnDefinition = "CHAR(50)", unique = true, updatable = false)
    private String id;

    @Column(name = "TITLE", nullable = false)
    private String title;

    @Column(name = "AUTHOR", nullable = false)
    private String author;

    @Enumerated(EnumType.STRING)
    @Column(name = "BOOK_STATUS", nullable = false)
    private BookStatus bookStatus;

    @Column(name = "NUMBER_OF_PAGES", nullable = false)
    private Integer numberOfPages;

    @Column(name = "ISBN", unique = true, nullable = false)
    private String isbn;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "BORROWER")
    private AppUser borrower;

    public Book(String id,
                String title,
                String author,
                BookStatus bookStatus,
                Integer numberOfPages,
                String isbn,
                AppUser borrower) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.bookStatus = bookStatus;
        this.numberOfPages = numberOfPages;
        this.isbn = isbn;
        this.borrower = borrower;
    }

    public Book(String title,
                String author,
                BookStatus bookStatus,
                int numberOfPages,
                String isbn,
                AppUser borrower) {
        this.title = title;
        this.author = author;
        this.bookStatus = bookStatus;
        this.numberOfPages = numberOfPages;
        this.isbn = isbn;
        this.borrower = borrower;
    }
}
