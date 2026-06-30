package com.example.catalog.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.catalog.model.Checkout;
import com.example.catalog.model.User;
import com.example.catalog.repo.CheckoutRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    @Autowired
    private final CheckoutRepository checkoutRepository;

    @Autowired
    private final UserService userService;

    @Transactional
    public Checkout checkout(Long catalogItemId, Long userId) {
        //Lookup existing checkout
        List<Checkout> exstingCheckouts = this.findByItemId(catalogItemId);

        Checkout checkoutedItem = exstingCheckouts.stream().filter(checkout -> checkout.getCheckedOut() == true)
                .findFirst().orElse(null);

        User user = userService.getUserById(userId);

        if(checkoutedItem == null) {

            Checkout newCheckout = Checkout.builder().checkedoutBy(user).checkedoutById(userId)
                    .checkoutDateTime(LocalDateTime.now()).checkedOut(true).itemId(catalogItemId).build();
            return this.createCheckout(newCheckout);

        }

        return null;
    }

    @Transactional
    public Checkout checkin(Long catalogItemId, Long userId) {
        //Lookup existing checkout
        List<Checkout> exstingCheckouts = this.findByItemId(catalogItemId);

        //TODO Check if user is same.
        Checkout checkoutedItem = exstingCheckouts.stream().filter(checkout -> checkout.getCheckedOut() == true)
                .findFirst().orElse(null);

        if(checkoutedItem != null) {
            checkoutedItem.setCheckedOut(false);
            checkoutedItem.setCheckinDateTime(LocalDateTime.now());
            return this.updateCheckout(catalogItemId, checkoutedItem);

        }

        return null;
    }

    @Transactional
    public List<Checkout> findByItemId(Long id) {
        return checkoutRepository.findByItemId(id);
    }

    @Transactional
    public List<Checkout> findByCheckedoutById(Long userId) {
        return checkoutRepository.findByCheckedoutById(userId);
    }

    @Transactional
    public Checkout createCheckout(Checkout checkout) {
        return checkoutRepository.save(checkout);
    }

    @Transactional(readOnly = true)
    public List<Checkout> getAllCheckouts() {
        return checkoutRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Checkout getCheckoutById(Long id) {
        return checkoutRepository.findById(id).orElseThrow(() -> new RuntimeException("Checkout record not found")); // Consider a custom exception
    }

    @Transactional
    public Checkout updateCheckout(Long id, Checkout checkoutDetails) {
        Checkout checkout = getCheckoutById(id); // Will throw if not found
        checkout.setCheckedOut(checkoutDetails.getCheckedOut());
        checkout.setCheckoutDateTime(checkoutDetails.getCheckoutDateTime());
        checkout.setCheckinDateTime(checkoutDetails.getCheckinDateTime());
        checkout.setItem(checkoutDetails.getItem());
        checkout.setCheckedoutBy(checkoutDetails.getCheckedoutBy());
        // Update other fields if necessary

        return checkoutRepository.save(checkout);
    }

    @Transactional
    public void deleteCheckout(Long id) {
        checkoutRepository.deleteById(id);
    }
}
