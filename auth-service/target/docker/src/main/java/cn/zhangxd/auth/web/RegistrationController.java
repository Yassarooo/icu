package cn.zhangxd.auth.web;

import cn.zhangxd.auth.config.registration.OnRegistrationCompleteEvent;
import cn.zhangxd.auth.domain.User;
import cn.zhangxd.auth.domain.VerificationToken;
import cn.zhangxd.auth.service.JwtTokenUtil;
import cn.zhangxd.auth.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RequestMapping("/auth")
@RestController
public class RegistrationController {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private MessageSource messages;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private Environment env;

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
    public User user(Principal principal) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String loggedUsername = auth.getName();
        return userService.findUserByUsername(loggedUsername);
    }

    // Registration
    @PostMapping("/register")
    public ResponseEntity<String> registerUserAccount(@RequestBody User accountDto, final HttpServletRequest request) {
        LOGGER.debug("Registering user account with information: {}", accountDto);

        final User registered = userService.save(accountDto);
        if (registered == null) {
            return new ResponseEntity<String>("failed", HttpStatus.CONFLICT);
        }
        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(registered, request.getLocale(), getAppUrl(request)));
        return new ResponseEntity<String>("success", HttpStatus.OK);
    }

    // User activation - verification
    @GetMapping("/resendEmail")
    public ResponseEntity<String> resendRegistrationToken(final HttpServletRequest request, @RequestParam("email") final String email) {

        final VerificationToken newToken = userService.generateNewVerificationToken(email);
        mailSender.send(constructResendVerificationTokenEmail(getAppUrl(request), request.getLocale(), newToken, email));
        return new ResponseEntity<String>("Email sent successfully", HttpStatus.OK);

    }

    // confirm activation
    @PostMapping(value = "/registrationconfirm")
    public ResponseEntity<User> registrationConfirm(@RequestParam("token") String token, @RequestParam("email") String email) {
        return new ResponseEntity<User>(userService.ActivateUser(token, email), HttpStatus.OK);
    }

    //check if username or email is used or no
    @PostMapping("/checkusername")
    public ResponseEntity<String> checkUsernameOrEmail(@RequestParam String username) {
        if (userService.loadUserByUsername(username) == null)
            return new ResponseEntity<String>("success", HttpStatus.OK);
        else if (userService.loadUserByUsername(username) != null) ;
        return new ResponseEntity<String>("failed", HttpStatus.CONFLICT);
    }


    // ============== NON-API ============

    private SimpleMailMessage constructResendVerificationTokenEmail(final String contextPath, final Locale locale, final VerificationToken newToken, final String emailaddress) {
        final String confirmationUrl = contextPath + "/#/registrationConfirm.html/" + emailaddress + "/" + newToken.getToken();
        final String message = messages.getMessage("message.regSuccLink", null, "You have registered successfully. To confirm your registration, please enter this code:\n " + newToken.getCode() + "\n or click on the below link.", locale);
        return constructEmail("Resend Registration Token", message + " \r\n" + confirmationUrl, emailaddress);
    }

    private SimpleMailMessage constructEmail(String subject, String body, String emailaddress) {
        final SimpleMailMessage email = new SimpleMailMessage();
        email.setSubject(subject);
        email.setText(body);
        email.setTo(emailaddress);
        email.setFrom(env.getProperty("support.email"));
        return email;
    }

    private String getAppUrl(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }

}
