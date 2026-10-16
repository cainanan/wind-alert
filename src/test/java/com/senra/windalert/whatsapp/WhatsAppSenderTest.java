package com.senra.windalert.whatsapp;

import com.senra.windalert.config.WindAlertProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WhatsAppSenderTest {

    private Path writeFakePhp(Path dir, Path argsFile) throws IOException {
        Path script = dir.resolve("fake-php.sh");
        String content = "#!/bin/sh\nprintf '%s\\n' \"$@\" > " + argsFile + "\nexit 0\n";
        Files.writeString(script, content);
        script.toFile().setExecutable(true);
        return script;
    }

    @Test
    void enviaExecutandoPhpComScriptEMensagemComoArgumentos(@TempDir Path tempDir) throws Exception {
        Path argsFile = tempDir.resolve("args.txt");
        Path fakePhp = writeFakePhp(tempDir, argsFile);

        WindAlertProperties props = new WindAlertProperties();
        props.setDryRun(false);
        props.setWhatsapperPhp(fakePhp.toString());
        props.setWhatsapperScript("/caminho/whatsapper.php");

        new WhatsAppSender(props).send("Vai ventar, fecha o toldo!");

        List<String> args = Files.readAllLines(argsFile);
        assertEquals(List.of("/caminho/whatsapper.php", "Vai ventar, fecha o toldo!"), args);
    }

    @Test
    void dryRunNaoExecutaPhp(@TempDir Path tempDir) throws Exception {
        Path argsFile = tempDir.resolve("args.txt");
        Path fakePhp = writeFakePhp(tempDir, argsFile);

        WindAlertProperties props = new WindAlertProperties();
        props.setDryRun(true);
        props.setWhatsapperPhp(fakePhp.toString());
        props.setWhatsapperScript("/caminho/whatsapper.php");

        new WhatsAppSender(props).send("qualquer mensagem");

        assertFalse(Files.exists(argsFile), "em dry-run o PHP não deve ser executado");
    }

    @Test
    void lancaExcecaoQuandoPhpRetornaExitCodeNaoZero(@TempDir Path tempDir) throws Exception {
        Path script = tempDir.resolve("fail.sh");
        Files.writeString(script, "#!/bin/sh\nexit 7\n");
        script.toFile().setExecutable(true);

        WindAlertProperties props = new WindAlertProperties();
        props.setDryRun(false);
        props.setWhatsapperPhp(script.toString());
        props.setWhatsapperScript("/qualquer");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> new WhatsAppSender(props).send("mensagem"));
        assertTrue(ex.getMessage().contains("exit code 7"), ex.getMessage());
    }
}
