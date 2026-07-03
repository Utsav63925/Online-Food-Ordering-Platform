package com.substring.foodies.scheduler;

import com.substring.foodies.dto.enums.OrderStatus;
import com.substring.foodies.dto.enums.Role;
import com.substring.foodies.entity.Order;
import com.substring.foodies.entity.User;
import com.substring.foodies.repository.OrderRepository;
import com.substring.foodies.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DeliveryAssignmentScheduler {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void assignDeliveryBoys() {

        List<Order> orders =
                orderRepository.findByStatusAndDeliveryBoyIsNull(
                        OrderStatus.PICKED_UP
                );

        for (Order order : orders) {

            Optional<User> deliveryBoyOpt =
                    userRepository.findFirstByRoleAndIsAvailableTrue(
                            Role.ROLE_DELIVERY_BOY
                    );

            if (deliveryBoyOpt.isEmpty()) return;

            User deliveryBoy = deliveryBoyOpt.get();

            order.setDeliveryBoy(deliveryBoy);
            deliveryBoy.setAvailable(false);
        }
    }
}
