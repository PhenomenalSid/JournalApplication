package com.Beginner.Project.Controller;

import com.Beginner.Project.Model.DTO.UserDTO;
import com.Beginner.Project.Model.Response.UserRes;
import com.Beginner.Project.Service.UserDetailsServiceImplementation;
import com.Beginner.Project.Service.JwtTokenBlacklistService;
import com.Beginner.Project.Service.UserService;
import com.Beginner.Project.Util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public")
public class PublicController {

    private static final Logger log = LoggerFactory.getLogger(PublicController.class);

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImplementation userDetailsServiceImplementation;
    private final JwtUtil jwtUtil;
    private final JwtTokenBlacklistService jwtTokenBlacklistService;

    @Autowired
    public PublicController(UserService userService, AuthenticationManager authenticationManager, UserDetailsServiceImplementation userDetailsServiceImplementation, JwtUtil jwtUtil, JwtTokenBlacklistService jwtTokenBlacklistService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.userDetailsServiceImplementation = userDetailsServiceImplementation;
        this.jwtUtil = jwtUtil;
        this.jwtTokenBlacklistService = jwtTokenBlacklistService;
    }


    @GetMapping("/health-check")
    public String hello() {
        return "Hello Project! Welcome to my guys!";
    }

    @PostMapping("/signup")
    public ResponseEntity<UserRes> signUp(@RequestBody UserDTO userDTO) {
        UserRes userRes = userService.saveNewUser(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(userRes);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDTO user) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUserName(), user.getPassword()));
            UserDetails userDetails = userDetailsServiceImplementation.loadUserByUsername(user.getUserName());
            String jwt = jwtUtil.generateToken(userDetails.getUsername());
            return ResponseEntity.ok(jwt);
        } catch (Exception e) {
            log.error("Exception occurred while creating JWT Token!", e);
            return ResponseEntity.badRequest().body("Incorrect username or password!");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {

        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You need to log in first.");
        }

        String jwtToken = token.substring(7);

        if (jwtTokenBlacklistService.isTokenBlacklisted(jwtToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token is already invalid.");
        }

        jwtTokenBlacklistService.blacklistToken(jwtToken, 5);
        return ResponseEntity.ok("Logged out successfully");
    }
}
