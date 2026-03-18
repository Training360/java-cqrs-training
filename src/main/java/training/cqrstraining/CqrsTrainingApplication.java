package training.cqrstraining;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CqrsTrainingApplication {

    static void main(String[] args) {
        SpringApplication.run(CqrsTrainingApplication.class, args);
    }

}
