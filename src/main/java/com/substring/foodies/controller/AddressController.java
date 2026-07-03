package com.substring.foodies.controller;

import com.substring.foodies.dto.AddressDto;
import com.substring.foodies.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    @Autowired
    private AddressService addressService;

    // CREATE
    @PostMapping("/admin")
    public ResponseEntity<AddressDto> createAddress(
            @Valid @RequestBody AddressDto addressDto) {

        AddressDto createdAddress = addressService.createAddress(addressDto);
        return ResponseEntity.status(201).body(createdAddress);
    }

    // READ single
    @GetMapping("/admin/{id}")
    public ResponseEntity<AddressDto> getAddressById(@PathVariable String id) {
        AddressDto address = addressService.getAddressById(id);
        return ResponseEntity.ok(address);
    }

    // READ all
    @GetMapping("/admin")
    public ResponseEntity<List<AddressDto>> getAllAddresses() {
        List<AddressDto> addresses = addressService.getAllAddresses();
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/{userId}/user")
    public ResponseEntity<AddressDto> getAddressByUserId(@PathVariable String userId) {
        AddressDto address = addressService.getAddressByUserId(userId);
        return ResponseEntity.ok(address);
    }

    @GetMapping("/{restaurantId}/restaurants")
    public ResponseEntity<Set<AddressDto>> getAddressesByRestaurant(
            @PathVariable String restaurantId
    ) {
        return ResponseEntity.ok(
                addressService.getAddressesByRestaurant(restaurantId)
        );
    }

    // UPDATE (full replace)
    @PutMapping("/{id}")
    public ResponseEntity<AddressDto> updateAddress(
            @PathVariable String id,
            @Valid @RequestBody AddressDto addressDto) {

        AddressDto updatedAddress = addressService.updateAddress(id, addressDto);
        return ResponseEntity.ok(updatedAddress);
    }

    // PATCH (partial update)
    @PatchMapping("/{id}")
    public ResponseEntity<AddressDto> patchAddress(
            @PathVariable String id,
            @RequestBody AddressDto patchDto) {

        AddressDto updatedAddress = addressService.patchAddress(id, patchDto);
        return ResponseEntity.ok(updatedAddress);
    }

    // DELETE
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable String id) {
        addressService.deleteAddress(id);
        return ResponseEntity.noContent().build();
    }
}
