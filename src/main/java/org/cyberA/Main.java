package org.cyberA;

import io.github.ollama4j.Ollama;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequest;
import io.github.ollama4j.models.chat.OllamaChatResult;
import io.github.ollama4j.tools.annotations.OllamaToolService;
import io.github.ollama4j.tools.annotations.ToolProperty;
import io.github.ollama4j.tools.annotations.ToolSpec;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;


public class Main {
    static class Bridge extends WebSocketClient {

        private final BlockingQueue<String> inbox = new ArrayBlockingQueue<>(16);

        Bridge(URI url){
            super(url);
        }

        @Override
        public void onOpen(ServerHandshake h) {
            System.out.println("[CyberA Panel] Connected to the VM, Status:" + h.getHttpStatus());
        }

        @Override
        public void onMessage(String message) {
            inbox.offer(message);
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            System.out.println("[CyberA Panel] Connection closed: " + reason);
        }

        @Override
        public void onError(Exception ex) {
            System.out.println("[CyberA Panel] An error occurred: " + ex);
        }

        void drain() {
            inbox.clear();
        }

        String request(JSONObject payload) throws InterruptedException {
            drain();
            send(payload.toString());
            String response = inbox.poll(30, TimeUnit.SECONDS);
            return response == null ? "{\"ERR\":\"timeout waiting for VM\"}" : response;
        }
    }

    private static Bridge bridge;

    public static void main(String[] args) throws Exception {

        URI ServerUri = new URI("ws://localhost:8080");
        bridge = new Bridge(ServerUri);
        bridge.connectBlocking();

        Ollama ollama = new Ollama("http://localhost:11434");
        ollama.setRequestTimeoutSeconds(120);

        String model = "gemma4:e2b";

        OllamaChatRequest request =
                OllamaChatRequest.builder()
                        .withModel(model)
                        .withMessage(
                                OllamaChatMessageRole.USER,
                                "What is the capital of France?"
                        )
                        .build();

        OllamaChatResult result = ollama.chat(request, token -> System.out.print(token));
        System.out.println("\n"+result.getResponseModel().getMessage().getResponse());
    }
}
