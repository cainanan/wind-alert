package com.senra.windalert.service;

import com.senra.windalert.config.WindAlertProperties;
import com.senra.windalert.state.StateStore;
import com.senra.windalert.state.WindAlertState;
import com.senra.windalert.weather.OpenMeteoClient;
import com.senra.windalert.whatsapp.WhatsAppSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WindAlertServiceTest {

    private OpenMeteoClient openMeteoClient;
    private StateStore stateStore;
    private WhatsAppSender whatsAppSender;
    private WindAlertProperties properties;
    private WindAlertService service;

    @BeforeEach
    void setUp() {
        openMeteoClient = mock(OpenMeteoClient.class);
        stateStore = mock(StateStore.class);
        whatsAppSender = mock(WhatsAppSender.class);

        properties = new WindAlertProperties();
        properties.setThresholdKmh(40);
        properties.setMessageWind("Vai ventar, fecha o toldo!");
        properties.setMessageCalm("Já passou o vento, pode abrir o toldo.");
        properties.setDryRun(false);

        service = new WindAlertService(openMeteoClient, stateStore, whatsAppSender, properties);
    }

    @Test
    void transicaoAbertoParaFechadoEnviaAvisoDeVento() {
        when(openMeteoClient.fetchMaxWindGust()).thenReturn(55.0);
        when(stateStore.load()).thenReturn(new WindAlertState(false));

        service.checkAndNotify();

        verify(whatsAppSender).send("Vai ventar, fecha o toldo!");
        verify(stateStore).save(new WindAlertState(true));
    }

    @Test
    void transicaoFechadoParaAbertoEnviaAvisoDeCalmaria() {
        when(openMeteoClient.fetchMaxWindGust()).thenReturn(10.0);
        when(stateStore.load()).thenReturn(new WindAlertState(true));

        service.checkAndNotify();

        verify(whatsAppSender).send("Já passou o vento, pode abrir o toldo.");
        verify(stateStore).save(new WindAlertState(false));
    }

    @Test
    void mantemFechadoNaoEnviaNemPersiste() {
        when(openMeteoClient.fetchMaxWindGust()).thenReturn(45.0);
        when(stateStore.load()).thenReturn(new WindAlertState(true));

        service.checkAndNotify();

        verify(whatsAppSender, never()).send(anyString());
        verify(stateStore, never()).save(any());
    }

    @Test
    void mantemAbertoNaoEnviaNemPersiste() {
        when(openMeteoClient.fetchMaxWindGust()).thenReturn(5.0);
        when(stateStore.load()).thenReturn(new WindAlertState(false));

        service.checkAndNotify();

        verify(whatsAppSender, never()).send(anyString());
        verify(stateStore, never()).save(any());
    }

    @Test
    void dryRunPersisteEstadoMasNaoExecutaPhp(@TempDir Path tempDir) throws Exception {
        properties.setDryRun(true);
        properties.setWhatsapperPhp("/usr/bin/touch");
        Path marker = tempDir.resolve("php-foi-chamado");
        properties.setWhatsapperScript(marker.toString());

        WhatsAppSender realSender = new WhatsAppSender(properties);
        WindAlertService dryRunService = new WindAlertService(openMeteoClient, stateStore, realSender, properties);

        when(openMeteoClient.fetchMaxWindGust()).thenReturn(50.0);
        when(stateStore.load()).thenReturn(new WindAlertState(false));

        dryRunService.checkAndNotify();

        verify(stateStore).save(new WindAlertState(true));
        assertFalse(Files.exists(marker), "em dry-run o PHP não deve ser executado");
    }
}
