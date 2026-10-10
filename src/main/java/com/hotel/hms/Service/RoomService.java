
package com.hotel.hms.Service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotel.hms.Entity.Room;
import com.hotel.hms.Exception.BadRequestException;
import com.hotel.hms.Exception.ConflictException;
import com.hotel.hms.Exception.ResourceNotFoundException;
import com.hotel.hms.Repository.BookingRepository;
import com.hotel.hms.Repository.RoomRepository;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    public RoomService(
            RoomRepository roomRepository,
            BookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional(readOnly = true)
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Room getRoomById(Integer id) {
        return roomRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Room not found with ID: " + id));
    }

    @Transactional
    public Room saveRoom(Room room) {
        if (room == null) {
            throw new BadRequestException(
                    "Room data is required");
        }

        if (room.getRoomNumber() == null) {
            throw new BadRequestException(
                    "Room number is required");
        }

        if (room.getRoomType() == null
                || room.getRoomType().isBlank()) {
            throw new BadRequestException(
                    "Room type is required");
        }

        if (room.getPrice() == null
                || room.getPrice() <= 0) {
            throw new BadRequestException(
                    "Room price must be greater than zero");
        }

        if (room.getStatus() == null
                || room.getStatus().isBlank()) {
            room.setStatus("AVAILABLE");
        }

        return roomRepository.save(room);
    }

    @Transactional
    public Room updateRoom(Integer id, Room room) {
        if (room == null) {
            throw new BadRequestException(
                    "Room data is required");
        }

        Room existingRoom = roomRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Room not found with ID: " + id));

        if (room.getRoomNumber() == null) {
            throw new BadRequestException(
                    "Room number is required");
        }

        if (room.getRoomType() == null
                || room.getRoomType().isBlank()) {
            throw new BadRequestException(
                    "Room type is required");
        }

        if (room.getPrice() == null
                || room.getPrice() <= 0) {
            throw new BadRequestException(
                    "Room price must be greater than zero");
        }

        existingRoom.setRoomNumber(room.getRoomNumber());
        existingRoom.setRoomType(room.getRoomType());
        existingRoom.setPrice(room.getPrice());

        if (room.getStatus() != null
                && !room.getStatus().isBlank()) {
            existingRoom.setStatus(room.getStatus());
        }

        return roomRepository.save(existingRoom);
    }

    @Transactional
    public void deleteRoom(Integer id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Room not found with ID: " + id));

        // Prevent deleting rooms associated with bookings.
        if (bookingRepository.existsByRooms_Id(id)) {
            throw new ConflictException(
                    "Cannot delete this room because it has existing bookings.");
        }

        roomRepository.delete(room);
    }
}
