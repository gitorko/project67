package com.demo.project67.task;

import java.time.LocalDateTime;

import com.demo.project67.domain.BookingEvent;
import com.demo.project67.repository.BookingEventRepository;
import com.demo.project67.service.HelperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookHotelTask {
    final BookingEventRepository bookingEventRepository;

    public Tasklet bookHotel() {
        return (contribution, chunkContext) -> {
            Long bookingId = (Long) chunkContext.getStepContext()
                    .getJobParameters()
                    .get("bookingId");
            log.info("Running bookHotel, bookingId: {}", bookingId);
            String customer = (String) chunkContext.getStepContext()
                    .getJobParameters()
                    .get("customer");
            bookingEventRepository.save(
                    BookingEvent.builder()
                            .event("Hotel Booked for customer " + customer)
                            .bookingId(bookingId)
                            .createdOn(LocalDateTime.now())
                            .build()
            );
            HelperUtil.delay();
            log.info("Completed bookHotel, bookingId: {}", bookingId);
            return RepeatStatus.FINISHED;
        };
    }
}
