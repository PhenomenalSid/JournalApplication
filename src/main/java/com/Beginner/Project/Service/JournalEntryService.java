package com.Beginner.Project.Service;

import com.Beginner.Project.Exceptions.AdminException.DeleteAdminJournalException;
import com.Beginner.Project.Exceptions.JournalExceptions.JournalNotFoundException;
import com.Beginner.Project.Exceptions.JournalExceptions.NoUserJournalsExistException;
import com.Beginner.Project.Model.DTO.JournalDTO;
import com.Beginner.Project.Model.JournalEntry;
import com.Beginner.Project.Model.Response.JournalRes;
import com.Beginner.Project.Model.Response.UserJournalRes;
import com.Beginner.Project.Model.Response.UserRes;
import com.Beginner.Project.Model.User;
import com.Beginner.Project.Repository.JournalEntryRepository;
import com.Beginner.Project.Repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class JournalEntryService {

    private static final String CACHE_KEY_PREFIX_JOURNAL_ENTRIES = "journals:";
    private static final String CACHE_KEY_PREFIX_USER_JOURNALS = "user_journals:";

    private static final Logger log = LoggerFactory.getLogger(JournalEntryService.class);

    private final JournalEntryRepository journalEntryRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisTemplate<String, JournalRes> journalResRedisTemplate;

    @Autowired
    public JournalEntryService(JournalEntryRepository journalEntryRepository, UserRepository userRepository, RedisTemplate<String, Object> redisTemplate, RedisTemplate<String, JournalRes> journalResRedisTemplate) {
        this.journalEntryRepository = journalEntryRepository;
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
        this.journalResRedisTemplate = journalResRedisTemplate;
    }

    @Transactional
    public JournalRes saveNewEntry(JournalDTO journalDTO, String userName) {
        try {
            User user = userRepository.findByUserName(userName);
            JournalEntry journalEntry = new JournalEntry();
            journalEntry.setTitle(journalDTO.getTitle());
            journalEntry.setContent(journalDTO.getContent());
            journalEntry.setDate(LocalDateTime.now());
            journalEntry.setUser(user);
            JournalEntry saved = journalEntryRepository.save(journalEntry);
            user.getJournalEntries().add(saved);
            userRepository.save(user);
            JournalRes journalRes = JournalRes.convertToRes(saved);
            invalidateCacheFoUserJournals(userName);
            invalidateCacheForAllJournals();
            cacheJournal(journalRes);
            return journalRes;
        } catch (Exception e) {
            log.error("Error occurred while saving journal", e);
            throw new RuntimeException("An error has occurred while saving this journal!");
        }
    }

    @Transactional(readOnly = true)
    public JournalRes findById(Long myId) {

        try {
            String cacheKey = CACHE_KEY_PREFIX_JOURNAL_ENTRIES + myId;
            JournalRes cachedJournalRes = (JournalRes) journalResRedisTemplate.opsForValue().get(cacheKey);
            if (cachedJournalRes != null) {
                return cachedJournalRes;
            }
            Optional<JournalEntry> journal = journalEntryRepository.findById(myId);
            if(journal.isEmpty())
            {
                throw new JournalNotFoundException("Journal entry not found with ID: " + myId);
            }

            JournalRes journalRes = JournalRes.convertToRes(journal.get());
            cacheJournal(journalRes);
            return journalRes;
        } catch (Exception e) {
            log.error("Error occurred while fetching journal with id {}", myId, e);
            throw new RuntimeException("An error has occurred while fetching this journal!");
        }
    }

    @Transactional
    public void deleteById(Long myId, String userName) {
        try {
            User user = userRepository.findByUserName(userName);
            boolean flag = user.getJournalEntries().removeIf(x -> x.getId().equals(myId));

            if (flag) {
                userRepository.save(user);
                journalEntryRepository.deleteById(myId);
                invalidateCacheFoUserJournals(userName);
                invalidateCacheForJournal(myId);
                invalidateCacheForAllJournals();
            } else {
                throw new JournalNotFoundException("You don't have Journal entry to delete with ID: " + myId);
            }
        } catch (Exception e) {
            log.error("Error occurred while deleting journal with id {}", myId, e);
            throw new RuntimeException("An error has occurred while deleting this journal!");
        }
    }

    @Transactional
    public JournalRes updateJournalEntryById(Long myId, JournalDTO journalEntry) {
        try {
            Optional<JournalEntry> found = journalEntryRepository.findById(myId);

            if(found.isPresent()){
                JournalEntry old = found.get();
                old.setTitle(!journalEntry.getTitle().isEmpty() ? journalEntry.getTitle() : old.getTitle());
                old.setContent(!journalEntry.getContent().isEmpty() ? journalEntry.getContent() : old.getContent());
                JournalEntry change = journalEntryRepository.save(old);
                JournalRes journalRes = JournalRes.convertToRes(change);
                invalidateCacheForJournal(old.getId());
                invalidateCacheForAllJournals();
                invalidateCacheFoUserJournals(old.getUser().getUserName());
                cacheJournal(journalRes);
                return journalRes;
            }
            else{
                throw new JournalNotFoundException("You don't have Journal entry to update with ID: " + myId);
            }
        } catch (Exception e) {
            log.error("Error occurred while updating journal with id {}", myId, e);
            throw new RuntimeException("An error has occurred while updating this journal!");
        }
    }

    @Transactional(readOnly = true)
    public List<UserJournalRes> getJournalEntries(String userName) {
        try {
            String cacheKey = CACHE_KEY_PREFIX_USER_JOURNALS + userName;
            List<UserJournalRes> cachedUsers = (List<UserJournalRes>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedUsers != null) {
                return cachedUsers;
            }

            User user = userRepository.findByUserName(userName);
            List<JournalEntry> list = user.getJournalEntries();

            if(list.isEmpty()){
                throw new NoUserJournalsExistException("User with " + userName + " does not have any journals!");
            }

            List<UserJournalRes> answer = new ArrayList<>();

            for(JournalEntry j : list){
                answer.add(UserJournalRes.convertToRes(j));
            }

            cacheUserJournals(userName, answer);
            return answer;
        } catch (Exception e) {
            log.error("Error occurred while fetching journals of user with username {}", userName, e);
            throw new RuntimeException("An error occurred while fetching journals of the user.");
        }
    }

    @Transactional
    public void deleteJournalByAdmin(Long id) {
        try {
            Optional<JournalEntry> journalEntry = journalEntryRepository.findById(id);

            if (journalEntry.isEmpty()) {
                throw new JournalNotFoundException("Journal Entry with id " + id + " not found!");
            }

            if (journalEntry.get().getUser().getRole().getName().equals("ROLE_ADMIN")) {
                throw new DeleteAdminJournalException("Admin's journal cannot be deleted!");
            }

            invalidateCacheForJournal(id);
            invalidateCacheFoUserJournals(journalEntry.get().getUser().getUserName());
            invalidateCacheForAllJournals();
            journalEntryRepository.deleteById(id);
        }
        catch (Exception e){
            log.error("Error occurred while deleting the journal with id {}", id, e);
            throw new RuntimeException("An error occurred while deleting journal with id " + id);
        }
    }

    private void cacheJournal(JournalRes journalRes) {
        String cacheKey = CACHE_KEY_PREFIX_JOURNAL_ENTRIES + journalRes.getId();
        journalResRedisTemplate.opsForValue().set(cacheKey, journalRes, 1, TimeUnit.HOURS);
    }

    private void cacheAllJournals(List<JournalRes> journalResponses) {
        String cacheKey = CACHE_KEY_PREFIX_JOURNAL_ENTRIES + "all";
        journalResRedisTemplate.opsForValue().set(cacheKey, (JournalRes) journalResponses, 1, TimeUnit.HOURS);
    }

    private void cacheUserJournals(String userName, List<UserJournalRes> journalResponses) {
        String cacheKey = CACHE_KEY_PREFIX_USER_JOURNALS + userName;
        redisTemplate.opsForValue().set(cacheKey, journalResponses, 1, TimeUnit.HOURS);
    }

    private void invalidateCacheForJournal(Long id) {
        String cacheKey = CACHE_KEY_PREFIX_JOURNAL_ENTRIES + id;
        journalResRedisTemplate.delete(cacheKey);
    }

    private void invalidateCacheForAllJournals() {
        String cacheKey = CACHE_KEY_PREFIX_JOURNAL_ENTRIES + "all";
        journalResRedisTemplate.delete(cacheKey);
    }

    private void invalidateCacheFoUserJournals(String userName) {
        String cacheKey = CACHE_KEY_PREFIX_USER_JOURNALS + userName;
        redisTemplate.delete(cacheKey);
    }
}
