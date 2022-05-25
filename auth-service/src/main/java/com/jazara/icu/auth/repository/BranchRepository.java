package com.jazara.icu.auth.repository;

import com.jazara.icu.auth.domain.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    Branch findById(Long id);
    ArrayList<Branch> findAllByOwner_Id(Long id);
}