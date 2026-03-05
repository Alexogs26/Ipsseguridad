package ag.ipsseguridad.controller;

import ag.ipsseguridad.model.Product;
import ag.ipsseguridad.model.ProductMedia;
import ag.ipsseguridad.repository.CategoryRepository;
import ag.ipsseguridad.repository.ProductRepository;
import ag.ipsseguridad.repository.SupplierRepository;
import ag.ipsseguridad.service.ProductImportService;
import ag.ipsseguridad.service.ProductService;
import ag.ipsseguridad.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final CloudinaryService cloudinaryService;
    private final ProductImportService productImportService;

    @GetMapping
    public String listProducts(@RequestParam(name = "q", required = false) String keyword, Model model) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            model.addAttribute("products", productRepository.searchByKeyword(keyword));
            model.addAttribute("keyword", keyword);
        } else {
            model.addAttribute("products", productRepository.findAll());
        }
        return "admin/product-list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("suppliers", supplierRepository.findAll());
        return "admin/product-form";
    }

    @PostMapping("/save")
    public String saveProduct(@ModelAttribute Product productForm,
                              @RequestParam(value = "images", required = false)MultipartFile[] images,
                              @RequestParam(value = "videoUrl", required = false) String videoUrl,
                              @RequestParam(value = "deleteMediaIds", required = false) List<Long> deleteMediaIds) {

        Product productToSave;

        if (productForm.getId() != null) {
            productToSave = productService.findByIdWithMedia(productForm.getId());

            productToSave.setSku(productForm.getSku());
            productToSave.setName(productForm.getName());
            productToSave.setDescription(productForm.getDescription());
            productToSave.setPrice(productForm.getPrice());
            productToSave.setCost(productForm.getCost());
            productToSave.setStock(productForm.getStock());
            productToSave.setCategory(productForm.getCategory());
            productToSave.setSupplier(productForm.getSupplier());

            if (deleteMediaIds != null && !deleteMediaIds.isEmpty()) {
                List<ProductMedia> mediaToDelete = productToSave.getMediaList().stream()
                        .filter(media -> deleteMediaIds.contains(media.getId()))
                        .toList();

                for (ProductMedia media : mediaToDelete) {
                    if (media.getType() == ProductMedia.MediaType.IMAGE) {
                        cloudinaryService.deleteImageFromUrl(media.getUrl());
                    }
                }

                productToSave.getMediaList().removeAll(mediaToDelete);
            }
        } else {
            productToSave = productForm;
        }

        if (images != null && images.length > 0 && !images[0].isEmpty()) {
            int orderIndex = productToSave.getMediaList().size() + 1;

            for (MultipartFile file : images) {
                try {
                    String urlCloudinary = cloudinaryService.uploadImage(file);
                    ProductMedia media = ProductMedia.builder()
                            .url(urlCloudinary)
                            .type(ProductMedia.MediaType.IMAGE)
                            .order(orderIndex++)
                            .product(productToSave)
                            .build();
                    productToSave.getMediaList().add(media);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }

        if (videoUrl != null && !videoUrl.trim().isEmpty()) {

        ProductMedia videoMedia = ProductMedia.builder()
                .url(videoUrl)
                .type(ProductMedia.MediaType.VIDEO)
                .order(99)
                .product(productToSave)
                .build();

        productToSave.getMediaList().add(videoMedia);
        }

        productService.save(productToSave);
        return "redirect:/admin/products";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Product product = productService.findById(id);

        if (product == null) {
            return "redirect:/admin/products";
        }

        model.addAttribute("product", product);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("suppliers", supplierRepository.findAll());

        return "admin/product-form";
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteById(id);
        return "redirect:/admin/products"; // recharge list
    }

    @PostMapping("/import")
    public String importCsv(@RequestParam("file") MultipartFile file,
                            @RequestParam("exchangeRate") BigDecimal exchangeRate,
                            @RequestParam("margin") BigDecimal margin) {
        try {
            productImportService.importAdisesCsv(file, exchangeRate, margin);
            // Idealmente aquí pondrías un FlashAttribute diciendo "Importación Exitosa"
        } catch (Exception e) {
            System.err.println("Error masivo: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }
}
