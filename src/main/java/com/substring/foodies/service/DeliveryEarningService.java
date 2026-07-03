package com.substring.foodies.service;

import com.substring.foodies.dto.DeliveryEarningDto;

import java.util.List;

public interface DeliveryEarningService {

    List<DeliveryEarningDto> getDeliveryEarningByDeliveryBoy(String id);
}
