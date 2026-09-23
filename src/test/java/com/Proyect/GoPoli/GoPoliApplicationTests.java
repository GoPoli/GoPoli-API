package com.Proyect.GoPoli;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Full-context smoke test. Disabled in the default unit suite because it
 * requires a live Postgres (localhost:5432 or SPRING_DATASOURCE_*).
 * Re-enable when an integration environment with DB is available.
 */
@SpringBootTest
@Disabled("Requires Postgres; excluded from unit suite without Docker/DB")
class GoPoliApplicationTests {

	@Test
	void contextLoads() {
	}

}
