package com.jazara.icu.auth.service;


import com.jazara.icu.auth.domain.Branch;
import com.jazara.icu.auth.domain.Department;
import com.jazara.icu.auth.repository.DepartmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;

@Service
public class DepartmentService {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private BranchService branchService;
    @Autowired
    private UserService userService;

    public Department createDepartment(Department dep) {
        Branch b = branchService.getBranchById(dep.getBranch_id());
        if (b != null && (b.getOwner().getId().equals(userService.getLoggedUserId()) || userService.isAdmin())) {
            dep.setBranch(b);
            return departmentRepository.save(dep);
        }
        return null;
    }

    @Transactional
    public Department editDepartment(Department dep) {
        Branch b = branchService.getBranchById(dep.getBranch_id());
        if (b != null && b.getOwner().getId().equals(userService.getLoggedUserId()) || userService.isAdmin()) {
            try {
                Department d = departmentRepository.findById(dep.getId());
                if (d == null)
                    return null;
                d.setName(dep.getName());
                d.setLocation(dep.getLocation());
                d.setBranch_id(b.getId());
                d.setBranch(b);
                departmentRepository.save(d);
                return d;
            } catch (ObjectOptimisticLockingFailureException e) {
                throw e;
            }
        }
        return null;
    }

    public Branch getBranchByDepId(Long id) {
        Department d = departmentRepository.findById(id);
        if (d != null && (d.getBranch().getOwner().getId().equals(userService.getLoggedUserId()) || userService.isAdmin())) {
            return d.getBranch();
        }
        return null;
    }

    public ArrayList<Department> getDepartmentsByBranchId(Long id) {
        Branch b = branchService.getBranchById(id);
        if (b != null && (b.getOwner().getId().equals(userService.getLoggedUserId()) || userService.isAdmin())) {
            return departmentRepository.findAllByBranch_id(id);
        }
        return new ArrayList<Department>();
    }

    public Department getDepartmentById(Long id) {
        Department dep = departmentRepository.findById(id);
        if (dep == null) {
            return null;
        }
        return dep;
    }

    public Boolean deleteDepartmentById(Long id) {
        Department d = departmentRepository.findById(id);
        if (d != null && (d.getBranch().getOwner().getId().equals(userService.getLoggedUserId()) || userService.isAdmin())) {
            departmentRepository.delete(id);
            return true;
        }
        return false;
    }

    public void deleteAllDepartments() {
        if (userService.isAdmin())
            departmentRepository.deleteAll();
    }
}