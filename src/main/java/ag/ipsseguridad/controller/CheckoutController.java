package ag.ipsseguridad.controller;

import ag.ipsseguridad.dto.CartItem;
import ag.ipsseguridad.model.*;
import ag.ipsseguridad.repository.OrderRepository;
import ag.ipsseguridad.repository.UserRepository;
import ag.ipsseguridad.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @PostMapping("/process")
    public String processOrder(@AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam(defaultValue = "Whatsapp/Transferencia") String paymentMethod,
                               RedirectAttributes redirectAttributes) {

        if (userDetails == null) return "redirect:login";
        if (cartService.getItems().isEmpty()) return "redirect:/cart";

        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        Order order = new Order();
        order.setDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDIENTE_PAGO);
        order.setUser(user);
        order.setTotal(cartService.getTotal());
        order.setPaymentMethod(paymentMethod);
        order.setShippingMethod("Por Acordar");

        String folio = "IPS-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        order.setFolio(folio);

        List<CartItem> cartItems = cartService.getItems();
        for (CartItem item : cartItems) {
            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setQuantity(item.getQuantity());
            detail.setPrice(item.getProduct().getPrice());

            order.getDetails().add(detail);
        }

        orderRepository.save(order);
        cartService.clearCart();
        String whatsappLink = generateWhatsAppMessage(order, cartItems);
        return "redirect:" + whatsappLink;
    }

    private String generateWhatsAppMessage(Order order, List<CartItem> items) {

        StringBuilder msg = new StringBuilder();
        msg.append("Hola, generé el pedido *").append(order.getFolio()).append("*").append("\n");
        msg.append("Quiero confirmar stock, precio y datos para transferencia.\n\n");

        for (CartItem item : items) {
            msg.append("- ").append(item.getQuantity()).append("x ").append(item.getProduct().getName()).append("\n");
        }

        msg.append("\n*Total: $").append(order.getTotal()).append("*");
        msg.append("\nUsuario: ").append(order.getUser().getEmail());

        try {
            String encoded = URLEncoder.encode(msg.toString(), StandardCharsets.UTF_8).replace("+", "%20");
            return "https://api.whatsapp.com/send?phone=5213322560090&text=" + encoded;
        } catch (Exception e) {
            return "https://api.whatsapp.com/send?phone=5213322560090";
        }
    }
}
