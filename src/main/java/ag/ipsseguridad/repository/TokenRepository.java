package ag.ipsseguridad.repository;

import ag.ipsseguridad.model.PasswordResetToken;
import ag.ipsseguridad.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByUser(User user);

    @Modifying
    @Transactional
    void deleteByExpiryDateBefore(LocalDateTime now);

    @Transactional
    void deleteByToken(String token);
}
