package ag.ipsseguridad.repository;

import ag.ipsseguridad.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    Page<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description, Pageable pageable);

    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchByKeyword(String keyword);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.mediaList WHERE p.id = :id")
    Optional<Product> findByIdWithMedia(@Param("id") Long id);

    @Query(value = "SELECT p FROM Product p",
            countQuery = "SELECT count(p) FROM Product p")
    Page<Product> findAllProductsPaged(Pageable pageable);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.mediaList WHERE p.id IN :ids")
    List<Product> findAllByIdWithMedia(@Param("ids") Iterable<Long> ids);
}
