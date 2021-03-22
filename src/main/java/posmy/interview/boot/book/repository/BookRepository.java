package posmy.interview.boot.book.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import posmy.interview.boot.book.entity.Book;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, String> {
    Optional<Book> findByIsbn(String isbn);

    Optional<Book> findById(String id);

    @Query(value = "SELECT * FROM BOOK b WHERE b.BOOK_STATUS = :status", nativeQuery = true)
    List<Book> getBooksByStatus(@Param("status") String status);

    @Query(value = "SELECT * FROM BOOK b WHERE b.BOOK_STATUS != :status", nativeQuery = true)
    List<Book> getBooksButExcludeStatus(@Param("status") String status);
}
