package com.substring.foodies.scheduler;

import com.substring.foodies.entity.Restaurant;
import com.substring.foodies.repository.RestaurantRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Component
@EnableScheduling
public class RestaurantStatusScheduler {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Scheduled(fixedRate = 5 * 60 * 1000)
    @Transactional
    public void updateRestaurantOpenStatus() {

        LocalTime now = LocalTime.now(ZoneId.of("Asia/Kolkata"));

        List<Restaurant> restaurants = restaurantRepository.findAll();

        for (Restaurant r : restaurants) {

            if (!r.isActive() || r.getOpenTime() == null || r.getCloseTime() == null) {
                r.setOpen(false);
                continue;
            }

            LocalTime open = r.getOpenTime();
            LocalTime close = r.getCloseTime();

            boolean shouldBeOpen;

            if (open.isBefore(close)) {
                // Same-day restaurant (07:30 → 10:00)
                shouldBeOpen = !now.isBefore(open) && now.isBefore(close);
            } else {
                // Overnight restaurant (18:00 → 01:00)
                shouldBeOpen = !now.isBefore(open) || now.isBefore(close);
            }

            r.setOpen(shouldBeOpen);
        }
    }

}
