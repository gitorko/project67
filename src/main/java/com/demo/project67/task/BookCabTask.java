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
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookCabTask {

    final BookingEventRepository bookingEventRepository;

    @Transactional
    public Tasklet bookCab() {
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                Long bookingId = (Long) chunkContext.getStepContext()
                        .getJobParameters()
                        .get("bookingId");
                log.info("Running bookCab, bookingId: {}", bookingId);
                String customer = (String) chunkContext.getStepContext()
                        .getJobParameters()
                        .get("customer");
                bookingEventRepository.save(
                        BookingEvent.builder()
                                .event("Cab Booked for customer " + customer)
                                .bookingId(bookingId)
                                .createdOn(LocalDateTime.now())
                                .build()
                );
                HelperUtil.delay();
                log.info("Completed bookCab, bookingId: {}", bookingId);
                return RepeatStatus.FINISHED;
            }
        };
    }
}
