package com.techtest.techtest;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RestCallUtil {
    @Autowired
    private RestTemplate restTemplate;

    public <T> Status post(String url, T request){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<T> entity = new HttpEntity<>(request, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        return new Status(response.getStatusCodeValue());
    }

    @AllArgsConstructor
    public static class Status{
        private final int code;

        public boolean isOk(){
            return 200<=code && code < 300;
        }
    }
}
