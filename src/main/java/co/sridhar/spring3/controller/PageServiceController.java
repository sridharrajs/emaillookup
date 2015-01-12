package main.java.co.sridhar.spring3.controller;

import main.java.co.sridhar.spring3.utils.SMTPMXLookup;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class PageServiceController {
	
	@RequestMapping("/")
	public ModelAndView helloWorld() {			
		return new ModelAndView("hello", "message", "");
	}	
	
	@RequestMapping(value ="/email", method = RequestMethod.GET)
	@ResponseBody
	public String generateEmail(@RequestParam String firstName,
								@RequestParam String lastName,
								@RequestParam String domain){				
		return SMTPMXLookup.querySMTP(firstName, lastName, domain);
	}
	
}
