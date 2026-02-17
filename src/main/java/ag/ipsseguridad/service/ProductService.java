package ag.ipsseguridad.service;

import ag.ipsseguridad.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {

    Page<Product> findAll(Pageable pageable);

    Page<Product> search(String query, Pageable pageable);

    Product save(Product product);

    Product findById(Long id);

    void deleteById(Long id);

    Page<Product> findByCategoryId(Long id, Pageable pageable);
}
