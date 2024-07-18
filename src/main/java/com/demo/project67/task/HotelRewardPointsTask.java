package com.demo.project67.task;

import java.time.LocalDateTime;

import com.demo.project67.domain.BookingEvent;
import com.demo.project67.repository.BookingEventRepository;
import com.demo.project67.service.HelperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class HotelRewardPointsTask {
    final BookingEventRepository bookingEventRepository;

    public Tasklet creditHotelRewardPoints() {
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                Long bookingId = (Long) chunkContext.getStepContext()
                        .getJobParameters()
                        .get("bookingId");
                log.info("Running creditHotelRewardPoints, bookingId: {}", bookingId);
                String customer = (String) chunkContext.getStepContext()
                        .getJobParameters()
                        .get("customer");
                bookingEventRepository.save(
                        BookingEvent.builder()
                                .event("Hotel Reward points added for customer " + customer)
                                .bookingId(bookingId)
                                .createdOn(LocalDateTime.now())
                                .build()
                );
                HelperUtil.delay();
                log.info("Completed creditHotelRewardPoints, bookingId: {}", bookingId);
                return RepeatStatus.FINISHED;
            }
        };
    }
}
