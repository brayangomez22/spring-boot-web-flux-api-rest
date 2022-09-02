package com.springbootwebflux.app.handler;

import com.springbootwebflux.app.entities.Category;
import com.springbootwebflux.app.entities.Product;
import com.springbootwebflux.app.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

@Component
public class ProductHandler {

    @Autowired
    private ProductService productService;

    @Autowired
    private Validator validator;

    @Value("${config.uploads.path}")
    private String path;

    public Mono<ServerResponse> createWithPhoto(ServerRequest request) {
        Mono<Product> productMono = request.multipartData().map(multipart -> {
            FormFieldPart name = (FormFieldPart) multipart.toSingleValueMap().get("name");
            FormFieldPart price = (FormFieldPart) multipart.toSingleValueMap().get("price");
            FormFieldPart categoryId = (FormFieldPart) multipart.toSingleValueMap().get("category.id");
            FormFieldPart categoryName = (FormFieldPart) multipart.toSingleValueMap().get("category.name");

            Category category = new Category(categoryName.value());
            category.setId(categoryId.value());
            return new Product(name.value(), Double.parseDouble(price.value()), category);
        });

        return request.multipartData().map(multipart -> multipart
                        .toSingleValueMap()
                        .get("file"))
                .cast(FilePart.class)
                .flatMap(file -> productMono
                        .flatMap(product -> {
                            product.setPhoto(UUID.randomUUID().toString() + "-" + file.filename()
                                    .replace(" ", "")
                                    .replace(":", "")
                                    .replace("\\", "")
                            );
                            product.setCreateAt(new Date());
                            return file.transferTo(new File(path + product.getPhoto()))
                                    .then(productService.save(product));
                        })
                )
                .flatMap(product -> ServerResponse
                        .created(URI.create("api/v2/products/".concat(product.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(product)
                )
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> upload(ServerRequest request) {
        String id = request.pathVariable("id");
        return request.multipartData().map(multipart -> multipart
                .toSingleValueMap()
                .get("file"))
                .cast(FilePart.class)
                .flatMap(file -> productService
                        .findById(id)
                        .flatMap(product -> {
                            product.setPhoto(UUID.randomUUID().toString() + "-" + file.filename()
                                    .replace(" ", "")
                                    .replace(":", "")
                                    .replace("\\", "")
                            );
                            return file.transferTo(new File(path + product.getPhoto()))
                                    .then(productService.save(product));
                        })
                )
                .flatMap(product -> ServerResponse
                        .created(URI.create("api/v2/products/".concat(product.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(product)
                );
    }

    public Mono<ServerResponse> findAll(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(productService.findAll(), Product.class);
    }

    public Mono<ServerResponse> findById(ServerRequest request) {
        String id = request.pathVariable("id");
        return productService.findById(id).flatMap(product -> ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(product)
                .switchIfEmpty(ServerResponse.notFound().build())
        );
    }

    public Mono<ServerResponse> save(ServerRequest request) {
        Mono<Product> productMono = request.bodyToMono(Product.class);
        return productMono.flatMap(product -> {
                    Errors errors = new BeanPropertyBindingResult(product, Product.class.getName());
                    validator.validate(product, errors);

                    if(errors.hasErrors()) {
                        return Flux.fromIterable(errors.getFieldErrors())
                                .map(fieldError -> "Field " + fieldError.getField() + " " + fieldError.getDefaultMessage())
                                .collectList()
                                .flatMap(list -> ServerResponse.badRequest().bodyValue(list));
                    } else {
                        if (product.getCreateAt() == null) product.setCreateAt(new Date());
                        return productService.save(product)
                                .flatMap(p -> ServerResponse
                                        .created(URI.create("api/v2/products/".concat(p.getId())))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(p)
                                );
                    }
                });
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        Mono<Product> productMono = request.bodyToMono(Product.class);
        String id = request.pathVariable("id");
        Mono<Product> productDB = productService.findById(id);

        return productDB.zipWith(productMono, (db, req) -> {
           db.setName(req.getName());
           db.setPrice(req.getPrice());
           db.setCategory(req.getCategory());
           return db;
        })
        .flatMap(product -> ServerResponse
                .created(URI.create("api/v2/products/".concat(product.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .body(productService.save(product), Product.class)
                .switchIfEmpty(ServerResponse.notFound().build())
        );
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("id");
        Mono<Product> productDB = productService.findById(id);

        return productDB.flatMap(product -> productService
                .delete(product)
                .then(ServerResponse.noContent().build())
        ).switchIfEmpty(ServerResponse.notFound().build());
    }
}