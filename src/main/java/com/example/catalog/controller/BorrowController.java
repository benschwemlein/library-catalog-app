package com.example.catalog.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.catalog.dto.CheckInOutRequestDTO;
import com.example.catalog.dto.CheckoutDTO;
import com.example.catalog.model.Checkout;
import com.example.catalog.model.User;
import com.example.catalog.service.CheckoutService;
import com.example.catalog.service.UserService;

@RestController
@RequestMapping("/catalog/borrow")
public class BorrowController {

    private final CheckoutService checkoutService;

    private final UserService userService;

    private final ModelMapper mapper;

    @Autowired
    public BorrowController(CheckoutService checkoutService, UserService userService) {
        this.checkoutService = checkoutService;
        this.userService = userService;
        this.mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

    /*@PostMapping
    public ResponseEntity<Checkout> createCheckout(@RequestBody Checkout checkout) {
        Checkout createdCheckout = checkoutService.createCheckout(checkout);
        return new ResponseEntity<>(createdCheckout, HttpStatus.CREATED);
    }*/

    @GetMapping
    public ResponseEntity<List<CheckoutDTO>> getAllCheckouts() {
        List<Checkout> checkouts = checkoutService.getAllCheckouts();
        List<CheckoutDTO> checkoutDtos = checkouts.stream().map(entity -> mapper.map(entity, CheckoutDTO.class))
                .collect(Collectors.toList());
        return new ResponseEntity<>(checkoutDtos, HttpStatus.OK);
    }

    @GetMapping("/item/{itemId}")
    public ResponseEntity<List<CheckoutDTO>> getAllCheckoutsByItemId(@PathVariable("itemId") Long itemId) {
        List<Checkout> checkouts = checkoutService.findByItemId(itemId);
        List<CheckoutDTO> checkoutDtos = checkouts.stream().map(entity -> mapper.map(entity, CheckoutDTO.class))
                .collect(Collectors.toList());
        return new ResponseEntity<>(checkoutDtos, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CheckoutDTO> getCheckoutById(@PathVariable("id") Long id) {
        Checkout checkout = checkoutService.getCheckoutById(id);
        CheckoutDTO checkoutDto = mapper.map(checkout, CheckoutDTO.class);
        return new ResponseEntity<>(checkoutDto, HttpStatus.OK);
    }

    @PutMapping("/checkin")
    public ResponseEntity<CheckoutDTO> checkin(@RequestBody CheckInOutRequestDTO checkinRequest) {
        User user = userService.getUserByEmail(checkinRequest.getUserEmail());

        Checkout updatedCheckout = checkoutService.checkin(checkinRequest.getItemId(), user.getId());

        //CheckoutDTO checkoutDto = mapper.map(updatedCheckout, CheckoutDTO.class);
        return new ResponseEntity<>(CheckoutDTO.builder().build(), HttpStatus.OK);
    }

    @PutMapping("/checkout")
    public ResponseEntity<CheckoutDTO> checkout(@RequestBody CheckInOutRequestDTO checkinRequest) {
        User user = userService.getUserByEmail(checkinRequest.getUserEmail());

        Checkout updatedCheckout = checkoutService.checkout(checkinRequest.getItemId(), user.getId());
        CheckoutDTO checkoutDto = mapper.map(updatedCheckout, CheckoutDTO.class);
        return new ResponseEntity<>(checkoutDto, HttpStatus.OK);
    }

    /*@PutMapping("/{id}")
    public ResponseEntity<Checkout> updateCheckout(@PathVariable Long id, @RequestBody Checkout checkoutDetails) {
        Checkout updatedCheckout = checkoutService.updateCheckout(id, checkoutDetails);
        return new ResponseEntity<>(updatedCheckout, HttpStatus.OK);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCheckout(@PathVariable Long id) {
        checkoutService.deleteCheckout(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }*/
}
