package com.jlss.placelive.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.jlss.placelive.geofencing", "com.jlss.placelive.commonlib"})
public class PlaceLiveSearchServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlaceLiveSearchServiceApplication.class, args);
	}

}
