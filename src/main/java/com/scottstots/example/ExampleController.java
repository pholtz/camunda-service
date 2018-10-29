package com.scottstots.example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExampleController
{
	@GetMapping("/test")
	public String test() {
		return "test";
	}
}
