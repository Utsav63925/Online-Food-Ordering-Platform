package com.substring.foodies.scheduler;

import com.substring.foodies.dto.enums.OrderStatus;
import com.substring.foodies.entity.Order;
import com.substring.foodies.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderStatusScheduler {

    private final OrderRepository orderRepository;
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void updateOrderStatuses() {

        LocalDateTime now = LocalDateTime.now(IST);

        List<Order> orders = orderRepository.findByStatusIn(
                List.of(
                        OrderStatus.PLACED,
                        OrderStatus.ACCEPTED,
                        OrderStatus.PREPARING,
                        OrderStatus.PICKED_UP
                )
        );

        for (Order order : orders) {

            long minutes =
                    Duration.between(order.getOrderedAt(), now).toMinutes();

            if (order.getStatus() == OrderStatus.PLACED && minutes >= 2) {
                order.setStatus(OrderStatus.ACCEPTED);
            }
            else if (order.getStatus() == OrderStatus.ACCEPTED && minutes >= 10) {
                order.setStatus(OrderStatus.PREPARING);
            }
            else if (order.getStatus() == OrderStatus.PREPARING && minutes >= 20) {
                order.setStatus(OrderStatus.PICKED_UP);
            }
            else if (order.getStatus() == OrderStatus.PICKED_UP && minutes >= 30) {
                order.setStatus(OrderStatus.DELIVERED);
            }
        }
    }
}
