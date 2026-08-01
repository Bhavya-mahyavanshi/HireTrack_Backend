package com.hiretrack.service;

import com.hiretrack.model.JobApplication;
import com.hiretrack.model.SkillMatch;
import com.hiretrack.model.UserSkill;
import com.hiretrack.repository.SkillMatchRepository;
import com.hiretrack.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkillMatcherService {

    private final SkillRepository skillRepository;
    private final SkillMatchRepository skillMatchRepository;

    @Transactional
    public SkillMatch calculateMatch(JobApplication application) {
        List<UserSkill> userSkillEntities = skillRepository.findByUser(application.getUser());
        Set<String> userSkills = userSkillEntities.stream()
                .map(s -> s.getSkillName().toLowerCase().trim())
                .collect(Collectors.toSet());

        String requiredSkillsStr = application.getJob().getRequiredSkills();
        if (requiredSkillsStr == null || requiredSkillsStr.isBlank()) {
            return saveMatch(application, 0, Collections.emptySet(), Collections.emptySet());
        }

        Set<String> requiredSkills = Arrays.stream(requiredSkillsStr.split(","))
                .map(s -> s.toLowerCase().trim())
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());

        if (requiredSkills.isEmpty()) {
            return saveMatch(application, 0, Collections.emptySet(), Collections.emptySet());
        }

        Set<String> matched = new HashSet<>(userSkills);
        matched.retainAll(requiredSkills);

        Set<String> missing = new HashSet<>(requiredSkills);
        missing.removeAll(userSkills);

        int score = (int) Math.round((double) matched.size() / requiredSkills.size() * 100);

        return saveMatch(application, score, matched, missing);
    }

    /**
     * FIX: update-in-place instead of delete-then-insert.
     *
     * The previous delete()+save() approach had two problems: (1) with
     * GenerationType.IDENTITY, the new INSERT fires immediately while the
     * DELETE can still be sitting unflushed, hitting the unique constraint
     * on application_id — and (2) even with an explicit flush(), two
     * near-simultaneous calls (a double-click, or a dev-mode double
     * invocation) can both pass the "row doesn't exist yet" check before
     * either has written, and both attempt an INSERT.
     *
     * Mutating the existing row's fields and saving that same entity turns
     * this into a single UPDATE keyed on the row's own primary key — there
     * is no INSERT to collide on for the recalculate case, and no delete/
     * flush timing window to get wrong.
     */
    private SkillMatch saveMatch(JobApplication application, int score, Set<String> matched, Set<String> missing) {
        SkillMatch skillMatch = skillMatchRepository.findByApplication(application)
                .orElseGet(() -> SkillMatch.builder().application(application).build());

        skillMatch.setMatchScore(score);
        skillMatch.setMatchedSkills(String.join(",", matched));
        skillMatch.setMissingSkills(String.join(",", missing));
        skillMatch.setCalculatedAt(LocalDateTime.now());

        return skillMatchRepository.save(skillMatch);
    }
}