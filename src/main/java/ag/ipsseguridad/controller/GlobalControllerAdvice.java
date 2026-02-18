package ag.ipsseguridad.controller;

import ag.ipsseguridad.repository.CategoryRepository;
import ag.ipsseguridad.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final CategoryRepository categoryRepository;
    private final CartService cartService;

    @ModelAttribute
    public void addCategoriesToModel(Model model) {
        model.addAttribute("menuCategories", categoryRepository.findAll());
        model.addAttribute("cartCount", cartService.getCount());
    }
}
