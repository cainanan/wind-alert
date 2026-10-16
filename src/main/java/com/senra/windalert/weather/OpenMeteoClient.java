package com.senra.windalert.weather;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.senra.windalert.config.WindAlertProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;

@Component
public class OpenMeteoClient {

    private static final Logger log = LoggerFactory.getLogger(OpenMeteoClient.class);
    private static final String TIMEZONE = "America/Sao_Paulo";

    private final WindAlertProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public OpenMeteoClient(WindAlertProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public double fetchMaxWindGust() {
        String url = buildUrl();
        log.info("Consultando Open-Meteo: {}", url);

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new RuntimeException("Falha de rede ao consultar o Open-Meteo", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Consulta ao Open-Meteo interrompida", e);
        }

        if (response.statusCode() != 200) {
            throw new RuntimeException("Open-Meteo respondeu HTTP " + response.statusCode());
        }

        return extractMaxWindGust(response.body());
    }

    String buildUrl() {
        return String.format(Locale.ROOT,
                "https://api.open-meteo.com/v1/forecast?latitude=%.6f&longitude=%.6f&hourly=wind_gusts_10m&timezone=%s&forecast_days=1",
                properties.getLatitude(), properties.getLongitude(), TIMEZONE);
    }

    double extractMaxWindGust(String jsonBody) {
        JsonNode root;
        try {
            root = objectMapper.readTree(jsonBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Resposta JSON inválida do Open-Meteo", e);
        }

        JsonNode timeNode = root.path("hourly").path("time");
        JsonNode gustNode = root.path("hourly").path("wind_gusts_10m");
        if (!timeNode.isArray() || !gustNode.isArray()) {
            throw new RuntimeException("Resposta do Open-Meteo sem os campos horários esperados");
        }

        int start = properties.getForecastStartHour();
        int end = properties.getForecastEndHour();

        double max = 0.0;
        boolean found = false;
        int size = Math.min(timeNode.size(), gustNode.size());
        for (int i = 0; i < size; i++) {
            JsonNode gust = gustNode.get(i);
            if (gust == null || gust.isNull() || !gust.isNumber()) {
                continue;
            }
            int hour = hourOf(timeNode.get(i).asText());
            if (hour >= start && hour <= end) {
                double value = gust.asDouble();
                if (!found || value > max) {
                    max = value;
                    found = true;
                }
            }
        }

        if (!found) {
            log.warn("Nenhuma rajada de vento encontrada na janela {}h-{}h", start, end);
        }
        return max;
    }

    private int hourOf(String time) {
        if (time.length() < 13) {
            throw new IllegalArgumentException("Formato de hora inesperado: " + time);
        }
        try {
            return Integer.parseInt(time.substring(11, 13));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Formato de hora inesperado: " + time, e);
        }
    }
}
