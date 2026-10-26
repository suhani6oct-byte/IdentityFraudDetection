### Typo-Domain Detection

**Objective**

The purpose of typo-domain detection is to identify email addresses that use common misspellings of popular email domains.

**Implementation**

The `EmailCheck` class checks the email domain against five predefined suspicious domains:

- `gmial.com`
- `gmaill.com`
- `yaho.com`
- `hotmial.com`
- `outlok.com`

When one of these domains is detected, the system adds **20 risk points** and records the finding *Possible misspelled email domain*.

**Test Cases and Expected Results**

| Input email | Expected typo-domain score |
|---|---:|
| alex@gmial.com | 20 |
| alex@gmaill.com | 20 |
| alex@yaho.com | 20 |
| alex@hotmial.com | 20 |
| alex@outlok.com | 20 |
| alex@gmail.com | 0 |
| alex@yahoo.com | 0 |
| alex@outlook.com | 0 |

**Verification**

The full Maven test suite completed successfully with 53 tests, 0 failures, 0 errors, and 0 skipped tests. This confirms that the current test suite passes with the typo-domain detection changes in place.

**Conclusion**

The feature flags the five configured misspelled domains while avoiding typo-related penalties for the three correctly spelled domains tested.
