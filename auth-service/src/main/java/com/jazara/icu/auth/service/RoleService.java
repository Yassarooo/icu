package com.jazara.icu.auth.service;

import com.jazara.icu.auth.domain.Role;
import com.jazara.icu.auth.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    public Role findByName(String name) {
        Role role = roleRepository.findByName(name);
        return role;
    }

    public Role CreateRole(Role role) {
        role = roleRepository.save(role);
        return role;
    }

    public void deleteAllRoles() {
        roleRepository.deleteAll();
    }
}
