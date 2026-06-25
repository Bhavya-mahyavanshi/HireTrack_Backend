package com.hiretrack.service;

import com.hiretrack.dto.response.JobResponse;
import com.hiretrack.exception.ScrapeFailedException;
import com.hiretrack.model.Job;
import com.hiretrack.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobScraperService {

    private final JobRepository jobRepository;

    private static final Set<String> TECH_KEYWORDS = new LinkedHashSet<>(Arrays.asList(
            "Java", "Spring", "Spring Boot", "Spring Security", "Hibernate", "JPA",
            "SQL", "MySQL", "PostgreSQL", "MongoDB", "Redis",
            "React", "JavaScript", "TypeScript", "Next.js", "Vue", "Angular", "HTML", "CSS",
            "Python", "Django", "Flask", "FastAPI",
            "AWS", "Azure", "GCP", "Docker", "Kubernetes", "Terraform",
            "Git", "GitHub", "GitLab", "CI/CD", "Jenkins",
            "REST", "API", "GraphQL", "Microservices",
            "Node.js", "Express", "Go", "Rust", "C++", "C#", ".NET",
            "Kafka", "RabbitMQ", "Elasticsearch",
            "Linux", "Bash", "Maven", "Gradle", "JUnit", "Mockito"
    ));

    public JobResponse scrapeJob(String url) {
        Optional<Job> existing = jobRepository.findByUrl(url);
        if (existing.isPresent()) {
            log.info("URL already scraped, returning cached job: {}", url);
            return toResponse(existing.get());
        }

        Document doc;
        try {
            doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();
        } catch (IOException e) {
            log.error("Failed to scrape URL: {}", url, e);
            throw new ScrapeFailedException("Failed to fetch job posting. Please check the URL and try again.");
        }

        String title = extractTitle(doc);
        String company = extractCompany(doc);
        String location = extractLocation(doc);
        String description = extractDescription(doc);
        String requiredSkills = extractSkills(description + " " + title);
        int[] salary = extractSalary(description);

        Job job = Job.builder()
                .url(url)
                .title(title)
                .company(company)
                .location(location)
                .description(description)
                .requiredSkills(requiredSkills)
                .salaryMin(salary != null ? salary[0] : null)
                .salaryMax(salary != null ? salary[1] : null)
                .build();

        Job saved = jobRepository.save(job);
        log.info("Scraped and saved job: {} at {}", title, company);
        return toResponse(saved);
    }

    private String extractTitle(Document doc) {
        String[] selectors = {
                "[data-testid='jobsearch-JobInfoHeader-title']",
                ".jobsearch-JobInfoHeader-title",
                "h1.job-title", "h1.title",
                ".job-title", "[class*='job-title']",
                "[class*='JobTitle']", "[class*='job_title']"
        };

        for (String sel : selectors) {
            Element el = doc.selectFirst(sel);
            if (el != null && !el.text().isBlank())
                return el.text().trim();
        }

        Element h1 = doc.selectFirst("h1");
        return h1 != null ? h1.text().trim() : "Unknown Title";
    }

    private String extractCompany(Document doc) {
        String[] selectors = {
                "[data-testid='inlineHeader-companyName']",
                "[data-company]", ".company-name", ".employer-name",
                "[class*='company']", "[class*='Company']",
                "[class*='employer']", ".orgname"
        };

        for (String sel : selectors) {
            Element el = doc.selectFirst(sel);
            if (el != null && !el.text().isBlank())
                return el.text().trim();
        }
        return "Unknown Company";
    }

    private String extractLocation(Document doc) {
        String[] selectors = {
                "[data-testid='job-location']", "[data-testid='inlineHeader-companyLocation']",
                ".job-location", ".location", "[class*='location']", "[class*='Location']"
        };

        for (String sel : selectors) {
            Element el = doc.selectFirst(sel);
            if (el != null && !el.text().isBlank())
                return el.text().trim();
        }

        return "";
    }

    private String extractDescription(Document doc) {
        String[] selectors = {
                "[data-testid='jobDescriptionText']",
                "#jobDescriptionText", ".jobsearch-jobDescriptionText",
                "[class*='job-description']", "[class*='JobDescription'",
                "[class*='description']", "article", ".content"
        };

        for (String sel : selectors) {
            Element el = doc.selectFirst(sel);
            if (el != null && !el.text().isBlank())
                return el.text().trim();
        }

        return doc.body() != null ? doc.body().text() : "";
    }

    String extractSkills(String text) {
        String lowerText = text.toLowerCase();
        List<String> found = new ArrayList<>();
        for (String keyword : TECH_KEYWORDS) {
            if (lowerText.contains(keyword.toLowerCase())) {
                found.add(keyword);
            }
        }

        return String.join(",", found);
    }

    public int[] extractSalary(String text) {
        Pattern pattern = Pattern.compile(
                "\\$([\\d,]+)k?\\s*[--]\\s*\\$([\\d,]+)k?|\\$([\\d,]+)k?",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);

        int min = 0, max = 0;
        while (matcher.find()) {
            if (matcher.group(1) != null && matcher.group(2) != null) {
                min = parseSalary(matcher.group(1));
                max = parseSalary(matcher.group(2));
                return new int[] { min, max };
            } else if (matcher.group(3) != null && min == 0) {
                min = parseSalary(matcher.group(3));
            }
        }

        return min > 0 ? new int[] { min, max } : null;
    }

    private int parseSalary(String raw) {
        String cleaned = raw.replace(",", "").trim();
        int val = Integer.parseInt(cleaned);
        return val < 1000 ? val * 1000 : val;
    }

    private JobResponse toResponse(Job job) {
        return JobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .company(job.getCompany())
                .location(job.getLocation())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .requiredSkills(job.getRequiredSkills())
                .url(job.getUrl())
                .build();
    }
}