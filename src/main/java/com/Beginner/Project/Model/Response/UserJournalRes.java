package com.Beginner.Project.Model.Response;

import com.Beginner.Project.Model.JournalEntry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserJournalRes {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime date;

    public static UserJournalRes convertToRes(JournalEntry journalEntry) {
        return new UserJournalRes(journalEntry.getId(), journalEntry.getTitle(), journalEntry.getContent(), journalEntry.getDate());
    }
}
