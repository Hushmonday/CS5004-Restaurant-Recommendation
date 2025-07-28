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
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        sendButton = new JButton("Send");
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

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
        new Thread(() -> {
            try {
                String aiResponse = callAI(userInput);
                chatArea.append("AI: " + aiResponse + "\n");
            } catch (Exception ex) {
                chatArea.append("[Error communicating with AI]\n");
            }
        }).start();
    }

    private String callAI(String userInput) throws Exception {
        // 构造请求体
        String json = "{" +
                "\"userPreference\":\"" + userInput + "\"," +
                "\"location\":\"\"," +
                "\"cuisine\":\"\"," +
                "\"priceRange\":\"\"," +
                "\"numberOfPeople\":1," +
                "\"occasion\":\"\"}";
        URL url = new URL("http://localhost:8080/api/recommendations");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes());
        }
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }
        // 提取aiExplanation字段内容
        String resp = response.toString();
        int idx = resp.indexOf("\"aiExplanation\"");
        if (idx != -1) {
            int start = resp.indexOf(":", idx) + 1;
            int end = resp.indexOf(",\"reasoning\"", start);
            if (end == -1) end = resp.indexOf("}", start);
            if (start > 0 && end > start) {
                String value = resp.substring(start, end).trim();
                // 去除前后的引号
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                value = value.replaceAll("\\\\n", "\n").replaceAll("\\\\\"", "\"");
                return value;
            }
        }
        // 如果无法提取，返回完整响应内容，便于调试
        return resp;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new RestaurantChatUI().setVisible(true);
        });
    }
} 