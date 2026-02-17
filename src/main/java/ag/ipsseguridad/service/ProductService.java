package ag.ipsseguridad.service;

import ag.ipsseguridad.model.Product;
import java.util.List;

public interface ProductService {

    List<Product> findAll();

    List<Product> search(String query);

    Product save(Product product);

    Product findById(Long id);

    void deleteById(Long id);

    List<Product> findByCategoryId(Long id);
}
