package com.university.booking.repository;

import com.university.booking.enums.BookingStatus;
import com.university.booking.model.Booking;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface BookingRepository extends MongoRepository<Booking, String> {

    List<Booking> findByPersonIdOrderByCreatedAtAsc(String personId);

    List<Booking> findAllByOrderByCreatedAtAsc();

    List<Booking> findByRoomIdAndEventDateAndStatusOrderByStartTimeAsc(String roomId, LocalDate eventDate,
                                                                       BookingStatus status);

    boolean existsByOrganizationId(String organizationId);

    @Query(value = """
            { 'roomId': ?0, 'eventDate': ?1, 'status': ?4,
              'startTime': { $lt: ?3 }, 'endTime': { $gt: ?2 } }
            """, sort = "{ 'startTime': 1 }")
    List<Booking> findOverlapping(String roomId, LocalDate date, LocalTime startTime, LocalTime endTime,
                                  BookingStatus status);
}