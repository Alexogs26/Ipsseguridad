package ag.ipsseguridad.service;

import ag.ipsseguridad.model.PasswordResetToken;
import ag.ipsseguridad.model.User;
import ag.ipsseguridad.repository.TokenRepository;
import ag.ipsseguridad.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public boolean processForgotPassword(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return false;
        }

        User user = userOptional.get();

        tokenRepository.findByUser(user).ifPresent(oldToken -> {
            tokenRepository.delete(oldToken);
            tokenRepository.flush();
        });

        PasswordResetToken token = new PasswordResetToken(user);
        tokenRepository.save(token);

        String resetLink = "http://localhost:8080/reset-password?token=" + token.getToken();
        sendEmail(email, resetLink);

        return true;
    }

    public User validateToken(String token) {
        Optional<PasswordResetToken> resetToken = tokenRepository.findByToken(token);

        if (resetToken.isEmpty() || resetToken.get().getExpiryDate().isBefore(LocalDateTime.now())) {
            return null;
        }

        return resetToken.get().getUser();
    }

    public void updatePassword(String tokenStr, String newPassword) {
        PasswordResetToken token = tokenRepository.findByToken(tokenStr)
                .orElseThrow(() -> new RuntimeException("Token Inválido"));

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenRepository.delete(token);
    }

    private void sendEmail(String to, String link) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom("noreply.ipsseguridad@gmail.com");
            msg.setTo(to);
            msg.setSubject("Recuperar Contraseña - IPS Seguridad");
            msg.setText("Hola,\n\n" +
                    "Para restablecer tu contraseña, usa el siguiente enlace:\n" +
                    link + "\n\n" +
                    "(Si no solicitaste esto, ignora este mensaje).");

            mailSender.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    public void removeExpiredTokens() {
        tokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
    }
}
