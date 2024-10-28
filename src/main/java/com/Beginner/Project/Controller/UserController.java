package com.Beginner.Project.Controller;

import com.Beginner.Project.Model.DTO.UserDTO;
import com.Beginner.Project.Model.Response.UserRes;
import com.Beginner.Project.Service.EmailService;
import com.Beginner.Project.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private EmailService emailService;

    @Autowired
    public UserController(UserService userService, EmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;
    }

    @PutMapping("/update")
    public ResponseEntity<UserRes> updateUser(@RequestBody UserDTO user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();

        UserRes userRes = userService.saveUser(userName, user);

        return ResponseEntity.ok(userRes);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();

        userService.deleteById(userName);
        return ResponseEntity.noContent().build();
    }
}

