package id.ac.ui.cs.advprog.papikos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableAspectJAutoProxy
public class PapikosApplication {

    public static void main(String[] args) {
        SpringApplication.run(PapikosApplication.class, args);
    }
}

