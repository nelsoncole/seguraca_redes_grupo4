package com.seguranca.redes.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.seguranca.redes.BrevoConfig;

import java.util.*;

@Service
public class EmailService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private BrevoConfig brevoConfig;

    private static final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";

    public void enviarEmail(String para, String assunto, String conteudoHtml) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", brevoConfig.getBrevoApiKey());

        Map<String, Object> email = new HashMap<>();
        email.put("sender", Map.of("name", "Grupo 4 Segurança de Redes", "email", "nelsocole@gmail.com"));
        email.put("to", List.of(Map.of("email", para)));
        email.put("subject", assunto);
        email.put("htmlContent", conteudoHtml);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(email, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(BREVO_URL, request, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("E-mail enviado com sucesso!");
        } else {
            System.out.println("Erro ao enviar e-mail: " + response.getBody());
        }
    }
}
