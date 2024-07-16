package com.demo.project67.repository;

import com.demo.project67.domain.Booking;
import org.springframework.data.repository.CrudRepository;

public interface BookingRepository extends CrudRepository<Booking, Long> {
}
