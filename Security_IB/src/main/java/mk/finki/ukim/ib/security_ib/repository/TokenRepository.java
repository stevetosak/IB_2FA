package mk.finki.ukim.ib.security_ib.repository;

import mk.finki.ukim.ib.security_ib.entities.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<AuthToken, Long>{
    @Query(value = "FROM AuthToken a WHERE a.tokenValue = ?1 AND a.isUsed = false")
    Optional<AuthToken> findByTokenValue(String tokenValue);
    @Query(value = "FROM AuthToken a where a.id = ?1")
    Optional<AuthToken> findAuthTokenByUserId(int userId);
}
