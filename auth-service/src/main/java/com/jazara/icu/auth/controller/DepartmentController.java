package com.jazara.icu.auth.controller;

import com.jazara.icu.auth.domain.Department;
import com.jazara.icu.auth.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RequestMapping("/department")
@RestController
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @PostMapping(value = "/add")
    public ResponseEntity<String> createDep(@RequestBody Department dep) {
        final Department d = departmentService.createDepartment(dep);
        if (d == null) {
            return new ResponseEntity<String>("cannot", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<String>("success", HttpStatus.OK);
    }

    @PutMapping(value = "/edit/{id}")
    public ResponseEntity<String> editDep(@PathVariable Long id, @RequestBody Department dep) {
        Department d = departmentService.editDepartment(dep);
        if (d == null) {
            return new ResponseEntity<String>("cannot", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<String>("success", HttpStatus.OK);
    }

    @GetMapping(value = "/all/{id}")
    public ResponseEntity<ArrayList<Department>> getDepartmentsByBranchID(@PathVariable Long id) {
        final ArrayList<Department> deps = departmentService.getDepartmentsByBranchId(id);
        return new ResponseEntity<ArrayList<Department>>(deps, HttpStatus.OK);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Department> getDep(@PathVariable Long id) {
        final Department d = departmentService.getDepartmentById(id);
        if (d == null) {
            return new ResponseEntity<Department>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<Department>(d, HttpStatus.OK);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<String> deleteDep(@PathVariable Long id) {
        if (departmentService.deleteDepartmentById(id))
            return new ResponseEntity<String>("success", HttpStatus.OK);
        return new ResponseEntity<String>("cannot", HttpStatus.UNAUTHORIZED);
    }
}
