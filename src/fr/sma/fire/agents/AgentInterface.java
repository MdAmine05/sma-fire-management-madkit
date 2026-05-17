package fr.sma.fire.agents;

import fr.sma.fire.config.AGRConfig;
import fr.sma.fire.logic.RiskCalculator;
import fr.sma.fire.messages.AlerteIncendie;
import fr.sma.fire.messages.DonneesMeteo;
import fr.sma.fire.messages.RisquePropagation;
import fr.sma.fire.messages.SMAFireMessage;
import fr.sma.fire.model.ZoneForet;
import madkit.kernel.Agent;
import madkit.kernel.Message;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class AgentInterface extends Agent {

    private volatile boolean running = true;

    // ── UI Components ──
    private JFrame frame;
    private JLabel statusLabel, statusDot;
    private JLabel zoneLabel, riskLabel, scoreLabel;
    private GaugePanel gaugePanel;
    private JTextPane logPane;
    private StyledDocument logDoc;
    private DefaultListModel<String> historyModel;
    private JLabel footerLabel;

    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");

    // ── Colors ──
    private static final Color BG_DARK       = new Color(13, 17, 23);
    private static final Color BG_CARD       = new Color(22, 27, 38);
    private static final Color BORDER_SUBTLE = new Color(48, 56, 70);
    private static final Color ACCENT_ORANGE = new Color(255, 107, 53);
    private static final Color ACCENT_BLUE   = new Color(77, 166, 255);
    private static final Color ACCENT_GREEN  = new Color(63, 185, 120);
    private static final Color ACCENT_RED    = new Color(235, 68, 68);
    private static final Color ACCENT_PURPLE = new Color(168, 85, 247);
    private static final Color ACCENT_YELLOW = new Color(230, 195, 50);
    private static final Color TEXT_PRIMARY   = new Color(230, 237, 245);
    private static final Color TEXT_SECONDARY = new Color(125, 140, 165);
    private static final Color TEXT_MUTED     = new Color(75, 88, 108);

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
                else if (lower.contains(">>>") || lower.contains("manuel"))      style = "trigger";
                else if (lower.contains("terminee") || lower.contains("terminée")) style = "done";
                else                                                              style = "normal";

                addLog(log, style);

                if (lower.contains("fausse alerte")) {
                    String zone = extractZone(log);
                    addToHistory(zone + " | Fausse alerte | Score 0 ✗");
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("FAUSSE ALERTE");
                        statusLabel.setForeground(ACCENT_BLUE);
                        statusDot.setForeground(ACCENT_BLUE);
                        zoneLabel.setText(zone);
                        zoneLabel.setForeground(TEXT_PRIMARY);
                        riskLabel.setText("Aucun risque");
                        riskLabel.setForeground(ACCENT_BLUE);
                        scoreLabel.setText("0 / 100");
                        scoreLabel.setForeground(TEXT_PRIMARY);
                        gaugePanel.setValue(-1, "ANNULE");
                        footerLabel.setText(zone + " : fausse alerte confirmee par drone — retour en surveillance");
                    });
                }
            }

            if (content instanceof RisquePropagation risque) {
                updateRiskState(risque);
                String evac = risque.getZone().isProcheHabitations() ? " | Evac ✓" : " | Sans evac ✓";
                addToHistory("Zone " + risque.getZone().getId()
                        + " | " + risque.getNiveau()
                        + " | " + Math.round(risque.getScore() * 100)
                        + evac);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  UI CONSTRUCTION
    // ═══════════════════════════════════════════════════════════════════

    private void createUI() {
        frame = new JFrame();
        frame.setTitle("SMA Gestion d'Incendie — MadKit AGR · Moniteur en direct");
        frame.setSize(1280, 780);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setBackground(BG_DARK);
        frame.getContentPane().setBackground(BG_DARK);
        frame.setLayout(new BorderLayout(0, 0));
        frame.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { running = false; }
        });

        frame.add(buildHeader(), BorderLayout.NORTH);
        frame.add(buildBody(),   BorderLayout.CENTER);
        frame.add(buildFooter(), BorderLayout.SOUTH);

        frame.setVisible(true);
        addLog("Systeme initialise. 8 agents actifs. Scan des zones en cours...", "normal");
    }

    // ── HEADER ──
    private JPanel buildHeader() {
        JPanel header = new GradientPanel(new Color(16, 22, 34), new Color(24, 32, 48));
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(16, 28, 16, 28));
        header.setPreferredSize(new Dimension(0, 72));

        // Left: title
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        JLabel icon = new JLabel("🔥");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));

        JLabel title = new JLabel("GESTION D'INCENDIE");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_PRIMARY);

        JLabel tag = new JLabel("  MadKit 5 · AGR · AALAADIN");
        tag.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tag.setForeground(TEXT_MUTED);

        left.add(icon);
        left.add(title);
        left.add(tag);

        // Right: live indicator
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        statusDot = new JLabel("●");
        statusDot.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        statusDot.setForeground(ACCENT_GREEN);

        statusLabel = new JLabel("SURVEILLANCE");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        statusLabel.setForeground(ACCENT_GREEN);

        JLabel agents = new JLabel("8 agents · 4 groupes · ForetCommunity   ");
        agents.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        agents.setForeground(TEXT_MUTED);

        right.add(agents);
        right.add(statusDot);
        right.add(statusLabel);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    // ── BODY ──
    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setBackground(BG_DARK);
        body.setBorder(BorderFactory.createEmptyBorder(14, 20, 8, 20));

        // Top row: metrics + gauge + buttons
        JPanel top = new JPanel(new BorderLayout(14, 0));
        top.setOpaque(false);
        top.setPreferredSize(new Dimension(0, 155));
        top.add(buildMetricsPanel(), BorderLayout.CENTER);
        top.add(buildGaugePanel(),   BorderLayout.EAST);

        // Middle: attack buttons
        JPanel mid = new JPanel(new BorderLayout(0, 12));
        mid.setOpaque(false);
        mid.add(top, BorderLayout.NORTH);
        mid.add(buildAttackBar(), BorderLayout.SOUTH);

        body.add(mid, BorderLayout.NORTH);

        // Bottom: log + history
        body.add(buildLogAndHistory(), BorderLayout.CENTER);

        return body;
    }

    // ── METRICS CARDS ──
    private JPanel buildMetricsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 12, 0));
        panel.setOpaque(false);

        zoneLabel  = createMetricCard(panel, "ZONE ACTIVE", "—", ACCENT_ORANGE);
        riskLabel  = createMetricCard(panel, "NIVEAU DE RISQUE", "—", ACCENT_RED);
        scoreLabel = createMetricCard(panel, "SCORE DE RISQUE", "— / 100", ACCENT_BLUE);

        return panel;
    }

    private JLabel createMetricCard(JPanel parent, String title, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 8)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(accent);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), 3, 3, 3));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 16, 20));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        titleLbl.setForeground(TEXT_MUTED);

        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLbl.setForeground(TEXT_PRIMARY);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLbl, BorderLayout.CENTER);
        parent.add(card);
        return valueLbl;
    }

    // ── GAUGE PANEL ──
    private JPanel buildGaugePanel() {
        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
            }
        };
        wrapper.setOpaque(false);
        wrapper.setPreferredSize(new Dimension(170, 0));
        wrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        gaugePanel = new GaugePanel();
        wrapper.add(gaugePanel, BorderLayout.CENTER);
        return wrapper;
    }

    // ── ATTACK BUTTONS BAR ──
    private JPanel buildAttackBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(18, 24, 34));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(BORDER_SUBTLE);
                g2.setStroke(new BasicStroke(1));
                g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JLabel label = new JLabel("⚡  SIMULATION D'ATTAQUE");
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(TEXT_MUTED);
        bar.add(label, BorderLayout.WEST);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btns.setOpaque(false);

        Object[][] attacks = {
            {"Zone A — Risque faible",     10, 55.0, false, false, new Color(55, 140, 90)},
            {"Zone B — Feu moyen",         11, 68.0, true,  false, ACCENT_ORANGE},
            {"Zone C — Critique + Evac",   12, 92.0, true,  true,  ACCENT_RED},
            {"Zone D — Fausse alerte",     13, 75.0, true,  false, ACCENT_BLUE},
        };

        for (Object[] a : attacks) {
            String  lbl    = (String)  a[0];
            int     zoneId = (Integer) a[1];
            double  temp   = (Double)  a[2];
            boolean smoke  = (Boolean) a[3];
            boolean near   = (Boolean) a[4];
            Color   color  = (Color)   a[5];

            JButton btn = createStyledButton(lbl, color);
            btn.addActionListener(e -> {
                btn.setEnabled(false);
                btn.setText("⏳ Envoi...");
                // Immediate feedback in status
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("TRAITEMENT...");
                    statusLabel.setForeground(ACCENT_ORANGE);
                    statusDot.setForeground(ACCENT_ORANGE);
                });
                injectAlert(new ZoneForet(zoneId, temp, smoke, near));
                Timer t = new Timer(4000, ev -> { btn.setEnabled(true); btn.setText(lbl); });
                t.setRepeats(false);
                t.start();
            });
            btns.add(btn);
        }

        bar.add(btns, BorderLayout.CENTER);
        return bar;
    }

    private JButton createStyledButton(String text, Color accent) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = isEnabled() ? (getModel().isRollover() ? accent : darker(accent, 0.7f)) : new Color(40, 48, 60);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(isEnabled() ? Color.WHITE : TEXT_MUTED);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setPreferredSize(new Dimension(195, 32));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── LOG + HISTORY ──
    private JPanel buildLogAndHistory() {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setOpaque(false);

        // Log
        logPane = new JTextPane();
        logPane.setEditable(false);
        logPane.setBackground(new Color(10, 13, 20));
        logPane.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        logDoc = logPane.getStyledDocument();

        JPanel logWrapper = wrapInCard("CHRONOLOGIE DES AGENTS", new JScrollPane(logPane));

        // History
        historyModel = new DefaultListModel<>();
        JList<String> historyList = new JList<>(historyModel);
        historyList.setBackground(new Color(10, 13, 20));
        historyList.setForeground(TEXT_SECONDARY);
        historyList.setFont(new Font("Consolas", Font.PLAIN, 11));
        historyList.setFixedCellHeight(30);
        historyList.setCellRenderer(new HistoryCellRenderer());

        JPanel histWrapper = wrapInCard("HISTORIQUE DES ZONES", new JScrollPane(historyList));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, logWrapper, histWrapper);
        split.setDividerLocation(820);
        split.setResizeWeight(0.72);
        split.setBorder(null);
        split.setDividerSize(6);
        split.setBackground(BG_DARK);

        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private JPanel wrapInCard(String title, JScrollPane scroll) {
        JPanel card = new JPanel(new BorderLayout(0, 6)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 14, 14));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        titleLbl.setForeground(TEXT_MUTED);

        scroll.setBorder(null);
        scroll.getViewport().setBackground(new Color(10, 13, 20));

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(scroll,   BorderLayout.CENTER);
        return card;
    }

    // ── FOOTER ──
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(11, 15, 21));
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_SUBTLE),
                BorderFactory.createEmptyBorder(6, 20, 6, 20)));
        footer.setPreferredSize(new Dimension(0, 30));

        footerLabel = new JLabel("Pret · Scan des zones en cours...");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footerLabel.setForeground(TEXT_MUTED);

        JLabel version = new JLabel("MadKit 5 · Java " + System.getProperty("java.version") + "  ");
        version.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        version.setForeground(TEXT_MUTED);

        footer.add(footerLabel, BorderLayout.WEST);
        footer.add(version, BorderLayout.EAST);
        return footer;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  AGENT LOGIC
    // ═══════════════════════════════════════════════════════════════════

    private void injectAlert(ZoneForet zone) {
        if (zone.getTemperature() <= 60 || !zone.isFumee()) {
            // Calculate theoretical score even though detection threshold not met
            RiskCalculator calc = new RiskCalculator();
            DonneesMeteo defaultMeteo = new DonneesMeteo("FAIBLE", 50.0);
            double score = calc.calculateScore(zone, defaultMeteo, 0, 10);
            int scoreInt = Math.max(0, (int) Math.round(score * 100));
            String level = calc.getRiskLevel(score);

            addLog("MANUEL → Zone " + zone.getId()
                    + " | " + zone.getTemperature()
                    + "°C | fumee=" + zone.isFumee()
                    + " — Sous le seuil (score theorique: " + scoreInt + "/" + level + ")", "trigger");
            addToHistory("Zone " + zone.getId() + " | " + level + " | Score " + scoreInt + " | Sous seuil ○");
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("SOUS SEUIL");
                statusLabel.setForeground(ACCENT_GREEN);
                statusDot.setForeground(ACCENT_GREEN);
                zoneLabel.setText("Zone " + zone.getId());
                zoneLabel.setForeground(TEXT_PRIMARY);
                riskLabel.setText(level);
                riskLabel.setForeground(ACCENT_GREEN);
                scoreLabel.setText(scoreInt + " / 100");
                scoreLabel.setForeground(TEXT_PRIMARY);
                gaugePanel.setValue(scoreInt, level);
                footerLabel.setText("Zone " + zone.getId() + " : " + level
                        + " (score " + scoreInt + ") — sous le seuil, aucune alerte");
            });
            return;
        }
        sendMessage(
                AGRConfig.COMMUNITY,
                AGRConfig.GROUPE_COORDINATION,
                AGRConfig.ROLE_COORDINATEUR,
                new SMAFireMessage(new AlerteIncendie(zone, "ManualTrigger"))
        );
        addLog("MANUEL → Zone " + zone.getId()
                + " | " + zone.getTemperature()
                + "°C | fumee=true | habitations="
                + zone.isProcheHabitations(), "trigger");
        SwingUtilities.invokeLater(() -> {
            footerLabel.setText("Alerte envoyee pour Zone " + zone.getId() + " — en attente du drone...");
        });
    }

    // ═══════════════════════════════════════════════════════════════════
    //  UI UPDATES
    // ═══════════════════════════════════════════════════════════════════

    private void updateRiskState(RisquePropagation risque) {
        SwingUtilities.invokeLater(() -> {
            String level = risque.getNiveau();
            int score = (int) Math.round(risque.getScore() * 100);

            statusLabel.setText("ALERTE ACTIVE");
            statusLabel.setForeground(ACCENT_RED);
            statusDot.setForeground(ACCENT_RED);
            zoneLabel.setText("Zone " + risque.getZone().getId());
            zoneLabel.setForeground(TEXT_PRIMARY);
            riskLabel.setText(level);
            scoreLabel.setText(score + " / 100");
            scoreLabel.setForeground(TEXT_PRIMARY);
            gaugePanel.setValue(score, level);

            Color riskColor = switch (level.toUpperCase()) {
                case "CRITIQUE" -> ACCENT_RED;
                case "ELEVE"    -> ACCENT_ORANGE;
                case "MOYEN"    -> ACCENT_YELLOW;
                default         -> ACCENT_GREEN;
            };
            riskLabel.setForeground(riskColor);

            String evacText = risque.getZone().isProcheHabitations()
                    ? " | Evacuation declenchee"
                    : " | Pas d'evacuation necessaire";
            footerLabel.setText("Zone " + risque.getZone().getId()
                    + " : " + level + " (score " + score + ")" + evacText);

            addLog("ETAT → Zone " + risque.getZone().getId()
                    + " | Risque=" + level
                    + " | Score=" + score, "confirm");
        });
    }

    private void addLog(String text, String style) {
        SwingUtilities.invokeLater(() -> {
            if (logDoc == null) return;
            try {
                SimpleAttributeSet attrs = new SimpleAttributeSet();
                StyleConstants.setFontFamily(attrs, "Consolas");
                StyleConstants.setFontSize(attrs, 12);

                Color color = switch (style) {
                    case "alert"       -> ACCENT_RED;
                    case "false_alarm" -> ACCENT_BLUE;
                    case "confirm"     -> ACCENT_ORANGE;
                    case "evacuation"  -> ACCENT_PURPLE;
                    case "trigger"     -> ACCENT_GREEN;
                    case "done"        -> TEXT_MUTED;
                    default            -> TEXT_SECONDARY;
                };

                boolean bold = style.equals("alert") || style.equals("false_alarm")
                        || style.equals("evacuation") || style.equals("trigger");

                StyleConstants.setForeground(attrs, color);
                StyleConstants.setBold(attrs, bold);

                // Timestamp prefix
                SimpleAttributeSet timeAttr = new SimpleAttributeSet();
                StyleConstants.setFontFamily(timeAttr, "Consolas");
                StyleConstants.setFontSize(timeAttr, 11);
                StyleConstants.setForeground(timeAttr, TEXT_MUTED);

                String timestamp = LocalTime.now().format(timeFormat);
                logDoc.insertString(logDoc.getLength(), timestamp + "  ", timeAttr);
                logDoc.insertString(logDoc.getLength(), text + "\n", attrs);
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
            while (i < log.length() && Character.isDigit(log.charAt(i))) sb.append(log.charAt(i++));
            return sb.toString();
        }
        return "Zone ?";
    }

    // ═══════════════════════════════════════════════════════════════════
    //  CUSTOM COMPONENTS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Circular gauge that shows the risk score visually.
     * value = 0 → empty (grey), value = -1 → false alarm (blue cross),
     * value 1-100 → filled arc with color.
     */
    private static class GaugePanel extends JPanel {
        private int value = 0;
        private String level = "AUCUN";

        GaugePanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(140, 140));
        }

        void setValue(int v, String l) {
            this.value = v;
            this.level = l;
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int size = Math.min(getWidth(), getHeight()) - 16;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;

            // Background arc
            g2.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(35, 42, 55));
            g2.drawArc(x, y, size, size, 225, -270);

            if (value == -1) {
                // False alarm — draw blue X
                g2.setColor(ACCENT_BLUE);
                g2.drawArc(x, y, size, size, 225, -270);
                // Center: "✗"
                g2.setFont(new Font("Segoe UI", Font.BOLD, 32));
                FontMetrics fm = g2.getFontMetrics();
                String s = "✗";
                int tx = (getWidth() - fm.stringWidth(s)) / 2;
                int ty = getHeight() / 2 + fm.getAscent() / 3;
                g2.drawString(s, tx, ty);
            } else if (value == 0) {
                // Empty — just show dash
                g2.setFont(new Font("Segoe UI", Font.BOLD, 28));
                g2.setColor(TEXT_MUTED);
                FontMetrics fm = g2.getFontMetrics();
                String s = "—";
                int tx = (getWidth() - fm.stringWidth(s)) / 2;
                int ty = getHeight() / 2 + fm.getAscent() / 3;
                g2.drawString(s, tx, ty);
            } else {
                // Value arc
                Color arcColor;
                if (value >= 81) arcColor = ACCENT_RED;
                else if (value >= 61) arcColor = ACCENT_ORANGE;
                else if (value >= 31) arcColor = ACCENT_YELLOW;
                else arcColor = ACCENT_GREEN;

                int sweep = (int) (-270.0 * value / 100.0);
                g2.setColor(arcColor);
                g2.drawArc(x, y, size, size, 225, sweep);

                // Center text: score number
                g2.setFont(new Font("Segoe UI", Font.BOLD, 28));
                g2.setColor(TEXT_PRIMARY);
                FontMetrics fm = g2.getFontMetrics();
                String valStr = String.valueOf(value);
                int tx = (getWidth() - fm.stringWidth(valStr)) / 2;
                int ty = getHeight() / 2 + 4;
                g2.drawString(valStr, tx, ty);
            }

            // Level text below gauge
            g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
            g2.setColor(TEXT_MUTED);
            FontMetrics fm2 = g2.getFontMetrics();
            int lx = (getWidth() - fm2.stringWidth(level)) / 2;
            g2.drawString(level, lx, getHeight() / 2 + 22);

            g2.dispose();
        }
    }

    /** Gradient background panel */
    private static class GradientPanel extends JPanel {
        private final Color top, bottom;
        GradientPanel(Color top, Color bottom) {
            this.top = top; this.bottom = bottom;
            setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bottom));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }

    /** Custom history list renderer */
    private static class HistoryCellRenderer extends DefaultListCellRenderer {
        @Override public Component getListCellRendererComponent(
                JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            String text = value.toString();
            setFont(new Font("Consolas", Font.PLAIN, 11));
            setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 8));
            if (!isSelected) {
                setBackground(index % 2 == 0 ? new Color(10, 13, 20) : new Color(14, 18, 26));
                if (text.contains("✗") || text.contains("Fausse") || text.contains("False"))
                    setForeground(ACCENT_BLUE);
                else if (text.contains("CRITIQUE"))
                    setForeground(ACCENT_RED);
                else if (text.contains("ELEVE"))
                    setForeground(ACCENT_ORANGE);
                else if (text.contains("MOYEN"))
                    setForeground(ACCENT_YELLOW);
                else if (text.contains("seuil") || text.contains("○"))
                    setForeground(TEXT_MUTED);
                else
                    setForeground(TEXT_SECONDARY);
            }
            return this;
        }
    }

    // ── Utility ──
    private static Color darker(Color c, float factor) {
        return new Color(
                Math.max((int)(c.getRed() * factor), 0),
                Math.max((int)(c.getGreen() * factor), 0),
                Math.max((int)(c.getBlue() * factor), 0));
    }
}
