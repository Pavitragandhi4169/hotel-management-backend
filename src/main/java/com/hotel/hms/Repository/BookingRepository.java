
package com.hotel.hms.Repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hotel.hms.Entity.Booking;

public interface BookingRepository
        extends JpaRepository<Booking, Integer> {

    // Check whether a room is referenced by any booking.
    boolean existsByRooms_Id(Integer roomId);

    // Check whether a room is booked for overlapping dates.
    @Query("""
        SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
        FROM Booking b
        JOIN b.rooms r
        WHERE r.id = :roomId
          AND UPPER(b.status) <> 'CANCELLED'
          AND b.checkIn < :checkOut
          AND b.checkOut > :checkIn
          AND (
              :excludeBookingId IS NULL
              OR b.id <> :excludeBookingId
          )
        """)
    boolean hasOverlappingBooking(
            @Param("roomId") Integer roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("excludeBookingId") Integer excludeBookingId);
}
