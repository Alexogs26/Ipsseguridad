package ag.ipsseguridad.controller;

import ag.ipsseguridad.model.Product;
import ag.ipsseguridad.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/category/{id}")
    public String productsByCategory(@PathVariable Long id, Model model) {

        model.addAttribute("products", productService.findByCategoryId(id));

        model.addAttribute("currentSearch", "Categoría Seleccionada");

        return "index";
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        Product product = productService.findById(id);

        if (product == null) {
            return "redirect:/";
        }

        model.addAttribute("product", product);
        model.addAttribute("currentSearch", product.getName());
        return "product-detail";
    }
}
