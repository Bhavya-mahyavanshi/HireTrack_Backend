package com.hiretrack.service;

import com.hiretrack.model.JobApplication;
import com.hiretrack.model.SkillMatch;
import com.hiretrack.model.UserSkill;
import com.hiretrack.repository.SkillMatchRepository;
import com.hiretrack.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkillMatcherService {
    
    private final SkillRepository skillRepository;
    private final SkillMatchRepository skillMatchRepository;

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
    
    private SkillMatch saveMatch(JobApplication application, int score, Set<String> matched, Set<String> missing){
        skillMatchRepository.findByApplication(application).ifPresent(skillMatchRepository::delete);

        SkillMatch skillMatch = SkillMatch.builder()
                .application(application)
                .matchScore(score)
                .matchedSkills(String.join(",", matched))
                .missingSkills(String.join(",", missing))
                .build();

        return skillMatchRepository.save(skillMatch);
    }
}
