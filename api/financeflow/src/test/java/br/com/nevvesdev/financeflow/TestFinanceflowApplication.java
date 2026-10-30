package br.com.nevvesdev.financeflow;

import org.springframework.boot.SpringApplication;

public class TestFinanceflowApplication {

	public static void main(String[] args) {
		SpringApplication.from(FinanceflowApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
