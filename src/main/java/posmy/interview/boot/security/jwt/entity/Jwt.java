package posmy.interview.boot.security.jwt.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "JWT")
public class Jwt {
    @Id
    @Column(name = "ID", unique = true, updatable = false)
    private String id;

    @Column(name = "USERNAME", updatable = false)
    private String username;

    @Column(name = "CREATED_DATE", nullable = false)
    private Date createdDate = new Date();

    public Jwt(String id, String username) {
        this.id = id;
        this.username = username;
    }
}
