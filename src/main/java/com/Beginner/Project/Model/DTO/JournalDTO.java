package com.Beginner.Project.Model.DTO;

import com.Beginner.Project.Model.JournalEntry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JournalDTO {
    private String title;
    private String content;

    public static JournalDTO convertToDTO(JournalEntry journalEntry){
        JournalDTO journalDTO = new JournalDTO();
        journalDTO.setTitle(journalEntry.getTitle());
        journalDTO.setContent(journalEntry.getContent());
        return journalDTO;
    }
}
