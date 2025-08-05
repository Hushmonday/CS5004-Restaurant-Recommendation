package com.restaurant.recommendation.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RestaurantChatUI extends JFrame {
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;

    public RestaurantChatUI() {
        setTitle("AI Restaurant Chat");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Chat area with better formatting
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(chatArea);
        add(scrollPane, BorderLayout.CENTER);

        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        inputField.setFont(new Font("Arial", Font.PLAIN, 14));
        sendButton = new JButton("Send");
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        // Add welcome message
        chatArea.append("Welcome to AI Restaurant Recommendations!\n");
        chatArea.append("Try asking: 'I want spicy food at San Jose' or 'Something special in Boston'\n");
        chatArea.append("----------------------------------------\n\n");

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
    }

    private void sendMessage() {
        String userInput = inputField.getText().trim();
        if (userInput.isEmpty()) return;

        chatArea.append("You: " + userInput + "\n");
        inputField.setText("");

        // Disable input while processing
        inputField.setEnabled(false);
        sendButton.setEnabled(false);

        new Thread(() -> {
            try {
                chatArea.append("AI: Thinking...\n");
                String aiResponse = callAI(userInput);

                // Remove the "Thinking..." message
                String text = chatArea.getText();
                text = text.substring(0, text.lastIndexOf("AI: Thinking..."));
                chatArea.setText(text);

                // Add the actual response
                chatArea.append("AI: " + aiResponse + "\n\n");

                // Auto-scroll to bottom
                chatArea.setCaretPosition(chatArea.getDocument().getLength());
            } catch (Exception ex) {
                chatArea.append("AI: [Error communicating with server: " + ex.getMessage() + "]\n\n");
            } finally {
                // Re-enable input
                SwingUtilities.invokeLater(() -> {
                    inputField.setEnabled(true);
                    sendButton.setEnabled(true);
                    inputField.requestFocus();
                });
            }
        }).start();
    }

    private String callAI(String userInput) throws Exception {
        // Extract location from user input if present
        String location = "";
        String preference = userInput;

        // Check for location indicators
        String lowerInput = userInput.toLowerCase();
        if (lowerInput.contains(" at ") || lowerInput.contains(" in ")) {
            int atIndex = lowerInput.indexOf(" at ");
            int inIndex = lowerInput.indexOf(" in ");
            int index = -1;
            String delimiter = "";

            if (atIndex > -1 && (inIndex == -1 || atIndex < inIndex)) {
                index = atIndex;
                delimiter = " at ";
            } else if (inIndex > -1) {
                index = inIndex;
                delimiter = " in ";
            }

            if (index > -1) {
                preference = userInput.substring(0, index).trim();
                location = userInput.substring(index + delimiter.length()).trim();
                System.out.println("Extracted - Preference: '" + preference + "', Location: '" + location + "'");
            }
        }

        // Construct request body with properly escaped JSON
        String json = "{" +
                "\"userPreference\":\"" + escapeJson(userInput) + "\"," +
                "\"location\":\"" + escapeJson(location) + "\"," +
                "\"cuisine\":\"\"," +
                "\"priceRange\":\"\"," +
                "\"numberOfPeople\":1," +
                "\"occasion\":\"\"" +
                "}";

        System.out.println("Sending JSON: " + json);

        URL url = new URL("http://localhost:8080/api/recommendations");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(30000); // 30 second timeout
        conn.setReadTimeout(30000);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes("UTF-8"));
            os.flush();
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Server returned code: " + responseCode);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }

        // Extract aiExplanation field content
        String resp = response.toString();
        System.out.println("Received response: " + resp);

        int idx = resp.indexOf("\"aiExplanation\"");
        if (idx != -1) {
            int start = resp.indexOf(":", idx) + 1;
            int end = resp.indexOf(",\"reasoning\"", start);
            if (end == -1) end = resp.indexOf("}", start);
            if (start > 0 && end > start) {
                String value = resp.substring(start, end).trim();
                // Remove surrounding quotes
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                // Unescape JSON
                value = value.replace("\\n", "\n")
                        .replace("\\\"", "\"")
                        .replace("\\\\", "\\")
                        .replace("\\/", "/")
                        .replace("\\b", "\b")
                        .replace("\\f", "\f")
                        .replace("\\r", "\r")
                        .replace("\\t", "\t");
                return value;
            }
        }

        // If we can't extract aiExplanation, return the full response for debugging
        return "Could not parse response. Raw data: " + resp;
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RestaurantChatUI ui = new RestaurantChatUI();
            ui.setVisible(true);
        });
    }
}