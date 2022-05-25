package com.jazara.icu.auth.repository;

import com.jazara.icu.auth.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Department findById(Long id);
    ArrayList<Department> findAllByBranch_id(Long id);
}