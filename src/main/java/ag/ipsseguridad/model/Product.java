package ag.ipsseguridad.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String sku;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    private BigDecimal cost;

    private Integer stock;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("order ASC")
    @Builder.Default
    private List<ProductMedia> mediaList = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    public String getMainImageUrl() {

        if (mediaList == null || mediaList.isEmpty()) {
            return "https://via.placeholder.com/300?text=Sin+Imagen";
        }

        return mediaList.stream()
                .filter(m -> m.getType() == ProductMedia.MediaType.IMAGE)
                .findFirst()
                .map(ProductMedia::getUrl)
                .orElse("https://via.placeholder.com/300?text=Sin+Imagen");
    }
}
