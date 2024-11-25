package mk.finki.ukim.ib.security_ib.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter @Setter
public class User {
    @Id
    @SequenceGenerator(name = "users_id_seq",sequenceName = "users_id_seq",allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "users_id_seq")
    private int id;
    private String username;
    private String email;
    private String password;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "last_updated")
    private LocalDateTime updatedAt;
    @Column(name = "verified")
    private boolean isAccountVerified;
}
