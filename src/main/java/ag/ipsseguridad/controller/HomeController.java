package ag.ipsseguridad.controller;

import ag.ipsseguridad.model.Product;
import ag.ipsseguridad.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public String home(@RequestParam(required = false) String q,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {

        PageRequest pageRequest = PageRequest.of(page, 12);
        Page<Product> productsPage;

        if (q != null && !q.isEmpty()) {
            productsPage = productService.search(q, pageRequest);
            model.addAttribute("currentSearch", q);
        } else {
            productsPage = productService.findAll(pageRequest);
        }

        model.addAttribute("products", productsPage.getContent()); // La lista de productos actual
        model.addAttribute("page", productsPage); // Toda la info de paginación (total de páginas, etc)

        return "index";
    }

    @GetMapping("/category/{id}")
    public String productsByCategory(@PathVariable Long id,
                                     @RequestParam(defaultValue = "0") int page,
                                     Model model) {

        PageRequest pageRequest = PageRequest.of(page, 12);
        Page<Product> productsPage = productService.findByCategoryId(id, pageRequest);

        model.addAttribute("products", productsPage.getContent());
        model.addAttribute("page", productsPage);
        model.addAttribute("currentSearch", "Categoría Seleccionada");
        model.addAttribute("categoryId", id);

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
