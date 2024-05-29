package rs.edu.raf.banka1.cucumber;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;

@CucumberOptions(features = "src/test/resources", glue = "rs.edu.raf.banka1.cucumber", tags = "not @Ignore")
@RunWith(Cucumber.class)
@ActiveProfiles("int_test")
public class CucumberIntegrationTest {
}
