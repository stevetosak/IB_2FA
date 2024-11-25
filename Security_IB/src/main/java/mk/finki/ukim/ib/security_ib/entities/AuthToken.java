package mk.finki.ukim.ib.security_ib.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "auth_token")
@Getter @Setter @ToString
public class AuthToken {
    @Id
    @SequenceGenerator(name = "auth_token_id_seq",sequenceName = "auth_token_id_seq",allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "auth_token_id_seq")
    private int id;
    @Column(name = "token_value")
    private String tokenValue;
    @Column(name = "expires")
    private LocalDateTime expiresAt;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @ManyToOne
    @JoinColumn(name = "user_id",referencedColumnName = "id",nullable = false)
    private User user;
    @Column(name = "used")
    private boolean isUsed;
    private String type;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
