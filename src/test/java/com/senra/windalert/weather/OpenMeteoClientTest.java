package com.senra.windalert.weather;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.senra.windalert.config.WindAlertProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenMeteoClientTest {

    private OpenMeteoClient clientWith(int start, int end) {
        WindAlertProperties props = new WindAlertProperties();
        props.setLatitude(-22.94113);
        props.setLongitude(-43.19211);
        props.setForecastStartHour(start);
        props.setForecastEndHour(end);
        return new OpenMeteoClient(props, new ObjectMapper());
    }

    @Test
    void retornaRajadaMaximaDentroDaJanela() {
        OpenMeteoClient client = clientWith(8, 20);
        String json = """
                {
                  "hourly": {
                    "time": ["2026-10-15T06:00","2026-10-15T07:00","2026-10-15T08:00","2026-10-15T12:00","2026-10-15T19:00","2026-10-15T20:00","2026-10-15T21:00"],
                    "wind_gusts_10m": [99.9,88.8,15.5,45.0,30.0,60.0,77.7]
                  }
                }
                """;
        assertEquals(60.0, client.extractMaxWindGust(json), 0.001);
    }

    @Test
    void ignoraValoresNulosDeRajada() {
        OpenMeteoClient client = clientWith(8, 20);
        String json = """
                {
                  "hourly": {
                    "time": ["2026-10-15T08:00","2026-10-15T09:00","2026-10-15T10:00"],
                    "wind_gusts_10m": [null,12.5,null]
                  }
                }
                """;
        assertEquals(12.5, client.extractMaxWindGust(json), 0.001);
    }

    @Test
    void retornaZeroQuandoNenhumDadoCaiNaJanela() {
        OpenMeteoClient client = clientWith(8, 20);
        String json = """
                {
                  "hourly": {
                    "time": ["2026-10-15T06:00","2026-10-15T07:00"],
                    "wind_gusts_10m": [10.0,20.0]
                  }
                }
                """;
        assertEquals(0.0, client.extractMaxWindGust(json), 0.001);
    }

    @Test
    void montaUrlComCoordenadasTimeZoneEForecastDays() {
        OpenMeteoClient client = clientWith(8, 20);
        String url = client.buildUrl();
        assertTrue(url.contains("latitude=-22.941130"), url);
        assertTrue(url.contains("longitude=-43.192110"), url);
        assertTrue(url.contains("hourly=wind_gusts_10m"), url);
        assertTrue(url.contains("timezone=America/Sao_Paulo"), url);
        assertTrue(url.contains("forecast_days=1"), url);
    }
}
