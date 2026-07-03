package com.substring.foodies.repository;

import com.substring.foodies.dto.enums.OrderStatus;
import com.substring.foodies.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    List<Order> findByRestaurantId(String restaurantId);
    List<Order> findByUserId(String userId);
    List<Order> findByDeliveryBoyId(String userId);

    // 🔁 Scheduler: move PLACED → ACCEPTED
    List<Order> findByStatusAndOrderedAtBefore(
            OrderStatus status,
            LocalDateTime time
    );

    // 🔁 Scheduler: move ACCEPTED → PREPARING
    List<Order> findByStatusIn(
           List<OrderStatus> roleList
    );

    // 🔁 Scheduler: assign delivery boy
    List<Order> findByStatusAndDeliveryBoyIsNull(
            OrderStatus status
    );
}
