package com.jazara.icu.auth.service;

import com.jazara.icu.auth.domain.Cam;
import com.jazara.icu.auth.domain.Department;
import com.jazara.icu.auth.domain.Room;
import com.jazara.icu.auth.repository.CamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;

@Service
public class CamService {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Autowired
    private CamRepository camRepository;
    @Autowired
    private RoomService roomService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private BranchService branchService;
    @Autowired
    private UserService userService;

    public Cam createCam(Cam cam) {
        Room r = roomService.getRoomById(cam.getRoom_id());
        if (r == null)
            return null;
        Department d = roomService.getDepByRoomId(cam.getRoom_id());
        if (d == null)
            return null;
        if (d.getBranch() != null && (d.getBranch().getOwner().getId().equals(userService.getLoggedUserId()) || userService.isAdmin())) {
            cam.setRoom(r);
            return camRepository.save(cam);
        }
        return null;
    }

    @Transactional
    public Cam editCam(Cam cam) {
        Room r = roomService.getRoomById(cam.getRoom_id());
        if (r == null)
            return null;
        Department d = roomService.getDepByRoomId(cam.getRoom_id());
        if (d == null)
            return null;
        if (d.getBranch() != null && d.getBranch().getOwner().getId().equals(userService.getLoggedUserId()) || userService.isAdmin()) {
            try {
                Cam c = camRepository.findById(cam.getId());
                if (c == null)
                    return null;
                c.setName(cam.getName());
                c.setUrl(cam.getUrl());
                c.setRoom_id(cam.getRoom_id());
                c.setRoom(r);
                camRepository.save(c);
                return c;
            } catch (ObjectOptimisticLockingFailureException e) {
                throw e;
            }
        }
        return null;
    }

    public ArrayList<Cam> getCamsByRoomId(Long id) {
        Room r = roomService.getRoomById(id);
        if (r == null)
            return null;
        Department d = roomService.getDepByRoomId(id);
        if (d == null)
            return null;
        if (d.getBranch() != null && (d.getBranch().getOwner().getId().equals(userService.getLoggedUserId()) || userService.isAdmin())) {
            return camRepository.findAllByRoom_id(id);
        }
        return new ArrayList<Cam>();
    }

    public Cam getCamById(Long id) {
        Cam c = camRepository.findById(id);
        if (c == null) {
            return null;
        }
        return c;
    }

    public Boolean deleteCamById(Long id) {
        Cam c = camRepository.findById(id);
        if (c != null && (c.getRoom().getDep().getBranch().getOwner().getId().equals(userService.getLoggedUserId()) || userService.isAdmin())) {
            camRepository.delete(id);
            return true;
        }
        return false;
    }

    public void deleteAllCams() {
        if (userService.isAdmin())
            camRepository.deleteAll();
    }
}