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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class FlightNotificationTask {
    final BookingEventRepository bookingEventRepository;
    AtomicInteger attemptCounter = new AtomicInteger();

    /**
     * Retry in flow
     */
    @Transactional
    public Tasklet sendingFlightNotificationTask() {
        return (contribution, chunkContext) -> {
            Long bookingId = (Long) chunkContext.getStepContext()
                    .getJobParameters()
                    .get("bookingId");
            log.info("Running sendingFlightNotificationTask, bookingId: {}, Attempt: {}", bookingId, attemptCounter.get());
            String customer = (String) chunkContext.getStepContext()
                    .getJobParameters()
                    .get("customer");
            bookingEventRepository.save(
                    BookingEvent.builder()
                            .event("Flight Notification Sent to customer " + customer + ", Attempt: " + attemptCounter.get())
                            .bookingId(bookingId)
                            .createdOn(LocalDateTime.now())
                            .build()
            );
            log.info("sendingFlightNotificationTask,  bookingId: {}, Attempt: {}", bookingId, attemptCounter.get());
            HelperUtil.delay(10);
            //Simulate error for first 2 attempts
            if (attemptCounter.incrementAndGet() < 3) {
                log.error("Failed to send flight notification!");
                throw new RuntimeException("Failed to send flight notification!");
            }
            log.info("Completed sendingFlightNotificationTask, bookingId: {}, Attempt: {}", bookingId, attemptCounter.get());
            return RepeatStatus.FINISHED;
        };
    }
}
