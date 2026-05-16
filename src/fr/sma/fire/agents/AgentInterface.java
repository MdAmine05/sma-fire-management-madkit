package fr.sma.fire.agents;

import fr.sma.fire.config.AGRConfig;
import fr.sma.fire.messages.RisquePropagation;
import fr.sma.fire.messages.SMAFireMessage;
import madkit.kernel.Agent;
import madkit.kernel.Message;

import javax.swing.*;
import java.awt.*;

public class AgentInterface extends Agent {

    private JFrame frame;
    private JLabel statusLabel;
    private JLabel zoneLabel;
    private JLabel riskLabel;
    private JTextArea logArea;
    private JPanel riskIndicator;

    @Override
    protected void activate() {
        createGroup(AGRConfig.COMMUNITY, AGRConfig.GROUPE_COORDINATION);
        requestRole(AGRConfig.COMMUNITY, AGRConfig.GROUPE_COORDINATION, "InterfaceObserver");

        SwingUtilities.invokeLater(this::createUI);

        System.out.println("[AgentInterface] Dashboard active.");
    }

    @Override
    protected void live() {
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < 30000) {
            Message message = waitNextMessage(1000);

            if (!(message instanceof SMAFireMessage fireMessage)) {
                continue;
            }

            Object content = fireMessage.getContent();

            if (content instanceof String log) {
                addLog(log);
            }

            if (content instanceof RisquePropagation risque) {
                updateFinalState(risque);
            }
        }
    }

    private void createUI() {
        frame = new JFrame("SMA Forest Fire Management — MadKit AGR");
        frame.setSize(780, 520);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(35, 45, 65));
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("Forest Fire Multi-Agent Simulation");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JLabel subtitle = new JLabel("MadKit + AALAADIN/AGR — Agent Communication Dashboard");
        subtitle.setForeground(new Color(210, 220, 235));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JPanel titleBox = new JPanel(new GridLayout(2, 1));
        titleBox.setOpaque(false);
        titleBox.add(title);
        titleBox.add(subtitle);

        header.add(titleBox, BorderLayout.WEST);
        frame.add(header, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 12));
        cards.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
        cards.setBackground(new Color(245, 247, 250));

        statusLabel = createCard(cards, "Status", "Waiting");
        zoneLabel = createCard(cards, "Zone", "-");
        riskLabel = createCard(cards, "Risk Level", "-");

        JPanel riskCard = new JPanel(new BorderLayout());
        riskCard.setBackground(Color.WHITE);
        riskCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 230)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JLabel riskTitle = new JLabel("Indicator");
        riskTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));

        riskIndicator = new JPanel();
        riskIndicator.setBackground(Color.LIGHT_GRAY);

        riskCard.add(riskTitle, BorderLayout.NORTH);
        riskCard.add(riskIndicator, BorderLayout.CENTER);
        cards.add(riskCard);

        frame.add(cards, BorderLayout.CENTER);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        logArea.setBackground(new Color(25, 30, 40));
        logArea.setForeground(new Color(230, 235, 240));
        logArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Agent Timeline"));

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createEmptyBorder(5, 15, 15, 15));
        bottom.setBackground(new Color(245, 247, 250));
        bottom.add(scrollPane, BorderLayout.CENTER);

        frame.add(bottom, BorderLayout.SOUTH);

        frame.setVisible(true);

        addLog("Dashboard initialized. Waiting for agent messages...");
    }

    private JLabel createCard(JPanel parent, String title, String value) {
        JPanel card = new JPanel(new GridLayout(2, 1));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 230)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(new Color(90, 100, 115));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(new Color(35, 45, 65));

        card.add(titleLabel);
        card.add(valueLabel);
        parent.add(card);

        return valueLabel;
    }

    private void addLog(String text) {
        SwingUtilities.invokeLater(() -> {
            if (logArea != null) {
                logArea.append("• " + text + "\n");
                logArea.setCaretPosition(logArea.getDocument().getLength());
            }
        });
    }

    private void updateFinalState(RisquePropagation risque) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Intervention");
            zoneLabel.setText("Zone " + risque.getZone().getId());
            riskLabel.setText(risque.getNiveau());

            if ("CRITIQUE".equalsIgnoreCase(risque.getNiveau())) {
                riskIndicator.setBackground(new Color(200, 45, 45));
            } else if ("ELEVE".equalsIgnoreCase(risque.getNiveau())) {
                riskIndicator.setBackground(new Color(230, 140, 40));
            } else if ("MOYEN".equalsIgnoreCase(risque.getNiveau())) {
                riskIndicator.setBackground(new Color(230, 200, 70));
            } else {
                riskIndicator.setBackground(new Color(80, 170, 100));
            }

            addLog("GLOBAL STATE: INTERVENTION EN COURS — Zone "
                    + risque.getZone().getId()
                    + " | Risque=" + risque.getNiveau()
                    + " | Score=" + Math.round(risque.getScore() * 100));
        });
    }
}