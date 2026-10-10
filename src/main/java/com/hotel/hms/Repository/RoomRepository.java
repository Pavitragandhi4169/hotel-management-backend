
package com.hotel.hms.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotel.hms.Entity.Room;

public interface RoomRepository
        extends JpaRepository<Room, Integer> {
}
