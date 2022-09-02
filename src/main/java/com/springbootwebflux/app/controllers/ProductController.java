package com.springbootwebflux.app.controllers;

import com.springbootwebflux.app.entities.Product;
import com.springbootwebflux.app.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Value("${config.uploads.path}")
    private String path;

    @GetMapping
    public Flux<Product> findAll() {
        return productService.findAll();
    }

    @GetMapping("{id}")
    public Mono<Product> findById(@PathVariable String id) {
        return productService.findById(id);
    }

    @PostMapping
    public Mono<Product> create(@RequestBody Product product) {
        if(product.getCreateAt() == null) product.setCreateAt(new Date());
        return productService.save(product);
    }

    @PostMapping("v2")
    public Mono<Product> createWithPhoto(Product product, @RequestPart FilePart file) {
        if(product.getCreateAt() == null) product.setCreateAt(new Date());

        product.setPhoto(UUID.randomUUID().toString() + "-" + file.filename()
                .replace(" ", "")
                .replace(":", "")
                .replace("\\", "")
        );

        return file.transferTo(new File(path + product.getPhoto())).then(productService.save(product));
    }

    @PostMapping("upload/{id}")
    public Mono<Product> upload(@PathVariable String id, @RequestPart FilePart file) {
        return productService.findById(id)
                .flatMap(product -> {
                    product.setPhoto(UUID.randomUUID().toString() + "-" + file.filename()
                            .replace(" ", "")
                            .replace(":", "")
                            .replace("\\", "")
                    );

                    return file.transferTo(new File(path + product.getPhoto())).then(productService.save(product));
                });
    }

    @PutMapping("{id}")
    public Mono<Product> update(@RequestBody Product product, @PathVariable String id) {
        return productService.findById(id)
                .flatMap(p -> {
                   p.setName(product.getName());
                   p.setPrice(product.getPrice());
                   p.setCategory(product.getCategory());

                   return productService.save(p);
                });
    }

    @DeleteMapping("{id}")
    public Mono<Void> delete(@PathVariable String id) {
        return productService.findById(id)
                .flatMap(product -> productService.delete(product));
    }
}
