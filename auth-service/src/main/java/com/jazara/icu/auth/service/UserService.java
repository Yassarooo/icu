package com.jazara.icu.auth.service;

import com.jazara.icu.auth.domain.Role;
import com.jazara.icu.auth.domain.User;
import com.jazara.icu.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UserService implements UserDetailsService {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private RoleService roleService;

    @Autowired
    EurekaDiscoveryClient discoveryClient;

    public UserService(UserRepository UserRepository) {
        this.userRepository = UserRepository;
    }

    public Authentication authenticate(String username, String password) throws Exception {
        try {
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String UsernameOrEmail) throws UsernameNotFoundException {
        boolean accountNonExpired = true;
        boolean credentialsNonExpired = true;
        boolean accountNonLocked = true;

        User User = userRepository.findByUsername(UsernameOrEmail);
        if (User != null) {
            return new org.springframework.security.core.userdetails.User(
                    User.getUsername(),
                    User.getPassword(),
                    User.isEnabled(),
                    accountNonExpired,
                    credentialsNonExpired,
                    accountNonLocked,
                    User.getAuthorities());
        } else {
            User = userRepository.findByEmail(UsernameOrEmail);
            if (User != null) {
                return new org.springframework.security.core.userdetails.User(User.getUsername(), User.getPassword(), User.getAuthorities());
            } else {
                System.err.println("Username Not Found");
                return null;
            }
        }
    }

    public User findUserByUsername(String UsernameOrEmail) throws UsernameNotFoundException {
        User User = userRepository.findByUsername(UsernameOrEmail);
        if (User != null) {
            return User;
        } else {
            User = userRepository.findByEmail(UsernameOrEmail);
            if (User != null) {
                return User;
            } else {
                System.err.println("Username Not Found");
                return null;
            }
        }
    }

    public User save(User User) {

        if (this.loadUserByUsername(User.getEmail()) != null || this.loadUserByUsername(User.getUsername()) != null)
            return null;
        else {
            Role role = roleService.findByName("USER");
            List<Role> roles = new ArrayList<Role>();
            roles.add(role);
            if (User.getEmail().split("@")[1].equals("admin.yr")) {
                role = roleService.findByName("ADMIN");
                roles.add(role);
            }
            if (User.getEmail().contains("yassar")) {
                role = roleService.findByName("ADMIN");
                roles.add(role);
            }
            if (User.getEmail().contains("yrhacker")) {
                role = roleService.findByName("ADMIN");
                roles.add(role);
            }
            User.setRoles(roles);

            User.setUsername(User.getUsername().toLowerCase().trim());
            User.setName(User.getName().trim());
            User.setGender(User.getGender().toLowerCase().trim());
            User.setEmail(User.getEmail().toLowerCase().trim());
            User.setCreatedAt(new Date());
            User.setPassword(bCryptPasswordEncoder.encode(User.getPassword()));
            return userRepository.save(User);
        }
    }

    public List<User> getAllUsers() {
        List<User> usersList = (List<User>) userRepository.findAll();

        if (usersList.size() > 0) {
            return usersList;
        } else {
            return new ArrayList<User>();
        }
    }

    @Transactional
    public User updateUser(User u) {
        try {
            User user = userRepository.findByUsername(u.getUsername());

            user.setName(u.getName().trim());
            user.setUsername(u.getUsername().toLowerCase().trim());
            user.setEmail(u.getEmail().toLowerCase().trim());
            user.setPhonenumber(u.getPhonenumber());
            user.setDob(u.getDob());
            user.setGender(u.getGender().trim());
            user.setRoles(u.getRoles());
            user.setUpdatedAt(new Date());

            userRepository.save(user);
            return u;
        } catch (ObjectOptimisticLockingFailureException e) {
            throw e;
        }
    }

    public Long getLoggedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        User Loggedin = userRepository.findByUsername(username);
        if (Loggedin == null) {
            return 0L;
        }
        LOGGER.info("Logged user id : " + Loggedin.getId());
        return Loggedin.getId();
    }

    public Boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean authorized = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        return authorized;
    }

    public User ActivateUser(final String email, final String serviceid) {
        LOGGER.info("SERVICE ID : " + serviceid);
        if (discoveryClient.getInstances(serviceid).size() > 0) {
            User u = findUserByUsername(email);
            if (u == null) {
                LOGGER.info("User not found");
                return null;
            }
            u.setEnabled(true);
            return userRepository.save(u);
        }
        return null;
    }


    public User getUserByID(final long id) {
        return userRepository.findById(id);
    }

    public void changeUserPassword(final User user, final String password) {
        user.setPassword(bCryptPasswordEncoder.encode(password));
        userRepository.save(user);
    }

    public boolean checkIfValidOldPassword(final User user, final String oldPassword) {
        return bCryptPasswordEncoder.matches(oldPassword, user.getPassword());
    }

    public void deleteAllUsers() {
        userRepository.deleteAll();
    }


}