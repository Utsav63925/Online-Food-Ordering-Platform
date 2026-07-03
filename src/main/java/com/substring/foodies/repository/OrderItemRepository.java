package com.substring.foodies.repository;

import com.substring.foodies.entity.Order;
import com.substring.foodies.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, String> {

    List<OrderItem> findByOrder(Order order);
}
