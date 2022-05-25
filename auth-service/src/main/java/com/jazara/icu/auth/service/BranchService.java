package com.jazara.icu.auth.service;

import com.jazara.icu.auth.domain.Branch;
import com.jazara.icu.auth.repository.BranchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;

@Service
public class BranchService {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private UserService userService;

    public Branch createBranch(Branch branch) {
        branch.setOwner(userService.getUserByID(userService.getLoggedUserId()));
        branch = branchRepository.save(branch);
        return branch;
    }

    @Transactional
    public Branch editBranch(Branch branch) {
        Branch b = branchRepository.findById(branch.getId());
        if (b != null && ((b.getOwner().getId().equals(userService.getLoggedUserId())) || userService.isAdmin())) {
            try {
                b.setName(branch.getName().trim());
                b.setLocation(branch.getLocation());
                branchRepository.save(b);
                return b;
            } catch (ObjectOptimisticLockingFailureException e) {
                throw e;
            }
        }
        return null;
    }

    public ArrayList<Branch> getBranchesByOwnerId(Long id) {
        if (id.equals(userService.getLoggedUserId()))
            return branchRepository.findAllByOwner_Id(id);
        return new ArrayList<Branch>();
    }

    public Branch getBranchById(Long id) {
        Branch branch = branchRepository.findById(id);
        if (branch == null) {
            return null;
        }
        return branch;
    }

    public Boolean deleteBranchById(Long id) {
        Branch b = branchRepository.findById(id);
        if (b != null && (b.getOwner().getId().equals(userService.getLoggedUserId()) || userService.isAdmin())) {
            branchRepository.delete(id);
            return true;
        }
        return false;
    }

    public void deleteAllBranches() {
        if (userService.isAdmin())
            branchRepository.deleteAll();
    }
}