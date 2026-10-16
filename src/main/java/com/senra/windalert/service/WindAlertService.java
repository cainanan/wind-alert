package com.senra.windalert.service;

import com.senra.windalert.config.WindAlertProperties;
import com.senra.windalert.state.StateStore;
import com.senra.windalert.state.WindAlertState;
import com.senra.windalert.weather.OpenMeteoClient;
import com.senra.windalert.whatsapp.WhatsAppSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Service
public class WindAlertService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(WindAlertService.class);

    private final OpenMeteoClient openMeteoClient;
    private final StateStore stateStore;
    private final WhatsAppSender whatsAppSender;
    private final WindAlertProperties properties;

    public WindAlertService(OpenMeteoClient openMeteoClient,
                            StateStore stateStore,
                            WhatsAppSender whatsAppSender,
                            WindAlertProperties properties) {
        this.openMeteoClient = openMeteoClient;
        this.stateStore = stateStore;
        this.whatsAppSender = whatsAppSender;
        this.properties = properties;
    }

    @Override
    public void run(String... args) {
        checkAndNotify();
    }

    public void checkAndNotify() {
        double maxGust = openMeteoClient.fetchMaxWindGust();
        boolean comVento = maxGust >= properties.getThresholdKmh();

        WindAlertState state = stateStore.load();

        log.info("Rajada máxima na janela: {} km/h (limite {} km/h) -> comVento={}, awningClosed={}",
                maxGust, properties.getThresholdKmh(), comVento, state.awningClosed());

        if (comVento && !state.awningClosed()) {
            log.info("Transição: toldo aberto -> fechado. Enviando aviso de vento.");
            whatsAppSender.send(properties.getMessageWind());
            stateStore.save(new WindAlertState(true));
        } else if (!comVento && state.awningClosed()) {
            log.info("Transição: toldo fechado -> aberto. Enviando aviso de calmaria.");
            whatsAppSender.send(properties.getMessageCalm());
            stateStore.save(new WindAlertState(false));
        } else {
            log.info("Sem mudança de estado; nenhuma mensagem enviada.");
        }
    }
}
