package ag.ipsseguridad.controller;

import ag.ipsseguridad.model.Order;
import ag.ipsseguridad.model.OrderStatus;
import ag.ipsseguridad.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderRepository orderRepository;

    @GetMapping
    public String listOrders(@RequestParam(name = "q", required = false) String keyword, Model model) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            model.addAttribute("orders", orderRepository.searchByKeyword(keyword));
            model.addAttribute("keyword", keyword);
        } else {
            model.addAttribute("orders", orderRepository.findByOrderByDateDesc());
        }
        model.addAttribute("allStatuses", OrderStatus.values());
        return "admin/order-list";
    }

    @PostMapping("/{id}/update-status")
    public String updateStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido inválido: " + id));
        order.setStatus(status);
        orderRepository.save(order);

        return "redirect:/admin/orders";
    }
}
