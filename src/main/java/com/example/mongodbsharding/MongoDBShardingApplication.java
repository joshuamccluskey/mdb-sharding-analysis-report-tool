package com.example.mongodbsharding;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MongoDBShardingApplication implements CommandLineRunner {

	@Autowired
	private MongoDBShardingService shardingService;

	@Autowired
	private PDFReportGenerator pdfReportGenerator;

	public static void main(String[] args) {
		SpringApplication.run(MongoDBShardingApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		System.out.println("Fetching sharding status...");
		ShardingStatus shardingStatus = shardingService.getShardingStatus();

		// Print to terminal
		System.out.println("Sharding Status:");
		ObjectMapper objectMapper = new ObjectMapper();
		System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(shardingStatus));

		// Generate PDF Report
		System.out.println("Generating PDF report...");
		pdfReportGenerator.generatePDFReport(shardingStatus, "mongodb_sharding_status_report.pdf");
		System.out.println("PDF report generated successfully!");
	}
}