package com.fivewheels.fraud;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

@GetMapping("/dashboard")
public String dashboard() {
    return "dashboard";
}

@GetMapping("/signup")
public String signup() {
    return "signup";
}


}
