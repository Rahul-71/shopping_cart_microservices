package com.microservices.ProductService.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.microservices.ProductService.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long>{

}
