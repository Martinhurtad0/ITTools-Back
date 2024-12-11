package com.example.ITTools.infrastructure.controllers.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private List<String> marqueeMessages = new ArrayList<>(List.of(
            "Welcome! Make the most of your time here and achieve your goals."
    ));

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/marquee")
    public ResponseEntity<List<String>> getMarqueeMessages() {
        return ResponseEntity.ok(marqueeMessages);
    }

    @PostMapping("/marquee")
    public ResponseEntity<String> addMarqueeMessage(@RequestBody Map<String, String> request) {
        marqueeMessages.add(request.get("text"));
        return ResponseEntity.ok("Message added.");
    }

    @DeleteMapping("/marquee/{index}")
    public ResponseEntity<String> deleteMarqueeMessage(@PathVariable int index) {
        if (index >= 0 && index < marqueeMessages.size()) {
            marqueeMessages.remove(index);
            return ResponseEntity.ok("Message deleted.");
        }
        return ResponseEntity.badRequest().body("Invalid index.");
    }

    @GetMapping("/random-word")
    public ResponseEntity<String> getRandomWord() {
        String url = "https://clientes.api.greenborn.com.ar/public-random-word?c=1&l=6";
        try {
            String randomWord = restTemplate.getForObject(url, String.class);
            return ResponseEntity.ok(randomWord);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching random word: " + e.getMessage());
        }
    }
}
