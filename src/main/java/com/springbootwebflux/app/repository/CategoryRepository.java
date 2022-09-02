package com.springbootwebflux.app.repository;

import com.springbootwebflux.app.entities.Category;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface CategoryRepository extends ReactiveMongoRepository<Category, String> {
    public Mono<Category> findCategoryByName(String name);
}
