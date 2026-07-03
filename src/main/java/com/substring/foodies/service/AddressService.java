package com.substring.foodies.service;

import com.substring.foodies.dto.AddressDto;
import com.substring.foodies.entity.Address;

import java.util.List;
import java.util.Set;

public interface AddressService {

    AddressDto createAddress(AddressDto address);

    AddressDto getAddressById(String id);

    AddressDto getAddressByUserId(String userId);

    List<AddressDto> getAllAddresses();

    Set<AddressDto> getAddressesByRestaurant(String restaurantId);

    AddressDto updateAddress(String id, AddressDto address);

    void deleteAddress(String id);

    AddressDto patchAddress(String id, AddressDto patch);

}
