package cn.zhangxd.auth.service;

import cn.zhangxd.auth.domain.Role;
import cn.zhangxd.auth.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service(value = "roleService")
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
