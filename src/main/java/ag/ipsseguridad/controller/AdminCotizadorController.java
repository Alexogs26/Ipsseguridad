package ag.ipsseguridad.controller;

import ag.ipsseguridad.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/cotizador")
@RequiredArgsConstructor
public class AdminCotizadorController {

    private final ProductRepository productRepository;

    @GetMapping
    public String viewCotizador() {
        return "admin/cotizador";
    }

    @GetMapping("/api/catalogo")
    @ResponseBody
    public List<Map<String, Object>> getCatalogoApi() {
        return productRepository.findAll().stream().map(p -> {
            Map<String, Object> dto = new HashMap<>();
            dto.put("sku", p.getSku());
            dto.put("name", p.getName());
            dto.put("costUsd", p.getCostUsd() != null ? p.getCostUsd() : 0.0);
            dto.put("imagen", p.getMainImageUrl() != null ? p.getMainImageUrl() : "https://via.placeholder.com/50");
            return dto;
        }).collect(Collectors.toList());
    }
}
