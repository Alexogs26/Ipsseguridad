package ag.ipsseguridad.repository;

import ag.ipsseguridad.model.Order;
import ag.ipsseguridad.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByDateDesc(User user);
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.details d LEFT JOIN FETCH d.product WHERE o.user = :user ORDER BY o.date DESC")
    List<Order> findByUserWithFullDetails(@Param("user") User user);
    Optional<Order> findByFolio(String folio);
    List<Order> findByOrderByDateDesc();

    @Query("SELECT o FROM Order o WHERE LOWER(o.folio) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(o.user.email) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY o.date DESC")
    List<Order> searchByKeyword(String keyword);
}
