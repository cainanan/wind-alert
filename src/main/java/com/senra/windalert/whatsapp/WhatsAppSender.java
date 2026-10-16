package com.senra.windalert.whatsapp;

import com.senra.windalert.config.WindAlertProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class WhatsAppSender {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppSender.class);

    private final WindAlertProperties properties;

    public WhatsAppSender(WindAlertProperties properties) {
        this.properties = properties;
    }

    public void send(String message) {
        if (properties.isDryRun()) {
            log.info("[dry-run] Mensagem NÃO enviada (seria enviada): \"{}\"", message);
            return;
        }

        ProcessBuilder processBuilder = new ProcessBuilder(
                properties.getWhatsapperPhp(),
                properties.getWhatsapperScript(),
                message
        );

        log.info("Enviando WhatsApp: {} {} \"{}\"",
                properties.getWhatsapperPhp(), properties.getWhatsapperScript(), message);

        try {
            Process process = processBuilder.start();
            String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            int exitCode = process.waitFor();

            if (!stdout.isBlank()) {
                log.info("whatsapper stdout: {}", stdout.strip());
            }
            if (exitCode != 0) {
                log.error("whatsapper falhou (exit code {}): {}", exitCode, stderr.strip());
                throw new RuntimeException("Envio de WhatsApp falhou (exit code " + exitCode + ")");
            }
            if (!stderr.isBlank()) {
                log.warn("whatsapper stderr: {}", stderr.strip());
            }
        } catch (IOException e) {
            throw new RuntimeException("Falha ao executar o script de WhatsApp", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Execução do script de WhatsApp interrompida", e);
        }
    }
}
