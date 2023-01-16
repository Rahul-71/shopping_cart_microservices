package com.microservices.ProductService.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.microservices.ProductService.entity.Product;
import com.microservices.ProductService.exception.ProductServiceCustomException;
import com.microservices.ProductService.model.ProductRequest;
import com.microservices.ProductService.model.ProductResponse;
import com.microservices.ProductService.repository.ProductRepository;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public long addProduct(ProductRequest productRequest) {
        log.info("Adding Product...");

        Product product = Product.builder()
                .productName(productRequest.getName())
                .quantity(productRequest.getQuantity())
                .price(productRequest.getPrice())
                .build();

        productRepository.save(product);
        log.info("Product created..");

        return product.getProductId();
    }

    @Override
    public ProductResponse getProductById(long productId) {
        Product product = this.productRepository.findById(productId)
                .orElseThrow(() -> new ProductServiceCustomException(
                        "Product not found with ID : " + productId + " !!", "PRODUCT_NOT_FOUND"));

        ProductResponse prodRes = new ProductResponse();

        // use to copy from one source to destination object.
        // So, beanUtils helps to copy that
        BeanUtils.copyProperties(product, prodRes);

        return prodRes;
    }

    @Override
    public void reduceQuantity(long productId, long quantity) {
        log.info("Reduce quantity: {} for productId: {}", quantity, productId);

        Product product = this.productRepository.findById(productId)
                .orElseThrow(() -> new ProductServiceCustomException(
                        "Product not found with ID : " + productId + " !!", "PRODUCT_NOT_FOUND"));

        if (product.getQuantity() < quantity) {
            log.warn("Insufficient quantity....");
            throw new ProductServiceCustomException(
                    "Product does not have sufficient quantity.", "INSUFFICIENT_QUANTITY");
        }

        product.setQuantity(product.getQuantity() - quantity);

        this.productRepository.save(product);
        log.info("Product quantity updated successfully !");
    }

}
