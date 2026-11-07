package com.fivewheels.fraud;

import com.fivewheels.fraud.model.CheckResult;
import com.fivewheels.fraud.model.SignupRecord;
import com.fivewheels.fraud.repository.SignupRecordRepository;
import com.fivewheels.fraud.service.VerificationService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** All the web pages: home, signup, admin dashboard, signup detail, and the review buttons. */
@Controller
public class PageController {

    /** The checks, in the order the dashboard shows them. */
    private static final List<String> CHECK_NAMES =
            List.of("EmailCheck", "BotCheck", "PhoneCheck", "DuplicateCheck", "ImpersonationCheck");

    private final SignupRecordRepository repository;
    private final VerificationService verificationService;

    public PageController(SignupRecordRepository repository, VerificationService verificationService) {
        this.repository = repository;
        this.verificationService = verificationService;
    }

    // ------------------------------------------------------------------ home

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("reviewFrom", RiskEngine.REVIEW_THRESHOLD);
        model.addAttribute("blockFrom", RiskEngine.BLOCK_THRESHOLD);
        model.addAttribute("total", repository.count());
        return "index";
    }

    // ---------------------------------------------------------------- signup

    @GetMapping("/signup")
    public String signup(Model model) {
        // The SERVER stamps the moment the form opened, so BotCheck never depends on the visitor's clock.
        model.addAttribute("formLoadedAt", System.currentTimeMillis());
        return "signup";
    }

    // ------------------------------------------------------------- dashboard

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(defaultValue = "all") String filter,
                            @RequestParam(defaultValue = "") String q,
                            Model model) {
        List<SignupRecord> all = repository.findAllByOrderByCreatedAtDesc();

        int approved = 0;
        int review = 0;
        int blocked = 0;
        int pending = 0;
        long scoreSum = 0;
        for (SignupRecord r : all) {
            scoreSum += r.getRiskScore();
            if ("APPROVE".equals(r.getDecision())) {
                approved++;
            } else if ("REVIEW".equals(r.getDecision())) {
                review++;
            } else if ("BLOCK".equals(r.getDecision())) {
                blocked++;
            }
            if (r.isNeedsReview()) {
                pending++;
            }
        }
        int total = all.size();

        // which signups to list
        String search = q.trim().toLowerCase(Locale.ROOT);
        List<SignupRecord> rows = new ArrayList<>();
        for (SignupRecord r : all) {
            if (matchesFilter(r, filter) && matchesSearch(r, search)) {
                rows.add(r);
            }
        }

        model.addAttribute("rows", rows);
        model.addAttribute("filter", filter);
        model.addAttribute("q", q);

        model.addAttribute("total", total);
        model.addAttribute("approved", approved);
        model.addAttribute("review", review);
        model.addAttribute("blocked", blocked);
        model.addAttribute("pending", pending);
        model.addAttribute("avgScore", total == 0 ? 0 : Math.round((float) scoreSum / total));
        model.addAttribute("pctApprove", percent(approved, total));
        model.addAttribute("pctReview", percent(review, total));
        model.addAttribute("pctBlock", percent(blocked, total));
        model.addAttribute("bars", triggerBars(all));
        return "dashboard";
    }

    // ---------------------------------------------------------------- detail

    @GetMapping("/dashboard/{id}")
    public String detail(@PathVariable Long id, Model model) {
        SignupRecord record = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No such signup"));
        model.addAttribute("r", record);
        model.addAttribute("reviewFrom", RiskEngine.REVIEW_THRESHOLD);
        model.addAttribute("blockFrom", RiskEngine.BLOCK_THRESHOLD);
        return "detail";
    }

    @PostMapping("/dashboard/{id}/review")
    public String review(@PathVariable Long id, @RequestParam String action) {
        String status;
        if ("approve".equals(action)) {
            status = SignupRecord.APPROVED;
        } else if ("reject".equals(action)) {
            status = SignupRecord.REJECTED;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown action");
        }
        verificationService.setReviewStatus(id, status)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No such signup"));
        return "redirect:/dashboard/" + id;
    }

    // --------------------------------------------------------------- helpers

    private static boolean matchesFilter(SignupRecord r, String filter) {
        return switch (filter) {
            case "approve" -> "APPROVE".equals(r.getDecision());
            case "review" -> "REVIEW".equals(r.getDecision());
            case "block" -> "BLOCK".equals(r.getDecision());
            case "pending" -> r.isNeedsReview();
            default -> true;
        };
    }

    private static boolean matchesSearch(SignupRecord r, String search) {
        if (search.isEmpty()) {
            return true;
        }
        return contains(r.getFullName(), search)
                || contains(r.getEmail(), search)
                || contains(r.getPhone(), search)
                || contains(r.getIpAddress(), search);
    }

    private static boolean contains(String value, String search) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(search);
    }

    private static int percent(int part, int total) {
        return total == 0 ? 0 : Math.round(part * 100f / total);
    }

    /** For each check: in how many signups did it find something suspicious? */
    private static List<Bar> triggerBars(List<SignupRecord> all) {
        int[] counts = new int[CHECK_NAMES.size()];
        for (SignupRecord r : all) {
            for (CheckResult c : r.getResults()) {
                int index = CHECK_NAMES.indexOf(c.getCheckName());
                if (index >= 0 && c.getRiskScore() > 0) {
                    counts[index]++;
                }
            }
        }
        int max = 1;
        for (int c : counts) {
            max = Math.max(max, c);
        }
        List<Bar> bars = new ArrayList<>();
        for (int i = 0; i < counts.length; i++) {
            bars.add(new Bar(CHECK_NAMES.get(i), counts[i], Math.round(counts[i] * 100f / max)));
        }
        return bars;
    }

    /** One bar of the "which check catches the most" chart. */
    public static class Bar {
        private final String label;
        private final int count;
        private final int width;

        public Bar(String label, int count, int width) {
            this.label = label;
            this.count = count;
            this.width = width;
        }

        public String getLabel() { return label; }
        public int getCount() { return count; }
        public int getWidth() { return width; }
    }
}
