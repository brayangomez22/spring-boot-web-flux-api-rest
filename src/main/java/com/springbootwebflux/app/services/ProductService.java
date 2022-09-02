package com.springbootwebflux.app.services;

import com.springbootwebflux.app.entities.Category;
import com.springbootwebflux.app.entities.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductService {
    public Flux<Product> findAll();
    public Flux<Product> findAllWithName();
    public Mono<Product> findById(String id);
    public Mono<Product> save(Product product);
    public Mono<Void> delete(Product product);
    public Flux<Category> findAllCategories();
    public Mono<Category> findCategoryById(String id);
    public Mono<Category> saveCategory(Category category);
    public Mono<Product> findByName(String name);
    public Mono<Category> findCategoryByName(String name);
}
