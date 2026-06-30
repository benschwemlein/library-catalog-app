package com.example.catalog.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.catalog.model.Checkout;

public interface CheckoutRepository extends JpaRepository<Checkout, Long> {

    List<Checkout> findByItemId(Long itemId);

    List<Checkout> findByCheckedoutById(Long userId);
}
