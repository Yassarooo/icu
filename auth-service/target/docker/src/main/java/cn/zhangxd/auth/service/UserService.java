package cn.zhangxd.auth.service;

import cn.zhangxd.auth.domain.Role;
import cn.zhangxd.auth.domain.User;
import cn.zhangxd.auth.domain.VerificationToken;
import cn.zhangxd.auth.repository.UserRepository;
import cn.zhangxd.auth.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository UserRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;


    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private RoleService roleService;

    public static final String TOKEN_INVALID = "invalidToken";
    public static final String TOKEN_EXPIRED = "expired";
    public static final String TOKEN_VALID = "valid";

    public UserService(UserRepository UserRepository) {
        this.UserRepository = UserRepository;
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

        User User = UserRepository.findByUsername(UsernameOrEmail);
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
            User = UserRepository.findByEmail(UsernameOrEmail);
            if (User != null) {
                return new org.springframework.security.core.userdetails.User(User.getUsername(), User.getPassword(), User.getAuthorities());
            } else {
                System.err.println("Username Not Found");
                return null;
            }
        }
    }

    public User findUserByUsername(String UsernameOrEmail) throws UsernameNotFoundException {
        User User = UserRepository.findByUsername(UsernameOrEmail);
        if (User != null) {
            return User;
        } else {
            User = UserRepository.findByEmail(UsernameOrEmail);
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
            return UserRepository.save(User);
        }
    }


    public List<User> getAllUsers() {
        List<User> usersList = (List<User>) UserRepository.findAll();

        if (usersList.size() > 0) {
            return usersList;
        } else {
            return new ArrayList<User>();
        }
    }

    @Transactional
    public User updateUser(User u) {
        try {
            User user = UserRepository.findByUsername(u.getUsername());

            user.setName(u.getName().trim());
            user.setUsername(u.getUsername().toLowerCase().trim());
            user.setEmail(u.getEmail().toLowerCase().trim());
            user.setPhonenumber(u.getPhonenumber());
            user.setDob(u.getDob());
            user.setGender(u.getGender().trim());
            user.setProfilepic(u.getProfilepic());
            user.setRoles(u.getRoles());
            user.setUpdatedAt(new Date());

            UserRepository.save(user);
            return u;
        } catch (ObjectOptimisticLockingFailureException e) {
            throw e;
        }
    }

    public User getUserByToken(final String verificationToken) {
        VerificationToken token = tokenRepository.findByToken(verificationToken);
        if (token != null) {
            return token.getUser();
        } else {
            token = tokenRepository.findByCode(verificationToken);
            if (token != null) {
                return token.getUser();
            }
        }
        return null;
    }

    public User ActivateUser(final String verificationToken, final String email) {
        VerificationToken token = tokenRepository.findByToken(verificationToken);
        if (token == null) {
            token = tokenRepository.findByCode(verificationToken);
        }
        if (token == null) {
            System.err.println("Token not Found");
            throw new UsernameNotFoundException(verificationToken);
        } else {
            if (token.getUser().getEmail().equals(email) || token.getUser().getUsername().equals(email)) {
                token.getUser().setEnabled(true);
                UserRepository.save(token.getUser());
                tokenRepository.delete(token);
                return token.getUser();
            } else {
                System.err.println("found the same token for other email");
                throw new UsernameNotFoundException(verificationToken);
            }
        }

    }

    public VerificationToken getVerificationToken(final String VerificationToken) {
        return tokenRepository.findByToken(VerificationToken);
    }

    public VerificationToken getTokenByUser(final User user) {
        return tokenRepository.findByuserID(user.getId());
    }

    public void deleteToken(final User user) {
        final VerificationToken verificationToken = tokenRepository.findByuserID(user.getId());

        if (verificationToken != null) {
            tokenRepository.delete(verificationToken);
        } else {
            System.err.println("verificationToken Not Found");
            throw new UsernameNotFoundException("verificationToken Not Found");
        }
    }

    public void createVerificationTokenForUser(final User user, final String token, final String code, final Long userID) {
        final VerificationToken myToken = new VerificationToken(token, code, userID, user);
        tokenRepository.save(myToken);
    }

    public VerificationToken generateNewVerificationToken(final String email) {
        User existinguser = findUserByUsername(email);
        if (existinguser != null) {
            VerificationToken vToken = tokenRepository.findByuserID(existinguser.getId());
            if (vToken != null) {
                Random rnd = new Random();
                int code = rnd.nextInt(999999);
                vToken.updateToken(UUID.randomUUID()
                        .toString(), String.format("%06d", code));
                return tokenRepository.save(vToken);
            } else {
                System.err.println("token Not Found");
                return null;
            }
        } else {
            System.err.println("User Not Found");
            return null;
        }

    }


    public Optional<User> getUserByID(final long id) {
        return UserRepository.findById(id);
    }

    public void changeUserPassword(final User user, final String password) {
        user.setPassword(bCryptPasswordEncoder.encode(password));
        UserRepository.save(user);
    }

    public boolean checkIfValidOldPassword(final User user, final String oldPassword) {
        return bCryptPasswordEncoder.matches(oldPassword, user.getPassword());
    }

    public String validateVerificationToken(String token) {
        final VerificationToken verificationToken = tokenRepository.findByToken(token);
        if (verificationToken == null) {
            return TOKEN_INVALID;
        }

        final User user = verificationToken.getUser();
        final Calendar cal = Calendar.getInstance();
        if ((verificationToken.getExpiryDate()
                .getTime() - cal.getTime()
                .getTime()) <= 0) {
            tokenRepository.delete(verificationToken);
            return TOKEN_EXPIRED;
        }

        user.setEnabled(true);
        // tokenRepository.delete(verificationToken);
        UserRepository.save(user);
        return TOKEN_VALID;
    }

    public void deleteAllUsers() {
        UserRepository.deleteAll();
    }


}