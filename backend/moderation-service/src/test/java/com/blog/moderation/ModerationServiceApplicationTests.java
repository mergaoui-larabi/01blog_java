package com.blog.moderation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Requires live PostgreSQL database on port 5436")
class ModerationServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
