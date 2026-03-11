package ag.ipsseguridad.service;

import ag.ipsseguridad.model.Category;
import ag.ipsseguridad.model.Product;
import ag.ipsseguridad.model.Supplier;
import ag.ipsseguridad.repository.CategoryRepository;
import ag.ipsseguridad.repository.ProductRepository;
import ag.ipsseguridad.repository.SupplierRepository;
import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductImportService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;

    @Transactional
    public void importAdisesCsv(MultipartFile file, BigDecimal exchangeRate, BigDecimal profitMarginPercent) throws Exception {

        Supplier supplier = supplierRepository.findAll().stream()
                .filter(s -> s.getName().toLowerCase().contains("adises") || s.getName().toLowerCase().contains("tiandy"))
                .findFirst()
                .orElseGet(() -> {
                    Supplier newSupplier = Supplier.builder()
                            .name("Adises (Tiandy)")
                            .email("contacto@adises.com")
                            .build();
                    return supplierRepository.save(newSupplier);
                });

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String[] line;
            int rowNumber = 0;

            while ((line = reader.readNext()) != null) {
                rowNumber++;

                if (rowNumber <= 2) continue;

                if (line.length < 12 || line[1] == null || line[1].trim().isEmpty()) continue;

                try {
                    String sku = line[1].trim();
                    String silverStr= line[11].trim();
                    if (sku.isEmpty() || sku.length() < 3 || !silverStr.matches(".*\\d.*") || sku.toLowerCase().contains("lista")) {
                        continue;
                    }
                    String categoryName = line[3].trim();
                    String description = line[5].trim();

                    int stock = parseIntSafe(line[8]);
                    BigDecimal costUsdNoIva = parseBigDecimalSafe(line[11]);

                    BigDecimal costMxnNoIva = costUsdNoIva.multiply(exchangeRate);
                    BigDecimal finalCostMxn = costMxnNoIva.multiply(new BigDecimal("1.16")).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal marginMultiplier = BigDecimal.ONE.add(profitMarginPercent.divide(new BigDecimal("100")));
                    BigDecimal finalPriceMxn = costMxnNoIva.multiply(marginMultiplier).multiply(new BigDecimal("1.16")).setScale(2, RoundingMode.HALF_UP);

                    Category category = null;
                    if (!categoryName.isEmpty()) {
                        category = categoryRepository.findAll().stream()
                                .filter(c -> c.getName().equalsIgnoreCase(categoryName))
                                .findFirst()
                                .orElseGet(() -> categoryRepository.save(Category.builder().name(categoryName).build()));
                    }

                    Optional<Product> existingProduct = productRepository.findBySku(sku);

                    if (existingProduct.isPresent()) {
                        Product p = existingProduct.get();
                        p.setStock(stock);
                        p.setCostUsd(costUsdNoIva);
                        p.setCost(finalCostMxn);
                        p.setPrice(finalPriceMxn);
                        productRepository.save(p);
                    } else {
                        Product p = Product.builder()
                                .sku(sku)
                                .name(description.length() > 255 ? description.substring(0, 250) : description)
                                .description(line[4].trim()) // Ponemos los "Technology Highlights" en la descripción detallada
                                .stock(stock)
                                .costUsd(costUsdNoIva)
                                .cost(finalCostMxn)
                                .price(finalPriceMxn)
                                .supplier(supplier)
                                .category(category)
                                .build();
                        productRepository.save(p);
                    }
                } catch (Exception e) {
                    System.err.println("Error procesando fila " + rowNumber + ": " + e.getMessage());
                }
            }
        }
    }

    private int parseIntSafe(String value) {
        try { return Integer.parseInt(value.trim()); } catch (Exception e) { return 0; }
    }

    private BigDecimal parseBigDecimalSafe(String value) {
        try {
            String cleanValue = value.replace("$", "").replace(",", "").trim();
            return new BigDecimal(cleanValue);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}