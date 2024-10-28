package com.Beginner.Project.Model.Response;

import com.Beginner.Project.Model.JournalEntry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JournalRes {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime date;
    private String userName;

    public static JournalRes convertToRes(JournalEntry journalEntry) {
        return new JournalRes(journalEntry.getId(), journalEntry.getTitle(), journalEntry.getContent(), journalEntry.getDate(), journalEntry.getUser().getUserName());
    }
}
