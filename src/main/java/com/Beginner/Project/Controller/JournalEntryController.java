package com.Beginner.Project.Controller;

import com.Beginner.Project.Model.DTO.JournalDTO;
import com.Beginner.Project.Model.Response.JournalRes;
import com.Beginner.Project.Model.Response.UserJournalRes;
import com.Beginner.Project.Service.JournalEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/journal")
public class JournalEntryController {

    private final JournalEntryService journalEntryService;

    @Autowired
    public JournalEntryController(JournalEntryService journalEntryService) {
        this.journalEntryService = journalEntryService;
    }

    @GetMapping("/myJournals")
    public ResponseEntity<?> getJournalEntries() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();

        List<UserJournalRes> list = journalEntryService.getJournalEntries(userName);

        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<?> saveNewEntry(@RequestBody JournalDTO journalDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        JournalRes journalRes = journalEntryService.saveNewEntry(journalDTO, userName);
        return new ResponseEntity<>(journalRes, HttpStatus.CREATED);
    }

    @GetMapping("get/{myId}")
    public ResponseEntity<?> getJournalEntryById(@PathVariable Long myId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        JournalRes journalRes = journalEntryService.findById(myId);
        return ResponseEntity.ok(journalRes);
    }

    @DeleteMapping("delete/{myId}")
    public ResponseEntity<?> deleteJournalEntryById(@PathVariable Long myId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        journalEntryService.deleteById(myId, userName);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Deleted journal with ID " + myId + " successfully.");
    }

    @PutMapping("update/{myId}")
    public ResponseEntity<?> updateJournalEntryById(@PathVariable Long myId, @RequestBody JournalDTO journalDTO) {
        JournalRes journalRes = journalEntryService.updateJournalEntryById(myId, journalDTO);
        return ResponseEntity.ok(journalRes);
    }
}
