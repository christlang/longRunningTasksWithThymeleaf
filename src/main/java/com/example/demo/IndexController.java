package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Controller
public class IndexController {

    private final ExecutorService nonBlockingService = Executors.newCachedThreadPool();

    @GetMapping("/")
    public String index(Model model) {

        model.addAttribute("data", "1234 567  as");

        return "index";
    }


    @GetMapping("longRunning")
    public String longRunning(Model model) throws InterruptedException {
        for (int i=0; i<10; ++i) {
            Thread.sleep(1000);
            log.info("longRunning - waited {} seconds", i);
        }
        log.info("longRunning - finished");

        return "longRunning";
    }

    @GetMapping("/sse")
    public SseEmitter handleSse() {
        SseEmitter emitter = new SseEmitter();
        nonBlockingService.execute(() -> {
            try {
                for (int i = 0; i < 10; ++i) {
                    emitter.send("<!-- update: %s -->".formatted(new Date().toString()));
                    Thread.sleep(1000);
                    log.info("sse - waited {} seconds", i);
                }
                emitter.send("<html><body><h1>Hello world</body></html>", MediaType.TEXT_HTML);
                log.info("sse - finished");

                // we could send more events
                emitter.complete();
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        });
        return emitter;
    }

    @GetMapping("/srb")
    public ResponseEntity<StreamingResponseBody> handleRbe() {
        StreamingResponseBody stream = out -> {
            try {
                for (int i = 0; i < 20; ++i) {
                    String msg = "<!-- update: %s -->".formatted(new Date().toString());
                    out.write(msg.getBytes());
                    out.flush();
                    Thread.sleep(1000);
                    log.info("srb - waited {} seconds", i);
                }
            } catch (Exception ex) {
                log.error("error in StreamingResponseBody", ex);
            }
            log.info("srb - finished");
            String msg = "<html><body><h1>hello world</body></html>";
            out.write(msg.getBytes());
        };
        return new ResponseEntity<>(stream, HttpStatus.OK);
    }

}
