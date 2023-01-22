package com.microservices.orderservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.microservices.orderservice.entity.Order;
import com.microservices.orderservice.exception.CustomException;
import com.microservices.orderservice.external.client.PaymentService;
import com.microservices.orderservice.external.client.ProductService;
import com.microservices.orderservice.external.request.PaymentRequest;
import com.microservices.orderservice.external.response.PaymentDetails;
import com.microservices.orderservice.model.OrderRequest;
import com.microservices.orderservice.model.OrderResponse;
import com.microservices.orderservice.model.PaymentMode;
import com.microservices.orderservice.model.ProductDetails;
import com.microservices.orderservice.repository.OrderRepository;

@SpringBootTest
public class OrderServiceImplTest {

        @Mock
        private OrderRepository orderRepo;

        @Mock
        private ProductService productService;

        @Mock
        private PaymentService paymentService;

        @Mock
        private RestTemplate restTemplate;

        @InjectMocks
        OrderService orderService = new OrderServiceImpl();

        @DisplayName("Get Order - Success Scenario")
        @Test
        void when_order_success_test() {

                int orderId = 1;

                ///////// mocking

                // // 1. mock orderRepo.findById
                Order mockOrder = getMockOrder();
                Mockito.when(orderRepo.findById(anyLong()))
                                .thenReturn(Optional.of(mockOrder));

                // 2. mock productService via restTemplate
                Mockito.when(restTemplate.getForObject("http://PRODUCT-SERVICE/product/"
                                + mockOrder.getProductId(), ProductDetails.class)).thenReturn(getMockProductDetails());

                // 3. mock paymentService via restTemplate
                Mockito.when(restTemplate.getForObject("http://PAYMENT-SERVICE/payment/order/"
                                + mockOrder.getId(), PaymentDetails.class)).thenReturn(getMockPaymentDetails());

                //////////// actual
                OrderResponse orderResponse = orderService.getOrderDetails(orderId);

                /////////// verification
                Mockito.verify(orderRepo, times(1)).findById(anyLong());
                Mockito.verify(restTemplate, times(1)).getForObject(
                                "http://PRODUCT-SERVICE/product/" + mockOrder.getProductId(), ProductDetails.class);
                Mockito.verify(restTemplate, times(1)).getForObject(
                                "http://PAYMENT-SERVICE/payment/order/" + mockOrder.getId(), PaymentDetails.class);

                ///////////// assert
                Assertions.assertNotNull(orderResponse, "OrderResponse is null!");
                assertEquals(mockOrder.getId(), orderResponse.getOrderId());

        }

        @Test
        @DisplayName("Get Orders - Failure Scenario")
        void when_Get_Order_NOT_FOUND_test() {
                Mockito.when(orderRepo.findById(anyLong()))
                                .thenReturn(Optional.ofNullable(null));

                CustomException exception = assertThrows(CustomException.class,
                                () -> orderService.getOrderDetails(1));

                assertEquals("NOT_FOUND", exception.getErrorCode());
                assertEquals(404, exception.getStatus());

                verify(orderRepo, times(1)).findById(anyLong());
        }

        @Test
        @DisplayName("Place Order - Success Scenario")
        void when_Place_order_Success() {
                Order order = getMockOrder();
                OrderRequest orderReq = getMockOrderRequest();

                when(orderRepo.save(any(Order.class))).thenReturn(order);

                when(productService.reduceQuantity(anyLong(), anyLong()))
                                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

                when(paymentService.doPayment(any(PaymentRequest.class)))
                                .thenReturn(new ResponseEntity<>(1L, HttpStatus.OK));

                long orderId = orderService.placeOrder(orderReq);

                verify(orderRepo, times(2)).save(any());
                verify(productService, times(1)).reduceQuantity(anyLong(), anyLong());
                verify(paymentService, times(1)).doPayment(any(PaymentRequest.class));

                assertEquals(order.getId(), orderId);
        }

        @Test
        @DisplayName("Place Order - Payment Failed Scenario")
        void test_when_Place_Order_Payment_Fails_then_Order_Placed() {
                Order order = getMockOrder();
                OrderRequest orderReq = getMockOrderRequest();

                when(orderRepo.save(any(Order.class))).thenReturn(order);

                when(productService.reduceQuantity(anyLong(), anyLong()))
                                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

                when(paymentService.doPayment(any(PaymentRequest.class)))
                                .thenThrow(new RuntimeException());

                long orderId = orderService.placeOrder(orderReq);

                verify(orderRepo, times(2)).save(any());
                verify(productService, times(1)).reduceQuantity(anyLong(), anyLong());
                verify(paymentService, times(1)).doPayment(any(PaymentRequest.class));

                assertEquals(order.getId(), orderId);
        }

        private OrderRequest getMockOrderRequest() {
                return OrderRequest.builder()
                                .productId(1)
                                .totalAmount(100)
                                .quantity(10)
                                .paymentMode(PaymentMode.CASH)
                                .build();
        }

        private PaymentDetails getMockPaymentDetails() {
                return PaymentDetails.builder()
                                .paymentId(1)
                                .status("ACCEPTED")
                                .paymentMode(PaymentMode.CASH)
                                .amount(1100)
                                .paymentDate(Instant.now())
                                .orderId(1)
                                .build();
        }

        private ProductDetails getMockProductDetails() {
                return ProductDetails.builder()
                                .productName("iphone")
                                .productId(1)
                                .price(1100)
                                .quantity(2)
                                .build();
        }

        private Order getMockOrder() {
                return Order.builder()
                                .id(1)
                                .productId(1)
                                .quantity(2)
                                .amount(200)
                                .orderStatus("PLACED")
                                .orderDate(Instant.now())
                                .build();
        }

}
