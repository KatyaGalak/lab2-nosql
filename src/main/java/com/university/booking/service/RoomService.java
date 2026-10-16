package com.university.booking.service;

import com.university.booking.dto.RoomRequest;
import com.university.booking.dto.RoomScheduleResponse;
import com.university.booking.enums.BookingStatus;
import com.university.booking.exception.ResourceNotFoundException;
import com.university.booking.model.Room;
import com.university.booking.repository.BookingRepository;
import com.university.booking.repository.RoomRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    @CacheEvict(value = "rooms", allEntries = true)
    public Room createRoom(RoomRequest request) {
        Room room = Room.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .type(request.getType())
                .capacity(request.getCapacity())
                .location(request.getLocation())
                .teacherOnly(request.isTeacherOnly())
                .build();

        return roomRepository.save(room);
    }

    public Room getRoom(String id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));
    }

    public RoomScheduleResponse getSchedule(String roomId, LocalDate date) {
        Room room = getRoom(roomId);
        List<RoomScheduleResponse.BusySlot> busy = bookingRepository
                .findByRoomIdAndEventDateAndStatusOrderByStartTimeAsc(room.getId(), date, BookingStatus.APPROVED)
                .stream()
                .map(b -> new RoomScheduleResponse.BusySlot(b.getStartTime(), b.getEndTime()))
                .toList();
        return new RoomScheduleResponse(room.getId(), date, busy);
    }

    @Cacheable(value = "rooms")
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @CacheEvict(value = "rooms", allEntries = true)
    public void deleteRoom(String id) {
        roomRepository.deleteById(id);
    }
}
