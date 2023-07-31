package mooncake.example.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class BankStudyApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(BankStudyApplication.class, args);
		String[] beanNames = context.getBeanDefinitionNames();
		// iter
	}

}
