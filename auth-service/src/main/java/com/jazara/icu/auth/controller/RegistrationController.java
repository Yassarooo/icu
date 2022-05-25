package com.jazara.icu.auth.controller;

import com.jazara.icu.auth.domain.User;
import com.jazara.icu.auth.service.JwtTokenUtil;
import com.jazara.icu.auth.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/api")
@RestController
public class RegistrationController {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserService userService;


    public RegistrationController() {
        super();
    }

    @PostMapping(value = "/login")
    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password) throws Exception {

        userService.authenticate(username, password);

        final UserDetails userDetails = userService.loadUserByUsername(username);
        User appUser = userService.findUserByUsername(username);
        if (appUser == null) {
            return new ResponseEntity<String>("There is no account with given username or email", HttpStatus.UNAUTHORIZED);
        }
        if (!appUser.isEnabled()) {
            return new ResponseEntity<String>("Please Activate Your Account", HttpStatus.UNAUTHORIZED);
        } else {
            final String token = jwtTokenUtil.generateToken(userDetails);

            Map<String, Object> tokenMap = new HashMap<String, Object>();

            if (token != null) {
                tokenMap.put("token", token);
                tokenMap.put("user", appUser);

                return new ResponseEntity<Map<String, Object>>(tokenMap, HttpStatus.OK);
            } else {
                tokenMap.put("token", null);
                return new ResponseEntity<Map<String, Object>>(tokenMap, HttpStatus.UNAUTHORIZED);
            }
        }
    }

    @GetMapping(value = "/checkjwttoken")
    public ResponseEntity<?> checktoken(@RequestParam String token) {
        try {
            if (jwtTokenUtil.isTokenExpired(token))
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            else {
                return new ResponseEntity<>(HttpStatus.OK);
            }
        } catch (ExpiredJwtException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

    }

    //get logged in user
    @GetMapping("/user")
    public User user() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String loggedUsername = auth.getName();
        return userService.findUserByUsername(loggedUsername);
    }

    //get logged in user id
    @GetMapping("/current")
    public Long getLoggedUserID() {
        return userService.getLoggedUserId();
    }

    // Registration
    @PostMapping("/register")
    public ResponseEntity<String> registerUserAccount(@RequestBody User accountDto) {
        LOGGER.debug("Registering user account with information: {}", accountDto);

        final User registered = userService.save(accountDto);
        if (registered == null) {
            return new ResponseEntity<String>("failed", HttpStatus.CONFLICT);
        }
        LOGGER.info("registered account : " + registered.getEmail());
        return new ResponseEntity<String>(registered.getEmail(), HttpStatus.OK);
    }

    // activation
    @PostMapping(value = "/activate")
    public ResponseEntity<String> activateUserAccount(@RequestBody String email,@RequestParam String serviceid) {
        User u = userService.ActivateUser(email,serviceid);
        if (u == null)
            return new ResponseEntity<String>("failed", HttpStatus.BAD_REQUEST);
        return new ResponseEntity<String>("success", HttpStatus.OK);
    }

    //check if username or email is used or no
    @PostMapping("/checkusername")
    public ResponseEntity<String> checkUsernameOrEmail(@RequestParam String username) {
        if (userService.loadUserByUsername(username) == null)
            return new ResponseEntity<String>("success", HttpStatus.OK);
        else if (userService.loadUserByUsername(username) != null) ;
        return new ResponseEntity<String>("failed", HttpStatus.CONFLICT);
    }


}
