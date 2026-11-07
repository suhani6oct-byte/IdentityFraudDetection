package com.fivewheels.fraud.demo;

import com.fivewheels.fraud.model.IdentityRequest;
import com.fivewheels.fraud.repository.SignupRecordRepository;
import com.fivewheels.fraud.service.VerificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * When the app starts, push a set of labelled demo signups through the REAL pipeline
 * (same checks, same scoring, saved in the same table) so the dashboard is not empty.
 * Switch it off with  fraud.seed-enabled=false  in application.properties.
 */
@Component
public class DemoDataSeeder implements CommandLineRunner {

    private static final String CHROME =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36";
    private static final String ANDROID =
            "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Mobile Safari/537.36";

    private final VerificationService service;
    private final SignupRecordRepository repository;
    private final boolean enabled;

    public DemoDataSeeder(VerificationService service,
                          SignupRecordRepository repository,
                          @Value("${fraud.seed-enabled:false}") boolean enabled) {
        this.service = service;
        this.repository = repository;
        this.enabled = enabled;
    }

    @Override
    public void run(String... args) {
        if (!enabled || repository.count() > 0) {
            return;
        }
        try {
            seedNormalPeople();
            seedBotFarm();
            seedImpersonators();
            seedFakeEmails();
            seedFakePhones();
            seedSameValueTwice();
            seedMixed();
            System.out.println(">>> Demo data loaded: " + repository.count()
                    + " signups. Open http://localhost:8080/dashboard");
        } catch (RuntimeException e) {
            // a demo helper must never stop the app from starting
            System.out.println(">>> Demo data could not be loaded: " + e);
        }
    }

    // ---------- 12 ordinary people: should all be APPROVED ----------
    private void seedNormalPeople() {
        String[][] people = {
                {"Rahul Sharma", "rahul.sharma@gmail.com", "9371846205"},
                {"Priya Nair", "priya.nair@outlook.com", "8264019375"},
                {"Amit Verma", "amit.verma@yahoo.com", "7012398456"},
                {"Sneha Iyer", "sneha.iyer@gmail.com", "9845123067"},
                {"Karan Mehta", "karan.mehta@gmail.com", "6283950174"},
                {"Ananya Gupta", "ananya.gupta@outlook.com", "9810467325"},
                {"Vikram Singh", "vikram.singh@gmail.com", "7905281643"},
                {"Neha Kapoor", "neha.kapoor@yahoo.com", "8130674529"},
                {"Rohan Das", "rohan.das@gmail.com", "9654023871"},
                {"Pooja Reddy", "pooja.reddy@gmail.com", "7389104562"},
                {"Arjun Patel", "arjun.patel@outlook.com", "9022516437"},
                {"Kavya Menon", "kavya.menon@gmail.com", "8801937264"},
        };
        for (int i = 0; i < people.length; i++) {
            send(people[i][0], people[i][1], people[i][2],
                    "49.36." + (10 + i) + "." + (20 + i),
                    i % 3 == 0 ? ANDROID : CHROME,
                    "", 9000 + i * 2300L);
        }
    }

    // ---------- a script creating accounts in bulk from one address ----------
    private void seedBotFarm() {
        for (int i = 1; i <= 6; i++) {
            send("user" + (1000 + i), "xk" + (2900 + i) + "fj@mailinator.com", "980000000" + i,
                    "103.77.1.1", "python-requests/2.31.0",
                    i % 2 == 1 ? "http://spam.example" : "", 300);
        }
    }

    // ---------- pretending to be someone else ----------
    private void seedImpersonators() {
        send("Rahu1 Sharrna", "rahu1.sharrna@gmail.com", "9712648350", "152.58.9.9", CHROME, "", 15000);
        send("Customer Support", "contact.us@gmail.com", "8605173942", "152.58.9.10", CHROME, "", 14000);
        send("Elon Musk", "elon.musk.official@gmail.com", "9506281473", "152.58.9.11", CHROME, "", 16000);
    }

    // ---------- throw-away email addresses ----------
    private void seedFakeEmails() {
        send("Neeraj Bhatt", "neeraj.bhatt@mailinator.com", "9930752841", "117.200.1.5", CHROME, "", 12000);
        send("Pooja Kapoor", "pooja.kapoor@yopmail.com", "8420963175", "117.200.2.5", ANDROID, "", 11000);
    }

    // ---------- invented phone numbers ----------
    private void seedFakePhones() {
        send("Karthik Rao", "karthik.rao@gmail.com", "9999999999", "182.70.4.4", CHROME, "", 13000);
        send("Sneha Pillai", "sneha.pillai@gmail.com", "9876543210", "182.70.4.5", CHROME, "", 13500);
        send("Imran Khan", "imran.khan@gmail.com", "9898989898", "182.70.4.6", ANDROID, "", 12500);
    }

    // ---------- the same value typed into two fields (DuplicateCheck) ----------
    private void seedSameValueTwice() {
        send("sam.rivera@gmail.com", "sam.rivera@gmail.com", "9830174526", "59.92.3.3", CHROME, "", 10500);
    }

    // ---------- several signs at once ----------
    private void seedMixed() {
        send("Ravi Teja", "xq8z3kd1@tempmail.com", "9000000000", "45.115.8.8", "curl/8.4.0", "", 400);
    }

    /** Builds one signup that was typed in `fillMs` milliseconds and sends it through the engine. */
    private void send(String name, String email, String phone, String ip, String userAgent,
                      String honeypot, long fillMs) {
        IdentityRequest r = new IdentityRequest(name, email, phone, ip);
        r.setUserAgent(userAgent);
        r.setHoneypot(honeypot);
        r.setDeviceId("demo-" + Math.abs((name + email).hashCode()));
        r.setFormLoadedAt(System.currentTimeMillis() - fillMs);
        service.verify(r);
    }
}