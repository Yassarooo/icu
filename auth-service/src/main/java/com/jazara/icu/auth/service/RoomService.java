package com.jazara.icu.auth.service;

import com.jazara.icu.auth.domain.Branch;
import com.jazara.icu.auth.domain.Department;
import com.jazara.icu.auth.domain.Room;
import com.jazara.icu.auth.repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;

@Service
public class RoomService {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private UserService userService;

    public Room createRoom(Room room) {
        Branch b = departmentService.getBranchByDepId(room.getDep_id());
        if (b != null && (b.getOwner().getId().equals(userService.getLoggedUserId()) || userService.isAdmin())) {
            return roomRepository.save(room);
        }
        return null;
    }

    @Transactional
    public Room editRoom(Room room) {
        Branch b = departmentService.getBranchByDepId(room.getDep_id());
        if (b != null && (b.getOwner().getId().equals(userService.getLoggedUserId()) || userService.isAdmin())) {
            try {
                Room r = roomRepository.findById(room.getId());
                if (r == null)
                    return null;
                r.setName(room.getName());
                roomRepository.save(r);
                return r;
            } catch (ObjectOptimisticLockingFailureException e) {
                throw e;
            }
        }
        return null;
    }


    public Department getDepByRoomId(Long id) {
        Room r = roomRepository.findById(id);
        if (r != null) {
            Department d = departmentService.getDepartmentById(r.getDep_id());
            return d;
        }
        LOGGER.info("null");
        return null;
    }

    public ArrayList<Room> getRoomsByDepId(Long id) {
        Department d = departmentService.getDepartmentById(id);
        if (d != null && (d.getBranch().getOwner().getId().equals(userService.getLoggedUserId()) || userService.isAdmin())) {
            return roomRepository.findAllByDep_id(id);
        }
        return new ArrayList<Room>();
    }

    public Room getRoomById(Long id) {
        Room room = roomRepository.findById(id);
        if (room == null) {
            return null;
        }
        return room;
    }

    public Boolean deleteRoomById(Long id) {
        Room r = roomRepository.findById(id);
        if (r != null && (r.getDep().getBranch().getOwner().getId().equals(userService.getLoggedUserId()) || userService.isAdmin())) {
            roomRepository.delete(id);
            return true;
        }
        return false;
    }

    public void deleteAllRooms() {
        if (userService.isAdmin())
            roomRepository.deleteAll();
    }
}