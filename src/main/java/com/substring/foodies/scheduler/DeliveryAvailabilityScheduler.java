package com.substring.foodies.scheduler;

import com.substring.foodies.dto.enums.Role;
import com.substring.foodies.entity.User;
import com.substring.foodies.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DeliveryAvailabilityScheduler {

    private final UserRepository userRepository;
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void freeDeliveryBoys() {

        LocalDateTime now = LocalDateTime.now(IST);

        List<User> busyDeliveryBoys =
                userRepository.findByRoleAndIsAvailableFalse(
                        Role.ROLE_DELIVERY_BOY
                );

        for (User user : busyDeliveryBoys) {

            if (user.getUpdatedAt()
                    .isBefore(now.minusMinutes(10))) {

                user.setAvailable(true);
            }
        }
    }
}
