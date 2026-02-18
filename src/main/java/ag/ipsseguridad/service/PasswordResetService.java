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

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    public boolean processForgotPassword(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) return false;

        User user = userOptional.get();

        PasswordResetToken token = new PasswordResetToken(user);
        tokenRepository.save(token);

        String resetLink = "http://localhost:8080/reset-password?token=" + token.getToken();

        return true;
    }

    public User validateToken(String token) {
        Optional<PasswordResetToken> resetToken = tokenRepository.findByToken(token);

        if (resetToken.isEmpty() || resetToken.get().getExpiryDate().isBefore(LocalDateTime.now())) {
            return null;
        }

        return resetToken.get().getUser();
    }

    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

    }

    private void sendEmail(String to, String link) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Recuperar Contraseña - IPS Seguridad");
        msg.setText("Hola,\n\nHas solicitado restablecer tu contraseña.\n" +
                "Haz clic en el siguiente enlace para crear una nueva:\n\n" + link +
                "\n\nEste enlace expira en 1 hora.");
        mailSender.send(msg);
    }
}
