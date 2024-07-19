package com.demo.project67.controller;

import java.time.LocalDateTime;

import com.demo.project67.domain.Booking;
import com.demo.project67.domain.BookingPayload;
import com.demo.project67.repository.BookingRepository;
import com.demo.project67.service.JobService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BookingController {

    final JobService jobService;
    final BookingRepository bookingRepository;

    @SneakyThrows
    @PostMapping("/book-travel")
    public ResponseEntity<String> startJob(@RequestBody BookingPayload payload) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonPayload = objectMapper.valueToTree(payload);
        Booking booking = bookingRepository.save(Booking.builder()
                .customer(payload.getCustomer())
                .createdOn(LocalDateTime.now())
                .build());
        jobService.startAsyncTravelJob(booking);
        return ResponseEntity.ok("Your BookingId: " + booking.getId());
    }

    @SneakyThrows
    @PostMapping("/employee-batch-job")
    public ResponseEntity<String> startJob() {
        jobService.startAsyncEmployeeJob();
        return ResponseEntity.ok("Started!");
    }

}
