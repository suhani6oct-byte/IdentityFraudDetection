# Email Risk Detection — Implementation Report

## 1. Objective

The Email Risk Detection module evaluates an email address for possible identity fraud. It identifies invalid email formats, disposable email providers, role-based addresses, suspicious usernames, misspelled email domains, and email addresses that do not resemble the applicant's name.

Each detected indicator contributes risk points. The module returns a risk score and a reason describing the detected indicators. Invalid email addresses receive an immediate score and do not undergo further checks.

The maximum score returned by the EmailCheck module is **100 points**.

## 2. Email Risk Rules and Scoring

| No. | Detection rule | Risk points | Description |
|---:|---|---:|---|
| 1 | Invalid email format | +40 | Rejects an email that is missing required parts or does not match the accepted email format. Stops further email checks. |
| 2 | Disposable email domain | +35 | Detects domains associated with temporary or throwaway email addresses using `disposable-domains.txt`. |
| 3 | Random-looking username | +20 | Identifies suspicious usernames using digit frequency, vowel presence, and consecutive consonant patterns. |
| 4 | Domain typo detection | +20 | Detects known misspellings of common email providers, such as `gmial.com`, `gmaill.com`, `yaho.com`, `hotmial.com`, and `outlok.com`. |
| 5 | Role-based email address | +15 | Detects generic addresses such as `noreply`, `no-reply`, `support`, `info`, and `test`. |
| 6 | Email/name mismatch | +5 | Adds a small risk score when the email username does not reasonably resemble the applicant's supplied name. |
| 7 | Missing mail server (MX) | +30 | Optionally checks whether the domain has mail-exchange records. This check is disabled by default using `fraud.mx-check-enabled=false`. Network or DNS failures must not independently increase the score. |

### Scoring safeguards

- **Early return:** An invalid email receives 40 points, and no other email-risk rules are evaluated.
- **Score cap:** The EmailCheck score must never exceed 100, even when several indicators are detected together.
- **Optional MX checking:** MX verification is disabled by default. It must be explicitly enabled through configuration.
- **Network failure handling:** An unavailable DNS service, timeout, or other network error must not be treated as proof of fraud.
- **Explainable results:** The result should identify the indicators that contributed to the risk score.
- **Disposable-domain maintenance:** The domain list should contain at least 100 unique domain entries, without duplicate entries or blank lines.

## 3. Example Inputs and Outputs

The following examples illustrate the intended scoring behavior. They assume the rules above have been implemented and that optional MX checking is disabled.

### Example 1 — Invalid email address

**Input**

```text
Name: Alex Morgan
Email: not-an-email
```

**Detected indicator:** Invalid email format

**Expected output**

```text
Check: EmailCheck
Risk Score: 40
Reason: Invalid email format
```

**Explanation:** The address does not contain a valid email structure. The module immediately returns 40 points without checking the domain, username, or name similarity.

### Example 2 — Disposable email address

**Input**

```text
Name: Alex Morgan
Email: alex@mailinator.com
```

**Detected indicator:** Disposable email domain

**Expected output**

```text
Check: EmailCheck
Risk Score: 35
Reason: Disposable email domain
```

**Explanation:** The email format is valid, but `mailinator.com` is present in the disposable-domain list. The module adds 35 risk points. No other listed indicator applies in this example.

### Example 3 — Role-based email address

**Input**

```text
Name: Alex Morgan
Email: support@example.com
```

**Detected indicators:**
- Role-based email address: +15
- Email/name mismatch: +5

**Expected output**

```text
Check: EmailCheck
Risk Score: 20
Reason: Role-based email address; Email does not resemble supplied name
```

**Explanation:** The username `support` is a generic role-based address rather than a personal name. It also does not resemble the supplied name, Alex Morgan. The combined score is 20 points.

**Important:** `example.com` is used here as a demonstration domain. These are expected outputs for the specified rules, not results from a newly executed test run.

## 4. Random-Looking Username Heuristic

The `looksRandom()` heuristic identifies usernames that may have been generated automatically or created to obscure a person's identity.

The intended checks are:

1. **High digit proportion:** At least 40% of the username's characters are digits.
2. **Very few or no vowels:** The username contains an unusually low proportion of vowels.
3. **Long consonant sequence:** The username contains six or more consecutive consonants.

A username that satisfies one or more of these conditions may receive 20 risk points. The final implementation should avoid treating a single heuristic as conclusive evidence of fraud.

### Heuristic examples

| Username | Likely behavior | Explanation |
|---|---|---|
| `alexmorgan` | Normal-looking | Contains readable letters and vowels. |
| `alex482917` | Suspicious | A high proportion of the username consists of digits. |
| `xqzvptkr` | Suspicious | Contains a long sequence of consonants with very few vowels. |
| `john2026` | Potential false positive | A legitimate person may include a year or numbers in an email username. |
| `rhythm` | Potential false positive | Some legitimate words naturally contain few vowels. |

## 5. Heuristic Mistakes and Limitations

A heuristic is a practical rule of thumb, not a definitive fraud detector. It can produce both false positives and false negatives.

### False positives

A false positive occurs when a legitimate email is marked suspicious.

Examples include:
- A genuine user includes their birth year or a meaningful number in their username.
- A real name or surname contains an unusual consonant sequence.
- A legitimate business uses a generic role-based address.
- A user's email address does not contain their legal name or preferred name.

**Improvement:** Use the username heuristic as one risk indicator rather than automatically rejecting the email. Combine it with other signals, and test realistic names, numbers, and international naming patterns.

### False negatives

A false negative occurs when a suspicious email is not identified.

Examples include:
- A fraudster uses a simple, readable username.
- A disposable provider is missing from the domain list.
- A misspelled provider domain is not included in the typo list.
- An attacker uses a plausible username that resembles a person's name.

**Improvement:** Maintain the disposable-domain list, test common provider typos, and refine heuristics using representative normal and suspicious email samples.

### Testing and refinement procedure

The email module should be tested against at least:

- 10 normal email addresses.
- 10 random-looking email addresses.
- Two or more known typo-domain examples.
- Disposable and role-based email addresses.
- Invalid and null email values.
- Multiple indicators occurring together.
- The score cap and optional MX behavior.

For each test, record the input, expected result, actual result, and whether it was classified correctly. Identify the worst-performing heuristic and improve it without breaking previously passing tests.

## 6. Configuration and Implementation Requirements

The MX-check configuration should default to:

```properties
fraud.mx-check-enabled=false
```

When disabled, the module should not perform MX lookups. When enabled, it may check whether the domain has mail-exchange records. A missing record may contribute 30 points, but a network or DNS failure must not automatically be interpreted as a missing record.

The final score should be capped before creating the result:

```java
return toCheckResult(Math.min(score, 100), findings);
```

Invalid-format handling must return immediately with 40 points. Disposable-domain scoring must add 35 points, and the implementation must preserve the existing `CheckResult` structure and integration with the overall fraud detection engine.

## 7. Conclusion

The Email Risk Detection module provides an explainable, rule-based approach to identifying potentially suspicious email addresses. It combines format validation, disposable-domain detection, username heuristics, role-based address detection, provider typo detection, name similarity, and optional MX verification.

The score cap ensures that the module never returns more than 100 points. Configurable MX checking avoids unnecessary network requests by default, while explicit findings make the results easier to understand and test.

The module should be considered complete only after the implementation, disposable-domain list, unit tests, and final test results have been verified against these requirements.
