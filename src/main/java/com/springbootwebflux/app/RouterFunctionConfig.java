package com.springbootwebflux.app;

import com.springbootwebflux.app.handler.ProductHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class RouterFunctionConfig {

    @Bean
    public RouterFunction<ServerResponse> routes(ProductHandler handler) {
        return route(GET("/api/v2/products"), handler::findAll)
                .andRoute(GET("/api/v2/products/{id}"), handler::findById)
                .andRoute(POST("/api/v2/products"), handler::save)
                .andRoute(PUT("/api/v2/products/{id}"), handler::update)
                .andRoute(DELETE("/api/v2/products/{id}"), handler::delete)
                .andRoute(POST("/api/v2/products/upload/{id}"), handler::upload)
                .andRoute(POST("/api/v2/products/create"), handler::createWithPhoto);
    }
}
