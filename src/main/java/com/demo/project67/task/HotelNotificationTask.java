package com.demo.project67.task;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import com.demo.project67.domain.BookingEvent;
import com.demo.project67.repository.BookingEventRepository;
import com.demo.project67.service.HelperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class HotelNotificationTask {
    final BookingEventRepository bookingEventRepository;
    AtomicInteger attemptCounter = new AtomicInteger();

    /**
     * Retry in code
     */
    @Transactional
    @Retryable(value = RuntimeException.class,
            maxAttempts = 3,
            stateful = true,
            backoff = @Backoff(delay = 300, multiplier = 3))
    public Tasklet sendHotelNotificationTask() {
        return (contribution, chunkContext) -> {
            Long bookingId = (Long) chunkContext.getStepContext()
                    .getJobParameters()
                    .get("bookingId");
            log.info("Running sendHotelNotificationTaskTransactional, bookingId: {}, Attempt: {}", bookingId, attemptCounter.get());
            String customer = (String) chunkContext.getStepContext()
                    .getJobParameters()
                    .get("customer");
            bookingEventRepository.save(
                    BookingEvent.builder()
                            .event("Hotel Notification sent to customer " + customer + ", Attempt: " + attemptCounter.get())
                            .bookingId(bookingId)
                            .createdOn(LocalDateTime.now())
                            .build()
            );
            log.info("sendHotelNotificationTaskTransactional, bookingId: {}, Attempt: {} ", bookingId, attemptCounter.get());
            HelperUtil.delay();
            //Simulate error for first 2 attempts
            if (attemptCounter.incrementAndGet() < 3) {
                log.error("Failed to send hotel notification!");
                throw new RuntimeException("Failed to send hotel notification!");
            }
            log.info("Completed sendHotelNotificationTaskTransactional, bookingId: {}, Attempt: {}", bookingId, attemptCounter.get());
            return RepeatStatus.FINISHED;
        };
    }

}
