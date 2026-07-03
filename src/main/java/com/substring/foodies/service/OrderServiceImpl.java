package com.substring.foodies.service;

import com.substring.foodies.dto.OrderDto;
import com.substring.foodies.dto.OrderPlaceRequest;
import com.substring.foodies.dto.enums.OrderStatus;
import com.substring.foodies.dto.enums.PaymentMode;
import com.substring.foodies.dto.enums.PaymentStatus;
import com.substring.foodies.dto.enums.Role;
import com.substring.foodies.entity.*;

import com.substring.foodies.exception.ResourceNotFound;
import com.substring.foodies.repository.*;
import jakarta.transaction.Transactional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private ModelMapper mapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    private User getLoggedInUser() {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("Invalid session"));
    }

    private void validateRestaurantAccess(Restaurant restaurant) {

        User user = getLoggedInUser();

        Role role = user.getRole();

        // ❌ Only ADMIN or RESTAURANT_ADMIN allowed
        if (role != Role.ROLE_ADMIN && role != Role.ROLE_RESTAURANT_ADMIN) {
            throw new AccessDeniedException(
                    "Access denied. Only ADMIN or RESTAURANT_ADMIN can manage restaurants."
            );
        }

        if (user.getRole() == Role.ROLE_RESTAURANT_ADMIN &&
                !user.getId().equals(restaurant.getOwner().getId())) {

            throw new AccessDeniedException(
                    "You are not authorized to perform this action. " +
                            "Only the restaurant owner or an admin can perform this action."
            );
        }
    }

    @Override
    @Transactional
    public OrderDto placeOrderRequest(OrderPlaceRequest orderPlaceRequest) {

        User loggedInUser = getLoggedInUser();
        String userId = orderPlaceRequest.getUserId();

        boolean isOwner = loggedInUser.getId().equals(userId);
        boolean isAdmin = loggedInUser.getRole() == Role.ROLE_ADMIN;

        if(!isOwner && !isAdmin)
        {
            throw new AccessDeniedException(
                    "Access denied. Only the cart owner or an administrator can place this order."
            );
        }

        User user = loggedInUser.getRole() == Role.ROLE_ADMIN ?
                userRepository.findById(userId).orElseThrow(()->new ResourceNotFound("User not found with id = "+userId))
                : loggedInUser;

        if(!isOwner && isAdmin && user.getRole() == Role.ROLE_ADMIN)
        {
            throw new AccessDeniedException(
                    "Access denied. You cannot place order for another."
            );
        }

        Cart cart = cartRepository.findByCreator(user).orElseThrow(() -> new ResourceNotFound(String.format("Cart Not Found for userId = %s", user.getId())));

        Restaurant restaurant = restaurantRepository.findById(cart.getRestaurant().getId())
                .orElseThrow(() -> new ResourceNotFound(String.format("Restaurant not found with id = %s", cart.getRestaurant().getId())));

        List<CartItems> cartItems = cart.getCartItems();

        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }
        // Convert cart items to order items
        Order order = new Order();
        order.setId(UUID.randomUUID().toString());
        order.setUser(user);
        order.setRestaurant(restaurant);

        String addressId = orderPlaceRequest.getAddress().getId();

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() ->
                        new ResourceNotFound("Address not found with id = " + addressId)
                );

        order.setAddress(address);
        order.setStatus(OrderStatus.PLACED);
        order.setOrderedAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        order.setPaymentMode(orderPlaceRequest.getPaymentMode());

        if(orderPlaceRequest.getPaymentMode().equals(PaymentMode.CASH_ON_DELIVERY))
        {
            order.setPaymentStatus(PaymentStatus.NOT_PAID);
        }
        else
        {
            order.setPaymentStatus(PaymentStatus.PAID);
        }

        final AtomicInteger totalAmount = new AtomicInteger(0);
        List<OrderItem> orderItems = cartItems.stream()
                .map(cartItem -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setId(UUID.randomUUID().toString());
                    orderItem.setOrder(order);
                    orderItem.setQuantity(cartItem.getQuantity());
                    orderItem.setFoodItems(cartItem.getFoodItems());
                    totalAmount.set(totalAmount.get()
                            +(cartItem.getTotalCartItemsPrice()));

                    return orderItem;
                })
                .collect(Collectors.toList());
        order.setTotalAmount(totalAmount.get());
        order.setOrderItemList(orderItems);
        order.setDeliveryTime(order.getOrderedAt().plusMinutes(30));

        orderRepository.save(order);
        // Clear user's cart
        cartService.clearCart(user.getId());
        return mapper.map(order, OrderDto.class);
    }

    @Override
    public List<OrderDto> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(order -> mapper.map(order, OrderDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDto> getOrdersByRestaurant(String restaurantId) {

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                                    .orElseThrow(()->new ResourceNotFound("Restaurant not found with id = "+restaurantId));

        validateRestaurantAccess(restaurant);

        List<Order> orders = orderRepository.findByRestaurantId(restaurantId);
        return orders.stream()
                .map(order -> mapper.map(order, OrderDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDto> getOrderByUserId(String userId) {

        User user = getLoggedInUser();

        boolean isOwner = user.getId().equals(userId);
        boolean isAdmin = user.getRole() == Role.ROLE_ADMIN;

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You are not authorized to perform this action.");
        }

        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
                .map(order -> mapper.map(order, OrderDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDto> getOrderByDeliveryBoy(String deliveryBoyId) {
        List<Order> orders = orderRepository.findByDeliveryBoyId(deliveryBoyId);
        return orders.stream()
                .map(order -> mapper.map(order, OrderDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public OrderDto trackOrder(String orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFound("Order not found with id: " + orderId));

        User loggedInUser = getLoggedInUser();

        boolean isOwner = loggedInUser.getId().equals(order.getUser().getId());
        boolean isAdmin = loggedInUser.getRole() == Role.ROLE_ADMIN;
        boolean isRestaurantAdmin =
                loggedInUser.getRole() == Role.ROLE_RESTAURANT_ADMIN &&
                        loggedInUser.getId().equals(order.getRestaurant().getOwner().getId());

        if (!isOwner && !isAdmin && !isRestaurantAdmin) {
            throw new AccessDeniedException(
                    "You are not authorized to track this order."
            );
        }

        return mapper.map(order, OrderDto.class);
    }

    @Override
    @Transactional
    public OrderDto cancelOrder(String orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFound("Order not found with id: " + orderId));

        User loggedInUser = getLoggedInUser();

        // 🔐 Authorization check
        boolean isOwner = loggedInUser.getId().equals(order.getUser().getId());
        boolean isAdmin = loggedInUser.getRole() == Role.ROLE_ADMIN;

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException(
                    "You are not authorized to cancel this order."
            );
        }

        // ❌ Order already delivered or cancelled
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Delivered orders cannot be cancelled.");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order is already cancelled.");
        }

        // ✅ Cancel order
        order.setStatus(OrderStatus.CANCELLED);

        // ⏱ Refund rule
        if (order.getPaymentStatus() == PaymentStatus.PAID) {

            boolean isAfterRefundWindow =
                    order.getOrderedAt()
                            .isBefore(LocalDateTime.now().minusMinutes(10));

            if (isAfterRefundWindow) {
                // ❌ No refund
                order.setPaymentStatus(PaymentStatus.NON_REFUNDABLE);
            } else {
                // ✅ Refund allowed
                order.setPaymentStatus(PaymentStatus.REFUNDED_INITIATED);
            }
        }


        Order savedOrder = orderRepository.save(order);

        return mapper.map(savedOrder, OrderDto.class);
    }


    @Override
    @Transactional
    public OrderDto updateOrderStatus(String orderId, OrderStatus orderStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFound("Order not found with id: " + orderId));
        order.setStatus(orderStatus);
        Order savedOrder = orderRepository.save(order);
        return mapper.map(savedOrder, OrderDto.class);
    }

}