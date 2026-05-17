package fr.sma.fire.agents;

import fr.sma.fire.config.AGRConfig;
import fr.sma.fire.messages.AlerteIncendie;
import fr.sma.fire.messages.RisquePropagation;
import fr.sma.fire.messages.SMAFireMessage;
import fr.sma.fire.model.ZoneForet;
import madkit.kernel.Agent;
import madkit.kernel.Message;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class AgentInterface extends Agent {

    private volatile boolean running = true;

    private JFrame frame;
    private JLabel statusLabel;
    private JLabel zoneLabel;
    private JLabel riskLabel;
    private JLabel scoreLabel;
    private JPanel riskIndicator;
    private JTextPane logPane;
    private StyledDocument logDoc;
    private DefaultListModel<String> historyModel;

    @Override
    protected void activate() {
        createGroup(AGRConfig.COMMUNITY, AGRConfig.GROUPE_COORDINATION);
        requestRole(AGRConfig.COMMUNITY, AGRConfig.GROUPE_COORDINATION, AGRConfig.ROLE_INTERFACE_OBSERVER);
        SwingUtilities.invokeLater(this::createUI);
        System.out.println("[AgentInterface] Dashboard active.");
    }

    @Override
    protected void live() {
        while (running) {
            Message message = waitNextMessage(500);
            if (!(message instanceof SMAFireMessage fireMessage)) continue;

            Object content = fireMessage.getContent();

            if (content instanceof String log) {
                String lower = log.toLowerCase();
                String style;
                if (lower.contains("fausse"))                                    style = "false_alarm";
                else if (lower.contains("alerte"))                               style = "alert";
                else if (lower.contains("confirme") || lower.contains("confirm")) style = "confirm";
                else if (lower.contains("evacuation") || lower.contains("évacuation")) style = "evacuation";
                else if (lower.contains(">>>"))                                  style = "trigger";
                else if (lower.contains("terminee") || lower.contains("terminée")) style = "done";
                else                                                              style = "normal";

                addLog(log, style);

                if (lower.contains("fausse alerte")) {
                    String zone = extractZone(log);
                    addToHistory(zone + " | Fausse alerte  ✗");
                    // Update status cards so the top panel reflects the false-alarm result
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("False Alarm");
                        zoneLabel.setText(zone);
                        riskLabel.setText("—");
                        scoreLabel.setText("—");
                        riskIndicator.setBackground(new Color(65, 115, 200));
                    });
                }
            }

            if (content instanceof RisquePropagation risque) {
                updateRiskState(risque);
                String evac = risque.getZone().isProcheHabitations() ? " | Evacuée ✓" : " | Sans évac ✓";
                addToHistory("Zone " + risque.getZone().getId()
                        + " | " + risque.getNiveau()
                        + " | Score " + Math.round(risque.getScore() * 100)
                        + evac);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  UI construction
    // ─────────────────────────────────────────────────────────────────────────

    private void createUI() {
        frame = new JFrame("SMA Forest Fire Management — MadKit AGR");
        frame.setSize(1150, 740);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                running = false;
            }
        });

        frame.add(buildHeader(), BorderLayout.NORTH);
        frame.add(buildMain(),   BorderLayout.CENTER);
        frame.setVisible(true);

        addLog("Dashboard initialized. Waiting for agent messages...", "normal");
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(28, 38, 58));
        header.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));

        JLabel title = new JLabel("Forest Fire Multi-Agent Simulation");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JLabel subtitle = new JLabel("MadKit 5 + AALAADIN/AGR — Live Agent Communication Dashboard");
        subtitle.setForeground(new Color(170, 190, 215));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBox.setOpaque(false);
        titleBox.add(title);
        titleBox.add(subtitle);

        JLabel agentLabel = new JLabel("Agents actifs: 8  |  Community: ForetCommunity");
        agentLabel.setForeground(new Color(130, 200, 145));
        agentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        agentLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        header.add(titleBox,    BorderLayout.WEST);
        header.add(agentLabel,  BorderLayout.EAST);
        return header;
    }

    private JPanel buildMain() {
        JPanel main = new JPanel(new BorderLayout(0, 10));
        main.setBackground(new Color(242, 245, 250));
        main.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Top: status cards + attack buttons
        JPanel topSection = new JPanel(new BorderLayout(0, 8));
        topSection.setOpaque(false);
        topSection.add(buildStatusCards(),  BorderLayout.NORTH);
        topSection.add(buildButtonsPanel(), BorderLayout.SOUTH);
        main.add(topSection, BorderLayout.NORTH);

        // Center: log (left) + history (right)
        main.add(buildCenterSplit(), BorderLayout.CENTER);
        return main;
    }

    private JPanel buildStatusCards() {
        JPanel cards = new JPanel(new GridLayout(1, 5, 10, 0));
        cards.setOpaque(false);
        cards.setPreferredSize(new Dimension(1100, 95));

        statusLabel = createCard(cards, "Status",       "Monitoring");
        zoneLabel   = createCard(cards, "Current Zone", "—");
        riskLabel   = createCard(cards, "Risk Level",   "—");
        scoreLabel  = createCard(cards, "Score / 100",  "—");

        JPanel riskCard = new JPanel(new BorderLayout(0, 6));
        riskCard.setBackground(Color.WHITE);
        riskCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(215, 222, 232)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        JLabel riskTitle = new JLabel("Risk Indicator");
        riskTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        riskTitle.setForeground(new Color(90, 100, 115));
        riskIndicator = new JPanel();
        riskIndicator.setBackground(new Color(110, 180, 120));
        riskIndicator.setToolTipText("Color changes with risk level");
        riskCard.add(riskTitle,     BorderLayout.NORTH);
        riskCard.add(riskIndicator, BorderLayout.CENTER);
        cards.add(riskCard);

        return cards;
    }

    private JPanel buildButtonsPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(new Color(232, 236, 244));
        outer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(195, 205, 220)),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));

        JLabel panelTitle = new JLabel("  Manual Attack Simulation:  ");
        panelTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panelTitle.setForeground(new Color(55, 65, 85));
        outer.add(panelTitle, BorderLayout.WEST);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setOpaque(false);

        Object[][] attacks = {
            // label,                          id,  temp,  smoke, near,  color
            {"Zone A — Low Risk (no alert)",   10,  55.0,  false, false, new Color(70, 148, 95)},
            {"Zone B — Medium Fire",           11,  68.0,  true,  false, new Color(195, 130, 35)},
            {"Zone C — Critical + Evacuation", 12,  92.0,  true,  true,  new Color(185, 45,  45)},
            {"Zone D — Force False Alarm",     13,  75.0,  true,  false, new Color(60,  110, 195)},
        };

        for (Object[] a : attacks) {
            String  label  = (String)  a[0];
            int     zoneId = (Integer) a[1];
            double  temp   = (Double)  a[2];
            boolean smoke  = (Boolean) a[3];
            boolean near   = (Boolean) a[4];
            Color   color  = (Color)   a[5];

            JButton btn = makeAttackButton(label, color);
            btn.addActionListener(e -> {
                btn.setEnabled(false);
                btn.setText("Sending...");
                injectAlert(new ZoneForet(zoneId, temp, smoke, near));
                Timer restore = new Timer(5000, ev -> {
                    btn.setEnabled(true);
                    btn.setText(label);
                });
                restore.setRepeats(false);
                restore.start();
            });
            btnRow.add(btn);
        }

        outer.add(btnRow, BorderLayout.CENTER);
        return outer;
    }

    private JButton makeAttackButton(String label, Color base) {
        JButton btn = new JButton(label);
        btn.setBackground(base);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(base.darker(), 1),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(base.brighter());
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(base);
            }
        });
        return btn;
    }

    private JSplitPane buildCenterSplit() {
        // Log pane
        logPane = new JTextPane();
        logPane.setEditable(false);
        logPane.setBackground(new Color(20, 25, 36));
        logPane.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        logDoc = logPane.getStyledDocument();

        JScrollPane logScroll = new JScrollPane(logPane);
        logScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(75, 88, 110)),
                "Agent Timeline",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 12),
                new Color(65, 78, 100)));

        // History list
        historyModel = new DefaultListModel<>();
        JList<String> historyList = new JList<>(historyModel);
        historyList.setBackground(new Color(26, 32, 46));
        historyList.setForeground(new Color(205, 218, 235));
        historyList.setFont(new Font("Consolas", Font.PLAIN, 12));
        historyList.setCellRenderer(new HistoryCellRenderer());
        historyList.setFixedCellHeight(30);

        JScrollPane historyScroll = new JScrollPane(historyList);
        historyScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(75, 88, 110)),
                "Zone History",
                0, 0,
                new Font("Segoe UI", Font.BOLD, 12),
                new Color(65, 78, 100)));

        // Legend panel below the history
        JPanel historyPanel = new JPanel(new BorderLayout(0, 4));
        historyPanel.setOpaque(false);
        historyPanel.add(historyScroll, BorderLayout.CENTER);
        historyPanel.add(buildLegend(), BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, logScroll, historyPanel);
        split.setDividerLocation(730);
        split.setResizeWeight(0.72);
        split.setBorder(null);
        return split;
    }

    private JPanel buildLegend() {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        legend.setBackground(new Color(232, 236, 244));
        legend.setBorder(BorderFactory.createLineBorder(new Color(195, 205, 220)));
        legend.add(legendDot(new Color(255, 100, 100), "Alert"));
        legend.add(legendDot(new Color(100, 160, 255), "False alarm"));
        legend.add(legendDot(new Color(255, 170, 60),  "Confirmed"));
        legend.add(legendDot(new Color(210, 100, 255), "Evacuation"));
        legend.add(legendDot(new Color(100, 230, 150), "Manual"));
        return legend;
    }

    private JLabel legendDot(Color color, String text) {
        JLabel lbl = new JLabel("● " + text);
        lbl.setForeground(color);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        return lbl;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Agent logic
    // ─────────────────────────────────────────────────────────────────────────

    private void injectAlert(ZoneForet zone) {
        if (zone.getTemperature() <= 60 || !zone.isFumee()) {
            addLog(">>> MANUAL TRIGGER: Zone " + zone.getId()
                    + " | temp=" + zone.getTemperature()
                    + "°C | fumee=" + zone.isFumee()
                    + " — Seuil non atteint, aucune alerte envoyee.", "trigger");
            addToHistory("Zone " + zone.getId() + " | Sous seuil — pas d'alerte");
            // Update cards to reflect the below-threshold result
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("Below Threshold");
                zoneLabel.setText("Zone " + zone.getId());
                riskLabel.setText("—");
                scoreLabel.setText("—");
                riskIndicator.setBackground(new Color(100, 115, 145));
            });
            return;
        }
        sendMessage(
                AGRConfig.COMMUNITY,
                AGRConfig.GROUPE_COORDINATION,
                AGRConfig.ROLE_COORDINATEUR,
                new SMAFireMessage(new AlerteIncendie(zone, "ManualTrigger"))
        );
        addLog(">>> MANUAL TRIGGER: Zone " + zone.getId()
                + " | temp=" + zone.getTemperature()
                + "°C | fumee=" + zone.isFumee()
                + " | proches=" + zone.isProcheHabitations(), "trigger");
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  UI update helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void updateRiskState(RisquePropagation risque) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Alert Active");
            zoneLabel.setText("Zone " + risque.getZone().getId());
            riskLabel.setText(risque.getNiveau());
            scoreLabel.setText(String.valueOf(Math.round(risque.getScore() * 100)));

            Color indicatorColor = switch (risque.getNiveau().toUpperCase()) {
                case "CRITIQUE" -> new Color(200, 45,  45);
                case "ELEVE"    -> new Color(230, 140, 40);
                case "MOYEN"    -> new Color(225, 195, 60);
                default         -> new Color(80,  170, 100);
            };
            riskIndicator.setBackground(indicatorColor);

            addLog("[Interface] GLOBAL STATE: Zone " + risque.getZone().getId()
                    + " | Risque=" + risque.getNiveau()
                    + " | Score=" + Math.round(risque.getScore() * 100), "confirm");
        });
    }

    private void addLog(String text, String style) {
        SwingUtilities.invokeLater(() -> {
            if (logDoc == null) return;
            try {
                SimpleAttributeSet attrs = new SimpleAttributeSet();
                StyleConstants.setFontFamily(attrs, "Consolas");
                StyleConstants.setFontSize(attrs, 13);

                switch (style) {
                    case "alert"       -> { StyleConstants.setForeground(attrs, new Color(255, 100, 100)); StyleConstants.setBold(attrs, true); }
                    case "false_alarm" -> { StyleConstants.setForeground(attrs, new Color(100, 160, 255)); StyleConstants.setBold(attrs, true); }
                    case "confirm"     ->   StyleConstants.setForeground(attrs, new Color(255, 175, 65));
                    case "evacuation"  -> { StyleConstants.setForeground(attrs, new Color(210, 100, 255)); StyleConstants.setBold(attrs, true); }
                    case "trigger"     -> { StyleConstants.setForeground(attrs, new Color(100, 230, 150)); StyleConstants.setBold(attrs, true); }
                    case "done"        ->   StyleConstants.setForeground(attrs, new Color(140, 155, 175));
                    default            ->   StyleConstants.setForeground(attrs, new Color(205, 218, 235));
                }

                logDoc.insertString(logDoc.getLength(), "• " + text + "\n", attrs);
                logPane.setCaretPosition(logDoc.getLength());
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        });
    }

    private void addToHistory(String entry) {
        SwingUtilities.invokeLater(() -> {
            if (historyModel != null) historyModel.addElement(entry);
        });
    }

    private String extractZone(String log) {
        int idx = log.indexOf("Zone ");
        if (idx >= 0) {
            StringBuilder sb = new StringBuilder("Zone ");
            int i = idx + 5;
            while (i < log.length() && Character.isDigit(log.charAt(i))) {
                sb.append(log.charAt(i++));
            }
            return sb.toString();
        }
        return "Zone ?";
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Card factory
    // ─────────────────────────────────────────────────────────────────────────

    private JLabel createCard(JPanel parent, String title, String value) {
        JPanel card = new JPanel(new GridLayout(2, 1, 0, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(215, 222, 232)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(new Color(88, 100, 118));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        valueLabel.setForeground(new Color(30, 42, 65));

        card.add(titleLabel);
        card.add(valueLabel);
        parent.add(card);
        return valueLabel;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  History list renderer
    // ─────────────────────────────────────────────────────────────────────────

    private static class HistoryCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            String text = value.toString();
            setFont(new Font("Consolas", Font.PLAIN, 12));
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 8));
            if (!isSelected) {
                setBackground(index % 2 == 0 ? new Color(26, 32, 46) : new Color(32, 39, 55));
                if      (text.contains("✗") || text.contains("Fausse"))  setForeground(new Color(100, 160, 255));
                else if (text.contains("CRITIQUE"))                       setForeground(new Color(255, 100, 100));
                else if (text.contains("ELEVE"))                          setForeground(new Color(255, 170, 60));
                else if (text.contains("MOYEN"))                          setForeground(new Color(225, 195, 60));
                else if (text.contains("Sous seuil"))                     setForeground(new Color(120, 135, 155));
                else                                                      setForeground(new Color(205, 218, 235));
            }
            return this;
        }
    }
}
