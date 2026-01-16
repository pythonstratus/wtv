Here's a simple email you can send:

---

**Subject:** Dev Database Tables Incident - Resolved

Hi Team,

I wanted to share a brief summary of an incident that occurred last night in the Dev environment and the steps we've taken to prevent it from happening again.

**What Happened**

While working late on the WTV web service modernization, I accidentally ran a schema initialization script against our Oracle Dev database. This script was intended for our local H2 test database but was executed against Oracle, which dropped and recreated several tables including TIMENON, TIMETIN, and related tables. This caused a temporary disruption to the Dev environment.

**Resolution**

The tables have been restored and the Dev environment is back to normal. No data was lost in Test or Production environments.

**Root Cause**

The WTV service uses two database configurations:
- **H2 (in-memory)** - for local development and testing with `ddl-auto: create-drop`
- **Oracle** - for Dev/Test/Prod with `ddl-auto: none`

When I connected to Oracle to test the service, the active profile was still set to use H2 settings, which triggered the schema drop and recreate.

**Our Testing Approach Going Forward**

To prevent this from happening again, we are reinforcing the following practices:

1. **Use H2 for local testing** - All development and unit testing will use the embedded H2 database, which resets on each run without touching Oracle
2. **Oracle connections use `ddl-auto: none`** - This ensures Hibernate never modifies Oracle schema automatically
3. **Separate configuration profiles** - Clear separation between `local`, `dev`, `test`, and `prod` profiles
4. **Schema scripts require manual execution** - Any DDL changes to Oracle will only be run manually through TOAD after review

**Benefit of H2 Testing**

Using H2 allows us to rapidly test our services with realistic sample data without risking any disruption to shared Oracle environments. We can reset, rebuild, and retest as many times as needed in isolation.

I apologize for any inconvenience this may have caused. It was a one-time mistake and we've put safeguards in place to ensure it doesn't happen again.

Please let me know if you have any questions or concerns.

Thanks,
[Your Name]

---

Feel free to adjust the tone or add/remove details based on your audience.
