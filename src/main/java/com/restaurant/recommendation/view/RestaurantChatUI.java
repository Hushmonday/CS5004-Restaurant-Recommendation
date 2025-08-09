package com.restaurant.recommendation.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RestaurantChatUI extends JFrame {

    // Colors - Bright Orange theme with white background
    private static final Color BACKGROUND_COLOR = new Color(255, 255, 255);  // White background
    private static final Color SIDEBAR_COLOR = new Color(250, 250, 250);     // Light gray sidebar
    private static final Color CARD_COLOR = new Color(245, 245, 245);        // Light card background
    private static final Color ACCENT_COLOR = new Color(255, 120, 55);       // Bright orange
    private static final Color ACCENT_HOVER = new Color(255, 140, 75);       // Lighter orange hover
    private static final Color TEXT_PRIMARY = new Color(34, 34, 34);         // Dark gray text
    private static final Color TEXT_SECONDARY = new Color(102, 102, 102);    // Medium gray text
    private static final Color SUCCESS_COLOR = new Color(76, 175, 80);       // Green
    private static final Color USER_MESSAGE_COLOR = new Color(255, 120, 55); // Orange for user messages
    private static final Color AI_MESSAGE_COLOR = new Color(240, 240, 240);  // Light gray for AI messages

    // Components
    private JPanel mainPanel;
    private JPanel sidebarPanel;
    private JPanel contentPanel;
    private JPanel homePanel;
    private JPanel chatPanel;
    private JTextPane chatArea;
    private JTextField messageInput;
    private JButton sendButton;
    private JPanel chatHistoryPanel;

    // Chat management
    private List<ChatSession> chatSessions = new ArrayList<>();
    private ChatSession currentChat;
    private boolean isInChatMode = false;

    // Chat session class
    private static class ChatSession {
        String id;
        String title;
        String timestamp;
        List<Message> messages;

        ChatSession(String title) {
            this.id = "chat_" + System.currentTimeMillis();
            this.title = title;
            this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"));
            this.messages = new ArrayList<>();
        }
    }

    private static class Message {
        String content;
        boolean isUser;
        String timestamp;

        Message(String content, boolean isUser) {
            this.content = content;
            this.isUser = isUser;
            this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        }
    }

    public RestaurantChatUI() {
        initializeUI();
        showHome();
    }

    private void initializeUI() {
        setTitle("AI Restaurant Assistant");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setBackground(BACKGROUND_COLOR);

        // Main panel
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        add(mainPanel);

        createSidebar();
        createContentArea();
    }

    private void createSidebar() {
        sidebarPanel = new JPanel();
        sidebarPanel.setBackground(SIDEBAR_COLOR);
        sidebarPanel.setPreferredSize(new Dimension(280, getHeight()));
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Logo/Title
        JLabel titleLabel = new JLabel("AI Restaurant");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarPanel.add(titleLabel);

        JLabel subtitleLabel = new JLabel("Your culinary companion");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarPanel.add(subtitleLabel);

        sidebarPanel.add(Box.createVerticalStrut(30));

        // New Chat Button
        JButton newChatBtn = createModernButton("+ New Chat", ACCENT_COLOR, ACCENT_HOVER);
        newChatBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        newChatBtn.addActionListener(e -> startNewChat());
        sidebarPanel.add(newChatBtn);

        sidebarPanel.add(Box.createVerticalStrut(20));

        // Home Button
        JButton homeBtn = createSidebarButton("Home");
        homeBtn.addActionListener(e -> showHome());
        sidebarPanel.add(homeBtn);

        sidebarPanel.add(Box.createVerticalStrut(10));

        // Chat History Section
        JLabel historyLabel = new JLabel("Recent Chats");
        historyLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        historyLabel.setForeground(TEXT_SECONDARY);
        historyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarPanel.add(historyLabel);

        sidebarPanel.add(Box.createVerticalStrut(10));

        // Chat history container
        chatHistoryPanel = new JPanel();
        chatHistoryPanel.setLayout(new BoxLayout(chatHistoryPanel, BoxLayout.Y_AXIS));
        chatHistoryPanel.setBackground(SIDEBAR_COLOR);
        chatHistoryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JScrollPane historyScroll = new JScrollPane(chatHistoryPanel);
        historyScroll.setBackground(SIDEBAR_COLOR);
        historyScroll.setBorder(null);
        historyScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        historyScroll.setPreferredSize(new Dimension(240, 300));
        sidebarPanel.add(historyScroll);

        sidebarPanel.add(Box.createVerticalGlue());

        mainPanel.add(sidebarPanel, BorderLayout.WEST);
    }

    private void createContentArea() {
        contentPanel = new JPanel(new CardLayout());
        contentPanel.setBackground(BACKGROUND_COLOR);

        createHomePanel();
        createChatPanel();

        mainPanel.add(contentPanel, BorderLayout.CENTER);
    }

    private void createHomePanel() {
        homePanel = new JPanel();
        homePanel.setBackground(BACKGROUND_COLOR);
        homePanel.setLayout(new BoxLayout(homePanel, BoxLayout.Y_AXIS));
        homePanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        // Welcome section
        JLabel welcomeLabel = new JLabel("Welcome to AI Restaurant Assistant");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeLabel.setForeground(TEXT_PRIMARY);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        homePanel.add(welcomeLabel);

        homePanel.add(Box.createVerticalStrut(12));

        JLabel descLabel = new JLabel("<html><div style='text-align: center; line-height: 1.4;'>" +
            "Your intelligent companion for discovering amazing restaurants.<br>" +
            "Get personalized recommendations based on your preferences, location, and mood." +
            "</div></html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descLabel.setForeground(TEXT_SECONDARY);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        homePanel.add(descLabel);

        homePanel.add(Box.createVerticalStrut(35));

        // Features section - larger cards
        JPanel featuresPanel = new JPanel(new GridLayout(1, 2, 25, 20));
        featuresPanel.setBackground(BACKGROUND_COLOR);
        featuresPanel.setMaximumSize(new Dimension(800, 180));

        featuresPanel.add(createFeatureCard("Target", "Smart Recommendations",
            "Get personalized restaurant suggestions based on your taste, location, and preferences"));
        featuresPanel.add(createFeatureCard("Chat", "Natural Conversation",
            "Chat naturally about your cravings, dietary needs, or special occasions"));

        homePanel.add(featuresPanel);

        homePanel.add(Box.createVerticalStrut(40));

        // Get started button - moved to center-bottom area
        JButton getStartedBtn = createModernButton("Get Started - New Chat", ACCENT_COLOR, ACCENT_HOVER);
        getStartedBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        getStartedBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        getStartedBtn.setPreferredSize(new Dimension(220, 45));
        getStartedBtn.addActionListener(e -> startNewChat());
        homePanel.add(getStartedBtn);

        // Add flexible space at the bottom
        homePanel.add(Box.createVerticalGlue());

        contentPanel.add(homePanel, "home");
    }

    private void createChatPanel() {
        chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBackground(BACKGROUND_COLOR);

        // Chat header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(new EmptyBorder(20, 30, 10, 30));

        JLabel chatTitle = new JLabel("Restaurant Chat");
        chatTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        chatTitle.setForeground(TEXT_PRIMARY);
        headerPanel.add(chatTitle, BorderLayout.WEST);

        JButton clearChatBtn = createSmallButton("Clear Chat");
        clearChatBtn.addActionListener(e -> clearCurrentChat());
        headerPanel.add(clearChatBtn, BorderLayout.EAST);

        chatPanel.add(headerPanel, BorderLayout.NORTH);

        // Chat area
        chatArea = new JTextPane();
        chatArea.setBackground(BACKGROUND_COLOR);
        chatArea.setForeground(TEXT_PRIMARY);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chatArea.setEditable(false);
        chatArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setBorder(null);
        chatScroll.setBackground(BACKGROUND_COLOR);
        chatScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chatPanel.add(chatScroll, BorderLayout.CENTER);

        // Input area
        createInputArea();

        contentPanel.add(chatPanel, "chat");
    }

    private void createInputArea() {
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(BACKGROUND_COLOR);
        inputPanel.setBorder(new EmptyBorder(10, 30, 30, 30));

        // Input field
        messageInput = new JTextField();
        messageInput.setBackground(Color.WHITE);
        messageInput.setForeground(TEXT_PRIMARY);
        messageInput.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageInput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            new EmptyBorder(12, 15, 12, 15)
        ));
        messageInput.addActionListener(e -> sendMessage());

        // Send button
        sendButton = createModernButton("Send", ACCENT_COLOR, ACCENT_HOVER);
        sendButton.setPreferredSize(new Dimension(80, 40));
        sendButton.addActionListener(e -> sendMessage());

        inputPanel.add(messageInput, BorderLayout.CENTER);
        inputPanel.add(Box.createHorizontalStrut(10), BorderLayout.EAST);
        inputPanel.add(sendButton, BorderLayout.EAST);

        chatPanel.add(inputPanel, BorderLayout.SOUTH);
    }

    private JPanel createFeatureCard(String icon, String title, String description) {
        JPanel card = new JPanel();
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(25, 25, 25, 25)
        ));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel iconLabel = new JLabel("[" + icon + "]");
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        iconLabel.setForeground(ACCENT_COLOR);
        iconLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(iconLabel);

        card.add(Box.createVerticalStrut(12));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(titleLabel);

        card.add(Box.createVerticalStrut(10));

        JLabel descLabel = new JLabel("<html>" + description + "</html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descLabel.setForeground(TEXT_SECONDARY);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(descLabel);

        return card;
    }

    private JButton createModernButton(String text, Color bgColor, Color hoverColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBorder(new EmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    private JButton createSidebarButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(SIDEBAR_COLOR);
        button.setForeground(TEXT_SECONDARY);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setBorder(new EmptyBorder(8, 12, 8, 12));
        button.setFocusPainted(false);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(240, 32));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(CARD_COLOR);
                button.setForeground(TEXT_PRIMARY);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(SIDEBAR_COLOR);
                button.setForeground(TEXT_SECONDARY);
            }
        });

        return button;
    }

    private JButton createSmallButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(CARD_COLOR);
        button.setForeground(TEXT_SECONDARY);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        button.setBorder(new EmptyBorder(6, 12, 6, 12));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(TEXT_PRIMARY);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(TEXT_SECONDARY);
            }
        });

        return button;
    }

    private void showHome() {
        isInChatMode = false;
        CardLayout cl = (CardLayout) contentPanel.getLayout();
        cl.show(contentPanel, "home");
    }

    private void startNewChat() {
        String title = "New Restaurant Chat";
        currentChat = new ChatSession(title);
        chatSessions.add(currentChat);

        isInChatMode = true;
        clearChatDisplay();
        addWelcomeMessage();
        updateChatHistory();
        showChatPanel();
        messageInput.requestFocus();
    }

    private void showChatPanel() {
        CardLayout cl = (CardLayout) contentPanel.getLayout();
        cl.show(contentPanel, "chat");
    }

    private void addWelcomeMessage() {
        appendToChatArea("Hello! I'm your AI restaurant assistant. I can help you discover amazing restaurants based on your preferences, location, and mood. What are you craving today?", false);
    }

    private void clearCurrentChat() {
        if (currentChat != null) {
            currentChat.messages.clear();
            clearChatDisplay();
            addWelcomeMessage();
            resetConversationOnServer();
        }
    }

    private void clearChatDisplay() {
        chatArea.setText("");
    }

    private void updateChatHistory() {
        chatHistoryPanel.removeAll();

        for (ChatSession session : chatSessions) {
            JButton chatBtn = createChatHistoryButton(session);
            chatHistoryPanel.add(chatBtn);
            chatHistoryPanel.add(Box.createVerticalStrut(5));
        }

        chatHistoryPanel.revalidate();
        chatHistoryPanel.repaint();
    }

    private JButton createChatHistoryButton(ChatSession session) {
        JButton button = new JButton();
        button.setLayout(new BoxLayout(button, BoxLayout.Y_AXIS));
        button.setBackground(SIDEBAR_COLOR);
        button.setBorder(new EmptyBorder(8, 12, 8, 12));
        button.setFocusPainted(false);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(240, 50));

        JLabel titleLabel = new JLabel(session.title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel timeLabel = new JLabel(session.timestamp);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timeLabel.setForeground(TEXT_SECONDARY);
        timeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        button.add(titleLabel);
        button.add(timeLabel);

        button.addActionListener(e -> loadChatSession(session));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(CARD_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(SIDEBAR_COLOR);
            }
        });

        return button;
    }

    private void loadChatSession(ChatSession session) {
        currentChat = session;
        isInChatMode = true;
        clearChatDisplay();

        for (Message msg : session.messages) {
            appendToChatArea(msg.content, msg.isUser);
        }

        showChatPanel();
        messageInput.requestFocus();
    }

    private void sendMessage() {
        String message = messageInput.getText().trim();
        if (message.isEmpty() || !isInChatMode) return;

        messageInput.setText("");
        messageInput.setEnabled(false);
        sendButton.setEnabled(false);

        // Add user message
        Message userMsg = new Message(message, true);
        currentChat.messages.add(userMsg);
        appendToChatArea(message, true);

        // Show thinking indicator
        appendToChatArea("Thinking...", false);

        new Thread(() -> {
            try {
                String response = callAI(message);

                SwingUtilities.invokeLater(() -> {
                    // Remove thinking indicator
                    removeLastMessage();

                    // Add AI response
                    Message aiMsg = new Message(response, false);
                    currentChat.messages.add(aiMsg);
                    appendToChatArea(response, false);

                    // Update chat title if this is the first user message
                    if (currentChat.messages.size() == 2) { // Welcome + User + AI
                        String shortTitle = message.length() > 30 ? message.substring(0, 30) + "..." : message;
                        currentChat.title = shortTitle;
                        updateChatHistory();
                    }
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    removeLastMessage();
                    appendToChatArea("Sorry, I encountered an error: " + e.getMessage(), false);
                });
            } finally {
                SwingUtilities.invokeLater(() -> {
                    messageInput.setEnabled(true);
                    sendButton.setEnabled(true);
                    messageInput.requestFocus();
                });
            }
        }).start();
    }

    private void appendToChatArea(String message, boolean isUser) {
        try {
            String color = isUser ? "#ffb380" : "#f0f0f0";  // Light orange for user, light gray for AI
            String textColor = isUser ? "#333333" : "#222222";  // Dark text for both for better readability
            String alignment = isUser ? "right" : "left";
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

            String html = String.format(
                "<div style='margin: 10px 0; padding: 12px; background-color: %s; color: %s; " +
                    "border-radius: 8px; max-width: 70%%; margin-%s: auto; text-align: %s; " +
                    "border: 1px solid #e0e0e0;'>" +
                    "<div style='font-size: 14px; line-height: 1.4;'>%s</div>" +
                    "<div style='font-size: 11px; color: %s; margin-top: 5px;'>%s</div>" +
                    "</div>",
                color, textColor, isUser ? "left" : "right", alignment,
                message.replace("\n", "<br>"),
                isUser ? "#666666" : "#666666",  // Same gray for time stamps
                time
            );

            chatArea.setContentType("text/html");
            String currentContent = chatArea.getText();
            if (currentContent.contains("<body>")) {
                currentContent = currentContent.replace("</body>", html + "</body>");
            } else {
                currentContent = "<html><body style='background-color: #ffffff; margin: 0; padding: 10px;'>" + html + "</body></html>";
            }
            chatArea.setText(currentContent);

            // Auto scroll to bottom
            chatArea.setCaretPosition(chatArea.getDocument().getLength());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeLastMessage() {
        try {
            String content = chatArea.getText();
            int lastDivStart = content.lastIndexOf("<div style='margin: 10px 0;");
            if (lastDivStart > 0) {
                int divEnd = content.indexOf("</div>", lastDivStart);
                if (divEnd > 0) {
                    divEnd = content.indexOf("</div>", divEnd + 1) + 6; // Include the closing tag
                    content = content.substring(0, lastDivStart) + content.substring(divEnd);
                    chatArea.setText(content);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String callAI(String userInput) throws Exception {
        String json = "{" +
            "\"userPreference\":\"" + escapeJson(userInput) + "\"," +
            "\"location\":\"\"," +
            "\"cuisine\":\"\"," +
            "\"priceRange\":\"\"," +
            "\"numberOfPeople\":1," +
            "\"occasion\":\"\"" +
            "}";

        URL url = new URL("http://localhost:8080/api/recommendations");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(30000);
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

        String resp = response.toString();
        int idx = resp.indexOf("\"aiExplanation\"");
        if (idx != -1) {
            int start = resp.indexOf(":", idx) + 1;
            int end = resp.indexOf(",\"reasoning\"", start);
            if (end == -1) end = resp.indexOf("}", start);
            if (start > 0 && end > start) {
                String value = resp.substring(start, end).trim();
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                return value.replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
            }
        }

        return "Unable to parse response.";
    }

    private void resetConversationOnServer() {
        try {
            URL url = new URL("http://localhost:8080/api/recommendations/reset");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(5000);
            conn.getResponseCode(); // Just trigger the reset
        } catch (Exception e) {
            System.err.println("Failed to reset server conversation: " + e.getMessage());
        }
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
            .replace("\"", "\\\"")
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