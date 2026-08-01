package com.hiretrack.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiretrack.dto.response.JobResponse;
import com.hiretrack.exception.ScrapeFailedException;
import com.hiretrack.model.Job;
import com.hiretrack.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobScraperService {

    private final JobRepository jobRepository;
    private final ObjectMapper objectMapper;

    @Value("${rapidapi.key:}")
    private String rapidApiKey;

    private static final Set<String> BLOCKED_DOMAINS = Set.of(
            "indeed.com", "ca.indeed.com", "www.indeed.com",
            "linkedin.com", "www.linkedin.com",
            "glassdoor.com", "www.glassdoor.ca", "www.glassdoor.com"
    );

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

    // ─── Entry point ──────────────────────────────────────────────────────────

    public JobResponse scrapeJob(String url) {
        Optional<Job> existing = jobRepository.findByUrl(url);
        if (existing.isPresent()) {
            log.info("Returning cached job for URL: {}", url);
            return toResponse(existing.get());
        }

        String domain = extractDomain(url);

        if (BLOCKED_DOMAINS.contains(domain)) {
            return scrapeViaJSearch(url, domain);
        }

        return scrapeViaJsoup(url);
    }

    // ─── Jsoup — Greenhouse, Lever, Workday, Ashby, company career pages ─────

    private JobResponse scrapeViaJsoup(String url) {
        Document doc;
        try {
            doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                            "AppleWebKit/537.36 (KHTML, like Gecko) " +
                            "Chrome/124.0.0.0 Safari/537.36")
                    .header("Accept-Language", "en-CA,en;q=0.9")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .timeout(10000)
                    .followRedirects(true)
                    .get();
        } catch (IOException e) {
            log.error("Jsoup failed for URL: {}", url, e);
            throw new ScrapeFailedException(
                    "Could not fetch that page. " +
                            "Try finding the same job on the company's own careers site and paste that URL instead."
            );
        }

        String title       = extractTitle(doc);
        String company     = extractCompany(doc);
        String location    = extractLocation(doc);
        String description = extractDescription(doc);
        String skills      = extractSkills(description + " " + title);
        int[]  salary      = extractSalary(description);

        Job job = Job.builder()
                .url(url)
                .title(title)
                .company(company)
                .location(location)
                .description(description)
                .requiredSkills(skills)
                .salaryMin(salary != null ? salary[0] : null)
                .salaryMax(salary != null ? salary[1] : null)
                .build();

        log.info("Scraped via Jsoup: {} at {}", title, company);
        return toResponse(jobRepository.save(job));
    }

    // ─── JSearch API — Indeed and LinkedIn ───────────────────────────────────
    // Free tier: rapidapi.com/letscrape-6bRBa3QguO5/api/jsearch
    // IMPORTANT: you must click "Subscribe to Test" on RapidAPI even for
    // the free plan — having a key is not enough, subscription is separate.
    // Add to application.properties: rapidapi.key=YOUR_KEY_HERE

    private JobResponse scrapeViaJSearch(String url, String domain) {
        if (rapidApiKey == null || rapidApiKey.isBlank()) {
            throw new ScrapeFailedException(
                    domain + " blocks automated scraping. " +
                            "To support " + domain + " links, add your RapidAPI key to application.properties " +
                            "(rapidapi.key=YOUR_KEY) after subscribing to JSearch at rapidapi.com. " +
                            "For now, find this job on the company's careers page and paste that URL instead."
            );
        }

        try {
            // Extract the job key from the URL to build a targeted search query.
            // JSearch's /search endpoint is more reliable than /job-details
            // because /job-details uses JSearch's own internal ID format
            // (not Indeed's jk parameter directly).
            String jk = extractQueryParam(url, "jk");
            String searchQuery = (jk != null)
                    ? "jobkey:" + jk                  // Indeed-specific search hint
                    : URLEncoder.encode(url, StandardCharsets.UTF_8);

            // Use /search with the job URL so JSearch can find the exact listing
            String apiUrl = "https://jsearch.p.rapidapi.com/search?query=" +
                    URLEncoder.encode(url, StandardCharsets.UTF_8) +
                    "&num_pages=1&page=1";

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(12))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("X-RapidAPI-Key", rapidApiKey)
                    .header("X-RapidAPI-Host", "jsearch.p.rapidapi.com")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());

            log.debug("JSearch API status: {}", response.statusCode());

            if (response.statusCode() == 403) {
                throw new ScrapeFailedException(
                        "RapidAPI returned 403 — this usually means you haven't subscribed to " +
                                "the JSearch API yet. Go to rapidapi.com/letscrape-6bRBa3QguO5/api/jsearch " +
                                "and click 'Subscribe to Test' on the Basic (free) plan, then try again."
                );
            }

            if (response.statusCode() == 429) {
                throw new ScrapeFailedException(
                        "JSearch API rate limit reached for this month (200 requests free). " +
                                "Upgrade your RapidAPI plan or paste the company's direct careers page URL instead."
                );
            }

            if (response.statusCode() != 200) {
                throw new ScrapeFailedException(
                        "Job API returned status " + response.statusCode() + ". " +
                                "Try again shortly or use the company's own careers page URL."
                );
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode data = root.path("data");

            if (!data.isArray() || data.size() == 0) {
                throw new ScrapeFailedException(
                        "Could not find that job listing. It may have been removed from " +
                                domain + ". Try the company's own careers page."
                );
            }

            JsonNode jobNode = data.get(0);
            String title    = jobNode.path("job_title").asText("Unknown Title");
            String company  = jobNode.path("employer_name").asText("Unknown Company");
            String location = buildLocation(jobNode);
            String desc     = jobNode.path("job_description").asText("");
            String skills   = extractSkills(desc + " " + title);
            int[]  salary   = extractSalaryFromNode(jobNode);

            Job job = Job.builder()
                    .url(url)
                    .title(title)
                    .company(company)
                    .location(location)
                    .description(desc)
                    .requiredSkills(skills)
                    .salaryMin(salary != null ? salary[0] : null)
                    .salaryMax(salary != null ? salary[1] : null)
                    .build();

            log.info("Scraped via JSearch: {} at {}", title, company);
            return toResponse(jobRepository.save(job));

        } catch (ScrapeFailedException e) {
            throw e;
        } catch (Exception e) {
            log.error("JSearch API error for URL: {}", url, e);
            throw new ScrapeFailedException(
                    "Failed to fetch that job from " + domain + ". " +
                            "Try the company's own careers page URL instead."
            );
        }
    }

    // ─── Field extractors (Jsoup) ─────────────────────────────────────────────

    private String extractTitle(Document doc) {
        String[] selectors = {
                ".app-title", "[data-qa='job-title']",
                ".job__title", "h1.job-post-label",
                ".posting-headline h2",
                "[data-automation-id='jobPostingHeader']",
                "h1.ashby-job-posting-brief-title",
                "h1.job-title", "h1.title", ".job-title",
                "[class*='job-title']", "[class*='JobTitle']"
        };
        for (String sel : selectors) {
            Element el = doc.selectFirst(sel);
            if (el != null && !el.text().isBlank()) return el.text().trim();
        }
        Element h1 = doc.selectFirst("h1");
        return h1 != null ? h1.text().trim() : "Unknown Title";
    }

    private String extractCompany(Document doc) {
        String[] selectors = {
                ".company-name", ".employer-name",
                "[data-company]", "[class*='company']",
                "[class*='Company']", "[class*='employer']", ".orgname"
        };
        for (String sel : selectors) {
            Element el = doc.selectFirst(sel);
            if (el != null && !el.text().isBlank()) return el.text().trim();
        }
        return "Unknown Company";
    }

    private String extractLocation(Document doc) {
        String[] selectors = {
                ".location", ".job-location",
                "[data-qa='job-location']",
                "[class*='location']", "[class*='Location']"
        };
        for (String sel : selectors) {
            Element el = doc.selectFirst(sel);
            if (el != null && !el.text().isBlank()) return el.text().trim();
        }
        return "";
    }

    private String extractDescription(Document doc) {
        String[] selectors = {
                "#content", ".job__description",
                "[data-qa='job-description']",
                ".posting-description",
                "[data-automation-id='jobPostingDescription']",
                "h1.ashby-job-posting-brief-description",
                "[class*='job-description']", "[class*='JobDescription']",
                "[class*='description']", "article", ".content"
        };
        for (String sel : selectors) {
            Element el = doc.selectFirst(sel);
            if (el != null && !el.text().isBlank()) return el.text().trim();
        }
        return doc.body() != null ? doc.body().text() : "";
    }

    // ─── Shared helpers ───────────────────────────────────────────────────────

    String extractSkills(String text) {
        String lower = text.toLowerCase();
        List<String> found = new ArrayList<>();
        for (String kw : TECH_KEYWORDS) {
            if (lower.contains(kw.toLowerCase())) found.add(kw);
        }
        return String.join(",", found);
    }

    public int[] extractSalary(String text) {
        Pattern p = Pattern.compile(
                "\\$([\\d,]+)k?\\s*[-–]\\s*\\$([\\d,]+)k?|\\$([\\d,]+)k?",
                Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text);
        int min = 0, max = 0;
        while (m.find()) {
            if (m.group(1) != null && m.group(2) != null) {
                return new int[]{ parseSalary(m.group(1)), parseSalary(m.group(2)) };
            } else if (m.group(3) != null && min == 0) {
                min = parseSalary(m.group(3));
            }
        }
        return min > 0 ? new int[]{ min, max } : null;
    }

    private int[] extractSalaryFromNode(JsonNode node) {
        double min = node.path("job_min_salary").asDouble(0);
        double max = node.path("job_max_salary").asDouble(0);
        return (min > 0 || max > 0) ? new int[]{ (int) min, (int) max } : null;
    }

    private String buildLocation(JsonNode node) {
        String city    = node.path("job_city").asText("");
        String state   = node.path("job_state").asText("");
        String country = node.path("job_country").asText("");
        List<String> parts = new ArrayList<>();
        if (!city.isBlank())    parts.add(city);
        if (!state.isBlank())   parts.add(state);
        if (!country.isBlank() && !country.equalsIgnoreCase("us")) parts.add(country);
        return String.join(", ", parts);
    }

    private String extractDomain(String url) {
        try {
            String host = URI.create(url).getHost();
            return host != null ? host.toLowerCase() : "";
        } catch (Exception e) { return ""; }
    }

    private String extractQueryParam(String url, String param) {
        try {
            String query = URI.create(url).getQuery();
            if (query == null) return null;
            for (String kv : query.split("&")) {
                String[] parts = kv.split("=", 2);
                if (parts.length == 2 && parts[0].equals(param)) return parts[1];
            }
        } catch (Exception ignored) {}
        return null;
    }

    private int parseSalary(String raw) {
        int val = Integer.parseInt(raw.replace(",", "").trim());
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