
package com.hotel.hms.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotel.hms.Entity.Booking;
import com.hotel.hms.Entity.Customer;
import com.hotel.hms.Entity.Room;
import com.hotel.hms.Entity.User;
import com.hotel.hms.Exception.BadRequestException;
import com.hotel.hms.Exception.ConflictException;
import com.hotel.hms.Exception.ResourceNotFoundException;
import com.hotel.hms.Repository.BookingRepository;
import com.hotel.hms.Repository.CustomerRepository;
import com.hotel.hms.Repository.RoomRepository;
import com.hotel.hms.Repository.UserRepository;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    public BookingService(
            BookingRepository bookingRepository,
            RoomRepository roomRepository,
            CustomerRepository customerRepository,
            UserRepository userRepository) {

        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Booking getBookingById(Integer id) {
        return bookingRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Booking not found"));
    }

    // Create a booking for the authenticated account.
    @Transactional
    public Booking saveBooking(
            Booking booking,
            String authenticatedEmail) {

        if (authenticatedEmail == null
                || authenticatedEmail.isBlank()) {
            throw new BadRequestException(
                    "Authenticated user email is required");
        }

        String email = authenticatedEmail.trim();

        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new ResourceNotFoundException(
                    "Logged-in user was not found");
        }

        Customer customer = customerRepository
                .findByEmailIgnoreCase(email)
                .orElseGet(() -> {
                    Customer newCustomer = new Customer();
                    newCustomer.setName(user.getName());
                    newCustomer.setEmail(user.getEmail());
                    newCustomer.setPhone(null);
                    newCustomer.setAddress(null);

                    return customerRepository.save(newCustomer);
                });

        // Never trust a customer ID supplied by a guest.
        booking.setCustomer(customer);

        return saveBooking(booking);
    }

    // Create or update a booking containing multiple rooms.
    @Transactional
    public Booking saveBooking(Booking booking) {

        validateBooking(booking);

        if (booking.getCustomer() == null
                || booking.getCustomer().getId() == null) {
            throw new BadRequestException(
                    "Customer is required");
        }

        Customer customer = customerRepository
                .findById(booking.getCustomer().getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found"));

        if (booking.getRooms() == null
                || booking.getRooms().isEmpty()) {
            throw new BadRequestException(
                    "Select at least one room");
        }

        // Validate room IDs and reject duplicates.
        Set<Integer> roomIds = new HashSet<>();

        for (Room requestedRoom : booking.getRooms()) {
            if (requestedRoom == null
                    || requestedRoom.getId() == null) {
                throw new BadRequestException(
                        "Every selected room must have an ID");
            }

            if (!roomIds.add(requestedRoom.getId())) {
                throw new BadRequestException(
                        "The same room cannot be selected twice");
            }
        }

        Integer bookingId = booking.getId();

        Booking targetBooking;

        if (bookingId == null) {
            targetBooking = booking;
        } else {
            targetBooking = bookingRepository.findById(bookingId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Booking not found"));
        }

        List<Room> selectedRooms = new ArrayList<>();

        for (Integer roomId : roomIds) {

            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Room not found: " + roomId));

            boolean conflict =
                    bookingRepository.hasOverlappingBooking(
                            roomId,
                            booking.getCheckIn(),
                            booking.getCheckOut(),
                            bookingId);

            if (conflict) {
                throw new ConflictException(
                        "Room " + room.getRoomNumber()
                        + " is already booked for the selected dates");
            }

            selectedRooms.add(room);
        }

        targetBooking.setCustomer(customer);
        targetBooking.setRooms(selectedRooms);
        targetBooking.setCheckIn(booking.getCheckIn());
        targetBooking.setCheckOut(booking.getCheckOut());
        targetBooking.setAdults(booking.getAdults());
        targetBooking.setChildren(booking.getChildren());
        targetBooking.setStatus(booking.getStatus());

        return bookingRepository.save(targetBooking);
    }

    @Transactional
    public void deleteBooking(Integer id) {

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Booking not found"));

        // Deleting the booking also removes its booking_rooms
        // associations because the database has ON DELETE CASCADE.
        bookingRepository.delete(booking);
    }

    private void validateBooking(Booking booking) {

        if (booking == null) {
            throw new BadRequestException(
                    "Booking data is required");
        }

        LocalDate checkIn = booking.getCheckIn();
        LocalDate checkOut = booking.getCheckOut();

        if (checkIn == null || checkOut == null) {
            throw new BadRequestException(
                    "Check-in and check-out dates are required");
        }

        if (checkIn.isBefore(LocalDate.now())) {
            throw new BadRequestException(
                    "Check-in date cannot be in the past");
        }

        if (!checkOut.isAfter(checkIn)) {
            throw new BadRequestException(
                    "Check-out must be after check-in");
        }

        if (booking.getAdults() == null
                || booking.getAdults() < 1) {
            throw new BadRequestException(
                    "At least one adult is required");
        }

        if (booking.getChildren() == null
                || booking.getChildren() < 0) {
            throw new BadRequestException(
                    "Children cannot be negative");
        }

        if (booking.getStatus() == null
                || booking.getStatus().isBlank()) {
            booking.setStatus("CONFIRMED");
        }
    }
}
