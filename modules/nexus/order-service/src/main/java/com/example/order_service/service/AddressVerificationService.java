package com.example.order_service.service;

import com.example.danxils_commons.dto.AddressDto;
import com.example.order_service.config.AppProperties;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AddressVerificationService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AddressVerificationService.class);

    private final AppProperties appProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    public AddressDto verifyAndNormalize(AddressDto address) {
        log.info("Verifying address: {}, {}, {}", address.getStreet(), address.getCity(), address.getPostalCode());

        try {
            String query = String.format("%s, %s, %s", address.getStreet(), address.getPostalCode(), address.getCity());
            String url = UriComponentsBuilder.fromHttpUrl(appProperties.getNominatimUrl())
                    .queryParam("q", query)
                    .queryParam("format", "json")
                    .queryParam("addressdetails", 1)
                    .queryParam("limit", 1)
                    .toUriString();

            List<Map<String, Object>> results = restTemplate.getForObject(url, List.class);

            if (results != null && !results.isEmpty()) {
                Map<String, Object> result = results.get(0);
                log.info("Address verified: {}", result.get("display_name"));

                // Here we could update the AddressDto with normalized data and lat/lon
                // For now, we just return the original address, but logged verification success
                // In a real implementation, we would update lat/lon fields in AddressDto if
                // they existed
                return address;
            } else {
                log.warn("Address verification failed: No results found for {}", query);
            }
        } catch (Exception e) {
            log.error("Error verifying address: {}", e.getMessage());
        }

        return address;
    }
}
