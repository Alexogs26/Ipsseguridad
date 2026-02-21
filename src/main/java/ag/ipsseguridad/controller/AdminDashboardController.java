package ag.ipsseguridad.controller;

import ag.ipsseguridad.repository.OrderRepository;
import ag.ipsseguridad.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("totalProducts", productRepository.count());
        model.addAttribute("totalOrders", orderRepository.count());

        return "admin/dashboard";
    }
}
