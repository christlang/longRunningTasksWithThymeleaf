package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class IndexController {

    @GetMapping("/")
    public String index(Model model) {

        model.addAttribute("data", "1234 567  as");

        return "index";
    }


    @GetMapping("longRunning")
    public String longRunning(Model model) throws InterruptedException {
        for (int i=0; i<90; ++i) {
            Thread.sleep(1000);
            log.info("waited {} seconds", i);
        }
        log.info("finished");

        return "longRunning";
    }
}
