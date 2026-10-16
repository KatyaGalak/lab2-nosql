package com.university.booking.repository;

import com.university.booking.enums.BookingStatus;
import com.university.booking.model.Booking;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, String> {

    List<Booking> findByPersonIdOrderByCreatedAtAsc(String personId);

    List<Booking> findAllByOrderByCreatedAtAsc();

    List<Booking> findByRoomIdAndEventDateAndStatusOrderByStartTimeAsc(String roomId, LocalDate eventDate,
                                                                       BookingStatus status);

    @Query("""
            select b from Booking b
            where b.roomId = :roomId
              and b.eventDate = :date
              and b.status = :status
              and b.startTime < :endTime
              and b.endTime > :startTime
            order by b.startTime
            """)
    List<Booking> findOverlapping(@Param("roomId") String roomId,
                                  @Param("date") LocalDate date,
                                  @Param("startTime") LocalTime startTime,
                                  @Param("endTime") LocalTime endTime,
                                  @Param("status") BookingStatus status);
}
