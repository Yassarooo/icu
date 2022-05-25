package com.jazara.icu.auth.controller;

import com.jazara.icu.auth.domain.Branch;
import com.jazara.icu.auth.service.BranchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RequestMapping("/branch")
@RestController
public class BranchController {

    @Autowired
    private BranchService branchService;

    @PostMapping(value = "/add")
    public ResponseEntity<String> createBranch(@RequestBody Branch branch) {
        final Branch b = branchService.createBranch(branch);
        if (b == null) {
            return new ResponseEntity<String>("failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<String>("success", HttpStatus.OK);
    }

    @PutMapping(value = "/edit/{id}")
    public ResponseEntity<String> editBranch(@PathVariable Long id, @RequestBody Branch branch) {
        Branch b = branchService.editBranch(branch);
        if (b == null) {
            return new ResponseEntity<String>("cannot", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<String>("success", HttpStatus.OK);
    }

    @GetMapping(value = "/user/{id}")
    public ResponseEntity<ArrayList<Branch>> getBranchesByOwnerId(@PathVariable Long id) {
        final ArrayList<Branch> branches = branchService.getBranchesByOwnerId(id);
        return new ResponseEntity<ArrayList<Branch>>(branches, HttpStatus.OK);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Branch> getBranch(@PathVariable Long id) {
        final Branch b = branchService.getBranchById(id);
        if (b == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<Branch>(b, HttpStatus.OK);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<String> deleteBranch(@PathVariable Long id) {
        if (branchService.deleteBranchById(id))
            return new ResponseEntity<String>("success", HttpStatus.OK);
        return new ResponseEntity<String>("cannot", HttpStatus.UNAUTHORIZED);
    }
}
