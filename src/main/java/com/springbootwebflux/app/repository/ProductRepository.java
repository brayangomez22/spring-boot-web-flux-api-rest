package com.springbootwebflux.app.repository;

import com.springbootwebflux.app.entities.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ProductRepository extends ReactiveMongoRepository<Product, String> {
    public Mono<Product> findByName(String name);
}
