package com.sist.web.controller;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RouterController {
	@GetMapping("/main")
	public String main_page()
	{
		return "main/main";
	}
}
