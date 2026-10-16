package com.senra.windalert.state;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.senra.windalert.config.WindAlertProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StateStoreTest {

    private StateStore storeFor(Path stateFile) {
        WindAlertProperties props = new WindAlertProperties();
        props.setStateFile(stateFile.toString());
        return new StateStore(props, new ObjectMapper());
    }

    @Test
    void loadSemArquivoRetornaPadraoAberto(@TempDir Path tempDir) {
        StateStore store = storeFor(tempDir.resolve("state.json"));
        assertFalse(store.load().awningClosed());
    }

    @Test
    void roundTripPersisteECriaDiretorio(@TempDir Path tempDir) throws Exception {
        Path stateFile = tempDir.resolve("data/state.json");
        StateStore store = storeFor(stateFile);

        store.save(new WindAlertState(true));

        assertTrue(Files.exists(stateFile));
        assertTrue(Files.readString(stateFile).contains("\"awningClosed\""));
        assertEquals(true, store.load().awningClosed());
    }
}
