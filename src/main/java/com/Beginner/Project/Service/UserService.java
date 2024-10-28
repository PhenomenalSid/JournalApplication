package com.Beginner.Project.Service;

import com.Beginner.Project.Exceptions.UserException.NoUsersExistException;
import com.Beginner.Project.Exceptions.UserException.UserAlreadyExistsException;
import com.Beginner.Project.Exceptions.UserException.UserNotFoundException;
import com.Beginner.Project.Model.DTO.UserDTO;
import com.Beginner.Project.Model.Response.UserRes;
import com.Beginner.Project.Model.Role;
import com.Beginner.Project.Model.User;
import com.Beginner.Project.Repository.RoleRepository;
import com.Beginner.Project.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    private static final String CACHE_KEY_PREFIX_USERS = "users:";
    private static final String CACHE_KEY_PREFIX_ROLES = "roles:";

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, RedisTemplate<String, Object> redisTemplate) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public UserRes saveNewUser(UserDTO userDTO) {
        try {
            if (userRepository.findByUserName(userDTO.getUserName()) != null) {
                throw new UserAlreadyExistsException("User with username " + userDTO.getUserName() + " already exists.");
            }

            User user = new User();
            user.setUserName(userDTO.getUserName());
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            Role userRole = roleRepository.findByName("ROLE_USER");
            user.setRole(userRole);
            User saved = userRepository.save(user);
            UserRes res = UserRes.convertToRes(saved);
            invalidateCacheForAllUsers();
            invalidateCacheForRole("ROLE_USER");
            cacheUser(res);
            return res;
        } catch (Exception e) {
            log.error("Error occurred while saving user with username {}", userDTO.getUserName(), e);
            throw new RuntimeException("An error occurred while saving the user.");
        }
    }

    @Transactional
    public UserRes saveUser(String userName, UserDTO user) {
        try {
            User old = userRepository.findByUserName(userName);
            if (user.getUserName() != null) {
                old.setUserName(user.getUserName());
            }

            if (user.getPassword() != null) {
                old.setPassword(passwordEncoder.encode(user.getPassword()));
            }

            User saved = userRepository.save(old);
            UserRes res = UserRes.convertToRes(saved);
            cacheUser(res);
            invalidateCacheForAllUsers();
            invalidateCacheForRole(saved.getRole().getName());
            return res;
        } catch (Exception e) {
            log.error("Error occurred while updating user with username {}", userName, e);
            throw new RuntimeException("An error occurred while updating the user.");
        }

    }

    @Transactional
    public UserRes saveAdmin(UserDTO userDTO) {

        try {
            if (userRepository.findByUserName(userDTO.getUserName()) != null) {
                throw new UserAlreadyExistsException("Admin with username " + userDTO.getUserName() + " already exists.");
            }

            User user = new User();
            user.setUserName(userDTO.getUserName());
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            Role role = roleRepository.findByName("ROLE_ADMIN");
            user.setRole(role);
            User saved = userRepository.save(user);
            UserRes res = UserRes.convertToRes(saved);
            cacheUser(res);
            invalidateCacheForRole("ROLE_ADMIN");
            invalidateCacheForAllUsers();
            return res;
        } catch (Exception e) {
            log.error("Error occurred while saving admin with username {}", userDTO.getUserName(), e);
            throw new RuntimeException("An error occurred while saving the admin.");
        }
    }

    @Transactional(readOnly = true)
    public List<UserRes> findAll() {
        try {
            String cacheKey = CACHE_KEY_PREFIX_USERS + "all";
            List<UserRes> users = (List<UserRes>) redisTemplate.opsForValue().get(cacheKey);

            if (users != null && !users.isEmpty()) {
                return users;
            }

            List<User> list = userRepository.findAll();

            if (!list.isEmpty()) {
                List<UserRes> answer = new ArrayList<>();

                for (User u : list) {
                    answer.add(UserRes.convertToRes(u));
                }

                cacheAllUsers(answer);
                return answer;
            }
            else {
                throw new NoUsersExistException("No users are present in the database");
            }

        } catch (Exception e) {
            log.error("Error occurred while fetching all users", e);
            throw new RuntimeException("An error occurred while fetching all user.");
        }
    }

    @Transactional(readOnly = true)
    public UserRes findById(Long myId) {
        try {
            UserRes cachedUser = (UserRes) redisTemplate.opsForValue().get(CACHE_KEY_PREFIX_USERS + myId);
            if (cachedUser != null) {
                return cachedUser;
            }

            User user = userRepository.findById(myId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + myId));
            UserRes userRes = UserRes.convertToRes(user);
            cacheUser(userRes);
            return userRes;
        } catch (Exception e) {
            log.error("Error occurred while fetching user with id {}", myId, e);
            throw new RuntimeException("An error occurred while fetching the user.");
        }
    }

    @Transactional
    public void deleteById(String userName) {
        try {
            User user = userRepository.findByUserName(userName);
            Long myId = user.getId();

            if (!userRepository.existsById(myId)) {
                throw new UserNotFoundException("User not found with ID: " + myId);
            }

            invalidateCacheForUser(userName);
            invalidateCacheForAllUsers();
            invalidateCacheForRole(user.getRole().getName());
            userRepository.deleteById(myId);
        } catch (Exception e) {
            log.error("Error occurred while deleting user with username {}", userName, e);
            throw new RuntimeException("An error occurred while deleting the user.");
        }
    }

    @Transactional(readOnly = true)
    public List<UserRes> findByRole(String roleName) {
        try {
            String cacheKey = CACHE_KEY_PREFIX_ROLES + roleName;
            List<UserRes> cachedUsers = (List<UserRes>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedUsers != null && !cachedUsers.isEmpty()) {
                return cachedUsers;
            }

            Role role = roleRepository.findByName(roleName);
            List<User> users = userRepository.findByRole(role);
            if (users != null && !users.isEmpty()) {
                List<UserRes> answer = new ArrayList<>();

                for (User u : users) {
                    answer.add(UserRes.convertToRes(u));
                }

                cacheUsersByRole(roleName, answer);
                return answer;
            }

            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error occurred while fetching users with role {}", roleName, e);
            throw new RuntimeException("An error occurred while fetching the users.");
        }
    }

    @Transactional
    public void deleteUserByAdmin(String userName) {
        try {
            User user = userRepository.findByUserName(userName);

            if (user == null) {
                throw new UserNotFoundException("User with username " + userName + " not found.");
            }

            if (user.getRole().getName().equals("ROLE_ADMIN")) {
                throw new UserNotFoundException("Not allowed to delete an admin");
            }

            Long myId = user.getId();
            invalidateCacheForUser(userName);
            invalidateCacheForAllUsers();
            invalidateCacheForRole("ROLE_USER");
            userRepository.deleteById(myId);
        } catch (Exception e) {
            log.error("Error occurred while deleting user with username {}", userName, e);
            throw new RuntimeException("An error occurred while deleting the user.");
        }
    }

    private void cacheUser(UserRes userRes) {
        String cacheKey = CACHE_KEY_PREFIX_USERS + userRes.getUserName();
        redisTemplate.opsForValue().set(cacheKey, userRes, 1, TimeUnit.HOURS);
    }

    private void cacheAllUsers(List<UserRes> userResponses) {
        String cacheKey = CACHE_KEY_PREFIX_USERS + "all";
        redisTemplate.opsForValue().set(cacheKey, userResponses, 1, TimeUnit.HOURS);
    }

    private void cacheUsersByRole(String roleName, List<UserRes> userResponses) {
        String cacheKey = CACHE_KEY_PREFIX_ROLES + roleName;
        redisTemplate.opsForValue().set(cacheKey, userResponses, 1, TimeUnit.HOURS);
    }

    private void invalidateCacheForUser(String userName) {
        String cacheKey = CACHE_KEY_PREFIX_USERS + userName;
        redisTemplate.delete(cacheKey);
    }

    private void invalidateCacheForAllUsers() {
        String cacheKey = CACHE_KEY_PREFIX_USERS + "all";
        redisTemplate.delete(cacheKey);
    }

    private void invalidateCacheForRole(String roleName) {
        String cacheKey = CACHE_KEY_PREFIX_ROLES + roleName;
        redisTemplate.delete(cacheKey);
    }
}