package edu.ecommerce.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_ON_EXIT=FALSE",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AppTest {

    @Test
    void testApplicationContextLoads(ApplicationContext context) {
        assertThat(context).isNotNull();
    }

    @Test
    void testSpringBootApplicationAnnotation() {
        assertThat(App.class.getAnnotation(SpringBootApplication.class)).isNotNull();
    }

    @Test
    void testEnableJpaRepositoriesAnnotation() {
        assertThat(App.class.getAnnotation(EnableJpaRepositories.class)).isNotNull();
    }

    @Test
    void testEntityScanAnnotation() {
        assertThat(App.class.getAnnotation(EntityScan.class)).isNotNull();
    }

    @Test
    void testMainMethodExists() {
        try {
            App.class.getMethod("main", String[].class);
            assertThat(true).isTrue();
        } catch (NoSuchMethodException e) {
            fail("main method not found");
        }
    }
}
