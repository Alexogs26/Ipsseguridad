package ag.ipsseguridad.controller;

import ag.ipsseguridad.dto.CartItem;
import ag.ipsseguridad.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public String viewCart(Model model) {
        model.addAttribute("cartItems", cartService.getItems());
        model.addAttribute("total", cartService.getTotal());
        return "cart/view";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId,
                            @RequestParam(defaultValue = "1") Integer quantity,
                            HttpServletRequest request) {
        cartService.addProduct(productId,quantity);

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

    @GetMapping("/remove/{id}")
    public String removeFromCart(@PathVariable Long id) {
        cartService.removeProduct(id);
        return "redirect:/cart";
    }

    @PostMapping("/update")
    public String updateQuantity(@RequestParam Long productId, @RequestParam Integer quantity) {
        cartService.updateQuantity(productId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/checkout/whatsapp")
    public String checkoutWhatsapp() {
        List<CartItem> items = cartService.getItems();
        if (items.isEmpty()) return "redirect:/cart";

        StringBuilder sb = new StringBuilder();
        sb.append("Hola *IPSSEGURIDAD*, me interesa cotizar la siguiente lista de equipos:\n\n");

        for (CartItem item : items) {
            sb.append("▪️ ").append(item.getQuantity()).append("x ")
                    .append(item.getProduct().getSku()).append(" (")
                    .append(item.getProduct().getName()).append(")\n");
        }

        sb.append("\n¿Me podrían confirmar el precio total, disponibilidad y opciones de envío?");

        try {
            String encodedText = java.net.URLEncoder.encode(sb.toString(), java.nio.charset.StandardCharsets.UTF_8.toString())
                    .replace("+", "%20");
            return "redirect:https://wa.me/5213322560090?text=" + encodedText;
        } catch (Exception e) {
            return "redirect:/cart";
        }
    }
}
