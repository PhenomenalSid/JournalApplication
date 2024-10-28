package com.Beginner.Project.Controller;

import com.Beginner.Project.Model.DTO.UserDTO;
import com.Beginner.Project.Model.Response.JournalRes;
import com.Beginner.Project.Model.Response.UserRes;
import com.Beginner.Project.Service.JournalEntryService;
import com.Beginner.Project.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final JournalEntryService journalEntryService;

    @Autowired
    public AdminController(UserService userService, JournalEntryService journalEntryService) {
        this.userService = userService;
        this.journalEntryService = journalEntryService;
    }


    @GetMapping("/all")
    public ResponseEntity<?> getAll() {
        List<UserRes> list = userService.findAll();

        if (list != null && !list.isEmpty()) {

            return new ResponseEntity<>(list, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/all-users")
    public ResponseEntity<?> getAllUsers() {
        List<UserRes> users = userService.findByRole("ROLE_USER");

        if (users != null && !users.isEmpty()) {
            return new ResponseEntity<>(users, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/all-admins")
    public ResponseEntity<?> getAllAdmins() {
        List<UserRes> admins = userService.findByRole("ROLE_ADMIN");

        if (admins != null && !admins.isEmpty()) {
            return new ResponseEntity<>(admins, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/create-admin")
    public ResponseEntity<?> createAdmin(@RequestBody UserDTO user) {
        UserRes userRes = userService.saveAdmin(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(userRes);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        UserRes user = userService.findById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/journal/{id}")
    public ResponseEntity<?> getJournalById(@PathVariable Long id) {
        JournalRes journal = journalEntryService.findById(id);
        return ResponseEntity.ok(journal);
    }

    @DeleteMapping("/user/{userName}")
    public ResponseEntity<?> deleteUserByAdmin(@PathVariable String userName) {
        userService.deleteUserByAdmin(userName);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Deleted user with username " + userName + " successfully.");
    }

    @DeleteMapping("/journal/{id}")
    public ResponseEntity<?> deleteJournal(@PathVariable Long id) {
        journalEntryService.deleteJournalByAdmin(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Deleted journal with ID " + id + " successfully.");
    }
}
