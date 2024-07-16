package com.demo.project67.repository;

import com.demo.project67.domain.BookingEvent;
import org.springframework.data.repository.CrudRepository;

public interface BookingEventRepository extends CrudRepository<BookingEvent, Long> {
}
