package com.university.booking.controller;

import com.university.booking.dto.RoomRequest;
import com.university.booking.dto.RoomScheduleResponse;
import com.university.booking.model.Room;
import com.university.booking.service.RoomService;
import com.university.booking.web.AdminOnly;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rooms")
@AllArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @AdminOnly
    @PostMapping
    public ResponseEntity<Room> create(@Valid @RequestBody RoomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.createRoom(request));
    }

    @GetMapping
    public ResponseEntity<List<Room>> getAll() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> getById(@PathVariable("id") String id) {
        return ResponseEntity.ok(roomService.getRoom(id));
    }

    @GetMapping("/{id}/schedule")
    public ResponseEntity<RoomScheduleResponse> getSchedule(@PathVariable("id") String id,
                                                            @RequestParam(name = "date", required = false) LocalDate date) {
        return ResponseEntity.ok(roomService.getSchedule(id, date != null ? date : LocalDate.now()));
    }

    @AdminOnly
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }
}
