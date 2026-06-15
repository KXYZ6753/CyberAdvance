package org.cyberA;

import io.github.ollama4j.Ollama;
import io.github.ollama4j.models.chat.*;
import io.github.ollama4j.models.generate.OllamaGenerateTokenHandler;
import io.github.ollama4j.tools.annotations.OllamaToolService;
import io.github.ollama4j.tools.annotations.ToolProperty;
import io.github.ollama4j.tools.annotations.ToolSpec;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@OllamaToolService(providers = { Main.class })
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

    private static JSONObject envelope(JSONObject tool) {
        JSONArray tools = new JSONArray();
        tools.put(tool);
        JSONObject payload = new JSONObject();
        payload.put("tools", tools);
        return payload;
    }

    private static String forward(JSONObject tool) {
        try {
            return bridge.request(envelope(tool));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "{\"ERR\":\"interrupted while waiting for VM\"}";
        }
    }

    @ToolSpec(desc = "Read the full contents of a single file on the VM by its path.")
    public String readFile(
            @ToolProperty(name = "path", desc = "Absolute or relative path of the file to read")
            String path) {
        JSONObject tool = new JSONObject();
        tool.put("tool_name", "read_file");
        tool.put("path", path);
        return forward(tool);
    }

    @ToolSpec(desc = "List the entries (files and sub-folders) inside a directory on the VM.")
    public String listFolder(
            @ToolProperty(name = "path", desc = "Path of the directory to list")
            String path) {
        JSONObject tool = new JSONObject();
        tool.put("tool_name", "list_folder");
        tool.put("path", path);
        return forward(tool);
    }

    @ToolSpec(desc = "Produce a structure report describing the layout of a project or directory on the VM.")
    public String structureReport(
            @ToolProperty(name = "path", desc = "Root path to generate the structure report for")
            String path) {
        JSONObject tool = new JSONObject();
        tool.put("tool_name", "structure_report");
        tool.put("path", path);
        return forward(tool);
    }

    public static void main(String[] args) throws Exception {


        URI ServerUri = new URI("ws://localhost:6767");
        bridge = new Bridge(ServerUri);
        bridge.connectBlocking();

        Ollama ollama = new Ollama("http://localhost:11434");
        ollama.setRequestTimeoutSeconds(120);

        String model = "gemma4:e2b";

        ollama.pullModel(model);

        ollama.registerAnnotatedTools();








        List<OllamaChatMessage> history = new ArrayList<>();
        history.add(new OllamaChatMessage(
                OllamaChatMessageRole.SYSTEM,
                "You are CyberA, an autonomous code-analysis agent operating on a remote VM. " +
                        "You have tools: readFile, listFolder, structureReport. " +
                        "Work in a loop: think about what you still need to know, call ONE tool, " +
                        "examine its result, then decide the next tool call. " +
                        "Keep calling tools until you have enough information to fully answer the user. " +
                        "When you are completely done and need no more tool calls, respond to user"
        ));


        int maxSteps = 15;





        UserInterface ui = new UserInterface();
        ui.onSubmit(new Runnable() {
            @Override
            public void run() {
                System.out.println("user pressed enter");
                String userInput = ui.getUserText();
                ui.setUserText("");

                OllamaGenerateTokenHandler thinkingStreamHandler =
                        (s) -> {
                            ui.concatModelText(s);
                        };
                OllamaGenerateTokenHandler responseStreamHandler = //doesnt work idk why
                        (s) -> {
                            System.out.println("[RESPONSE] " + s);
                            ui.concatModelText(s);
                        };

                new Thread(() -> {
                    try {

                        history.add(new OllamaChatMessage(OllamaChatMessageRole.USER, userInput));
                        for (int step =0; step < maxSteps; step++) {
                            ui.setChatText("\nTHINKING PROCESS (Step "+(step+1)+"):\n->");
                            OllamaChatRequest request = OllamaChatRequest.builder()
                                    .withModel(model)
                                    .withMessages(history)
                                    .build();

                            OllamaChatResult result = ollama.chat(
                                    request,
                                    new OllamaChatStreamObserver(thinkingStreamHandler,responseStreamHandler)
                            );

                            var message = result.getResponseModel().getMessage();
                            List<OllamaChatToolCalls> toolCalls = msg.getToolCalls();

                        }
                        ui.setChatText("\nTHINKING PROCESS:\n->");
                        OllamaChatResult result =
                        ui.setChatText("\nRESPONSE:\n->");
                        ui.concatModelText(result.getResponseModel().getMessage().getResponse());
                    } catch (io.github.ollama4j.exceptions.OllamaException e) {
                        System.err.println("Ollama error: " + e.getMessage());
                    }
                }).start();
            }
        });

        ui.initalize();

    }
}
