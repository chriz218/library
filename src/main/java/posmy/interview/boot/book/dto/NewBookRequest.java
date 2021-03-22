package posmy.interview.boot.book.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class NewBookRequest {
    private final String title;
    private final String author;
    private final Integer numberOfPages;
    private final String isbn;
}
