package com.university.booking.controller;

import com.university.booking.dto.BookingRequest;
import com.university.booking.model.Booking;
import com.university.booking.model.Cart;
import com.university.booking.service.CartService;
import com.university.booking.web.PersonAuthInterceptor;

import jakarta.validation.Valid;

import lombok.AllArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@AllArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<Cart> getCart(@RequestAttribute(PersonAuthInterceptor.PERSON_ID_ATTRIBUTE) String personId) {
        return ResponseEntity.ok(cartService.getCart(personId));
    }

    @PostMapping("/items")
    public ResponseEntity<Cart> addItem(@RequestAttribute(PersonAuthInterceptor.PERSON_ID_ATTRIBUTE) String personId,
                                        @Valid @RequestBody BookingRequest request) {
        return ResponseEntity.ok(cartService.addItem(personId, request));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Cart> removeItem(@RequestAttribute(PersonAuthInterceptor.PERSON_ID_ATTRIBUTE) String personId,
                                           @PathVariable("itemId") String itemId) {
        return ResponseEntity.ok(cartService.removeItem(personId, itemId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@RequestAttribute(PersonAuthInterceptor.PERSON_ID_ATTRIBUTE) String personId) {
        cartService.clearCart(personId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/checkout")
    public ResponseEntity<List<Booking>> checkout(@RequestAttribute(PersonAuthInterceptor.PERSON_ID_ATTRIBUTE) String personId) {
        return ResponseEntity.ok(cartService.checkout(personId));
    }
}
