package ag.ipsseguridad.controller;

import ag.ipsseguridad.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;

    @GetMapping("/")
    public String home(@RequestParam(required = false) String q, Model model) {

        if (q != null && !q.isEmpty()) {
            model.addAttribute("products", productService.search(q));
            model.addAttribute("currentSearch", q);
        } else {
            model.addAttribute("products", productService.findAll());
        }

        return "index";
    }
}
