package com.jazara.icu.auth.controller;


import com.jazara.icu.auth.domain.Room;
import com.jazara.icu.auth.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RequestMapping("/room")
@RestController
public class RoomController {

    @Autowired
    private RoomService roomService;

    @PostMapping(value = "/add")
    public ResponseEntity<String> createRoom(@RequestBody Room room) {
        final Room r = roomService.createRoom(room);
        if (r == null) {
            return new ResponseEntity<String>("cannot", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<String>("success", HttpStatus.OK);
    }

    @PutMapping(value = "/edit/{id}")
    public ResponseEntity<String> editRoom(@PathVariable Long id, @RequestBody Room room) {
        Room r = roomService.editRoom(room);
        if (r == null) {
            return new ResponseEntity<String>("cannot", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<String>("success", HttpStatus.OK);
    }

    @GetMapping(value = "/all/{id}")
    public ResponseEntity<ArrayList<Room>> getRoomsByDepID(@PathVariable Long id) {
        final ArrayList<Room> rooms = roomService.getRoomsByDepId(id);
        return new ResponseEntity<ArrayList<Room>>(rooms, HttpStatus.OK);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Room> getRoom(@PathVariable Long id) {
        final Room r = roomService.getRoomById(id);
        if (r == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<Room>(r, HttpStatus.OK);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<String> deleteRoom(@PathVariable Long id) {
        if (roomService.deleteRoomById(id))
            return new ResponseEntity<String>("success", HttpStatus.OK);
        return new ResponseEntity<String>("cannot", HttpStatus.UNAUTHORIZED);
    }
}
