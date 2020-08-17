package com.demo.project67.optimistic;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.transaction.Transactional;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.demo.project67.optimistic")
public class OptimisticMain {

    @Autowired
    MyService myService;

    ExecutorService pool = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        SpringApplication.run(OptimisticMain.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            myService.seedData();
            pool.submit(() -> {
                Boolean status = myService.bookSeat(1, "Joe");
                System.out.println("Booking for Joe success: "+ status);
            });
            pool.submit(() -> {
                Boolean status = myService.bookSeat(1, "Jack");
                System.out.println("Booking for Jack success: "+ status);
            });
            pool.shutdown();
            pool.awaitTermination(60, TimeUnit.SECONDS);
            myService.showData();
        };
    }
}

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Integer seatNumber;
    @Temporal(TemporalType.DATE)
    private Date onDay;
    private String bookedBy;
    @Version
    private int version;
}

@Repository
interface TicketRepository extends JpaRepository<Ticket, Long> {
    @Transactional
    Ticket findBySeatNumberAndOnDay(Integer seatNumber, Date onDay);
}

@Component
class MyService {
    @Autowired
    TicketRepository ticketRepository;

    public Boolean bookSeat(Integer seatNumber, String personName) {
        try {
            System.out.println("Booking seat: " + seatNumber + " By: " + personName);
            Ticket seat = ticketRepository.findBySeatNumberAndOnDay(seatNumber, new Date());
            if (seat.getBookedBy() == null) {
                seat.setBookedBy(personName);
                ticketRepository.save(seat);
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return false;
    }

    public void showData() {
        ticketRepository.findAll().forEach(e -> {
            System.out.println(e);
        });
    }

    public void seedData() {
        ticketRepository.deleteAll();
        for (int i = 1; i <= 3; i++) {
            ticketRepository.save(Ticket.builder().seatNumber(i).onDay(new Date()).build());
        }
    }
}
