package ag.ipsseguridad.controller;

import ag.ipsseguridad.model.User;
import ag.ipsseguridad.repository.OrderRepository;
import ag.ipsseguridad.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class UserOrderController {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @GetMapping("/mis-pedidos")
    public String myOrders(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        model.addAttribute("orders", orderRepository.findByUserWithFullDetails(user));
        return "user/orders";
    }
}
