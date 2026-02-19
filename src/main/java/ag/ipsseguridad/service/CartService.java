package ag.ipsseguridad.service;

import ag.ipsseguridad.dto.CartItem;
import ag.ipsseguridad.model.Product;
import ag.ipsseguridad.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@SessionScope
@RequiredArgsConstructor
public class CartService {

    private final ProductRepository productRepository;

    private Map<Long, CartItem> items = new HashMap<>();

    public void addProduct(Long productId, Integer quantity) {

        CartItem item = items.get(productId);

        if (item == null) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            item = new CartItem(product, quantity);
            items.put(productId, item);
        } else {
            item.setQuantity(item.getQuantity() + quantity);
        }
    }

    public void removeProduct(Long productId) {
        items.remove(productId);
    }

    public void updateQuantity(Long productId, Integer quantity) {
        if (items.containsKey(productId)) {
            if (quantity <= 0) {
                items.remove(productId);
            } else {
                items.get(productId).setQuantity(quantity);
            }
        }
    }

    public List<CartItem> getItems() {
        return new ArrayList<>(items.values());
    }

    public BigDecimal getTotal() {
        return items.values().stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getCount() {
        if (items.isEmpty()) {
            return 0;
        }
        return items.values().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public void clearCart() {
        items.clear();
    }
}

