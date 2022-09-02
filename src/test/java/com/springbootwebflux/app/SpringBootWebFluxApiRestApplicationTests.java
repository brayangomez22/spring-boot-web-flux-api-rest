package com.springbootwebflux.app;

import com.springbootwebflux.app.entities.Category;
import com.springbootwebflux.app.entities.Product;
import com.springbootwebflux.app.services.ProductService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class SpringBootWebFluxApiRestApplicationTests {

	@Autowired
	private WebTestClient client;

	@Autowired
	private ProductService productService;

	@Value("${config.base.endpoint}")
	private String url;

	@Test
	void findAllTest() {
		client.get()
				.uri(url)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBodyList(Product.class)
				.consumeWith(response -> {
					List<Product> products = response.getResponseBody();
					Assertions.assertThat(products.size()>=8).isTrue();
				});
	}

	@Test
	void findAllTwoTest() {
		client.get()
				.uri(url)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBodyList(Product.class)
				.hasSize(8);
	}

	@Test
	void findByIdTest() {
		Product product = productService.findByName("Mica").block();

		client.get()
				.uri(url+"/{id}", Collections.singletonMap("id", product.getId()))
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody(Product.class)
				.consumeWith(response -> {
					Product p = response.getResponseBody();
					Assertions.assertThat(p.getId()).isNotEmpty();
					Assertions.assertThat(p.getId().length()>0).isTrue();
					Assertions.assertThat(p.getName()).isEqualTo("Mica");
				});
	}

	@Test
	void findByIdTwoTest() {
		Product product = productService.findByName("Mica").block();

		client.get()
				.uri(url+"/{id}", Collections.singletonMap("id", product.getId()))
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.id").isNotEmpty()
				.jsonPath("$.name").isEqualTo("Mica");
	}

	@Test
	void createTest() {
		Category category = productService.findCategoryByName("Electronic").block();
		Product product = new Product("pepe", 12.12, category);

		client.post()
				.uri(url)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(Mono.just(product), Product.class)
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.id").isNotEmpty()
				.jsonPath("$.name").isEqualTo("pepe")
				.jsonPath("$.category.name").isEqualTo("Electronic");
	}

	@Test
	void createTwoTest() {
		Category category = productService.findCategoryByName("Electronic").block();
		Product product = new Product("pepe", 12.12, category);

		client.post()
				.uri(url)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(Mono.just(product), Product.class)
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody(Product.class)
				.consumeWith(response -> {
					Product p = response.getResponseBody();
					Assertions.assertThat(p.getId()).isNotEmpty();
					Assertions.assertThat(p.getId().length()>0).isTrue();
					Assertions.assertThat(p.getName()).isEqualTo("pepe");
					Assertions.assertThat(p.getCategory().getName()).isEqualTo("Electronic");
				});
	}

	@Test
	void updateTest() {
		Product product  = productService.findByName("Mica").block();
		Category category = productService.findCategoryByName("Electronic").block();
		Product editedProduct = new Product("pepe", 12.12, category);

		client.put()
				.uri(url+"/{id}", Collections.singletonMap("id", product.getId()))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(Mono.just(editedProduct), Product.class)
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.id").isNotEmpty()
				.jsonPath("$.name").isEqualTo("pepe")
				.jsonPath("$.category.name").isEqualTo("Electronic");
	}

	@Test
	void deleteTest() {
		Product product = productService.findByName("Mica").block();

		client.delete()
				.uri(url+"/{id}", Collections.singletonMap("id", product.getId()))
				.exchange()
				.expectStatus().isNoContent()
				.expectBody()
				.isEmpty();
	}
}
