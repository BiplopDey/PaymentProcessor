package com.techtest.techtest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RestCallUtil {
    @Autowired
    private RestTemplate restTemplate;

    public <T> ResponseEntity<String> post(String url, T request){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<T> entity = new HttpEntity<>(request, headers);
        return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }
}
