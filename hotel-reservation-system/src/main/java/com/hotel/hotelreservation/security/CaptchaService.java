package com.hotel.hotelreservation.security;

/** Security helper for authentication, token validation, or abuse prevention. */

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

@Service
public class CaptchaService {
    @Value("${app.captcha.enabled:false}")
    private boolean enabled;
    @Value("${app.captcha.secret:}")
    private String secret;
    @Value("${app.captcha.verify-url}")
    private String verifyUrl;

    private final RestClient restClient = RestClient.create();

    // Helper function that isValid for the calling workflow.
    public boolean isValid(String token, String remoteIp) {
        if (!enabled) return true;
        if (token == null || token.isBlank() || secret == null || secret.isBlank()) return false;

        var form = new LinkedMultiValueMap<String, String>();
        form.add("secret", secret);
        form.add("response", token);
        if (remoteIp != null && !remoteIp.isBlank()) form.add("remoteip", remoteIp);
        try {
            CaptchaResponse result = restClient.post()
                .uri(verifyUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(CaptchaResponse.class);
            return result != null && result.success();
        } catch (Exception ex) {
            return false;
        }
    }

    public record CaptchaResponse(boolean success) {}
}
