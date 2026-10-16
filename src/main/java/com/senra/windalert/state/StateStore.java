package com.senra.windalert.state;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.senra.windalert.config.WindAlertProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class StateStore {

    private static final Logger log = LoggerFactory.getLogger(StateStore.class);

    private final ObjectMapper objectMapper;
    private final Path stateFile;

    public StateStore(WindAlertProperties properties, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.stateFile = Path.of(properties.getStateFile());
    }

    public WindAlertState load() {
        if (!Files.exists(stateFile)) {
            log.info("Arquivo de estado não encontrado ({}); usando padrão awningClosed=false", stateFile);
            return new WindAlertState(false);
        }
        try {
            return objectMapper.readValue(stateFile.toFile(), WindAlertState.class);
        } catch (IOException e) {
            log.warn("Não foi possível ler o arquivo de estado {}; usando padrão awningClosed=false", stateFile, e);
            return new WindAlertState(false);
        }
    }

    public void save(WindAlertState state) {
        try {
            if (stateFile.getParent() != null) {
                Files.createDirectories(stateFile.getParent());
            }
            objectMapper.writeValue(stateFile.toFile(), state);
            log.info("Estado persistido em {}: awningClosed={}", stateFile, state.awningClosed());
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível gravar o arquivo de estado " + stateFile, e);
        }
    }
}
