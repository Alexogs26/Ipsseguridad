package ag.ipsseguridad.controller;

import ag.ipsseguridad.model.Role;
import ag.ipsseguridad.model.User;
import ag.ipsseguridad.repository.RoleRepository;
import ag.ipsseguridad.repository.UserRepository;
import ag.ipsseguridad.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, Model model) {

        if (userRepository.existsByEmail(user.getEmail())) {
            model.addAttribute("error", "El correo ya está registrado.");
            return "register";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setUsername(user.getEmail());

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Error: Rol USER no encontrdo."));
        user.setRoles(Collections.singletonList(userRole));

        userRepository.save(user);

        return "redirect:/login?registered";
    }

    @Autowired private PasswordResetService passwordResetService;

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String proccessForgotPassword(@RequestParam String email, Model model) {
        boolean sent = passwordResetService.processForgotPassword(email);

        if (!sent) {
            model.addAttribute("error", "No encontramos ese correo registrado");
            return "forgot-password";
        }
        return "redirect:/login?resetSent";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam String token, Model model) {
        User user = passwordResetService.validateToken(token);
        if (user == null) {
            return "redirect:/login?tokenError";
        }
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String saveNewPassword(@RequestParam String token, @RequestParam String password) {
        User user = passwordResetService.validateToken(token);
        if (user == null) {
            return "redirect:/login?tokenError";
        }
        passwordResetService.updatePassword(user, password);
        return "redirect:/login?resetSuccess";
    }

    @GetMapping("/profile/change-password")
    public String showChangePasswordPage(Model model) {
        return "user/change-password";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            RedirectAttributes redirectAttributes) {

        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "La contraseña es incorrecta");
            return "redirect:/profile/change-password";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("success", "Contraseña actualizada correctamente");
        return "redirect:/profile/change-password";
    }
}
