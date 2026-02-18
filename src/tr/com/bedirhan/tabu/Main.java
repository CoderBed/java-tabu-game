package tr.com.bedirhan.tabu;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Tabu Oyunu");
            frame.setSize(900, 600);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setLayout(new BorderLayout());

            // =========================
            // ÜST PANEL
            // =========================
            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
            topPanel.setPreferredSize(new Dimension(0, 95));
            topPanel.setBackground(Color.LIGHT_GRAY);

            JLabel teamLabel = new JLabel("Takım: A");
            JLabel scoreALabel = new JLabel("Skor(A): 0");
            JLabel scoreBLabel = new JLabel("Skor(B): 0");
            JLabel timeLabel = new JLabel("Süre: 60");
            Color normalTimeColor = timeLabel.getForeground();
            JLabel passLabel = new JLabel("Pas: 0/3");
            JLabel roundLabel = new JLabel("Tur: 1/6");
            JLabel roundStatsLabel = new JLabel("Bu Tur: ✓ 0 | ␣ 0 | ✕ 0");

            JLabel durationLabel = new JLabel("Tur Süresi:");
            Integer[] durationOptions = {60, 90, 120, 180};
            JComboBox<Integer> durationBox = new JComboBox<>(durationOptions);

            // === Ayarlar (oyun başlamadan seçilecek) ===
            JLabel roundsLabel = new JLabel("Toplam Tur:");
            Integer[] roundsOptions = {4, 6, 8};
            JComboBox<Integer> roundsBox = new JComboBox<>(roundsOptions);

            JLabel passLimitLabel = new JLabel("Pas Limiti:");
            String[] passLimitOptions = {"0", "3", "5", "Sınırsız"};
            JComboBox<String> passLimitBox = new JComboBox<>(passLimitOptions);

            JLabel tabooPenaltyLabel = new JLabel("Tabu Cezası:");
            String[] tabooPenaltyOptions = {"0", "-1", "-2"};
            JComboBox<String> tabooPenaltyBox = new JComboBox<>(tabooPenaltyOptions);

            JButton startButton = new JButton("BAŞLAT");

            JLabel infoLabel = new JLabel(" ");
            infoLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            JToggleButton darkModeToggle = new JToggleButton("🌙 Dark"); // listener'ı aşağıda (applyTheme sonrası) eklenecek

            // Takım isimleri + renk seçimi (oyun başlamadan)
            JLabel teamANameLabel = new JLabel("Takım A Adı:");
            JTextField teamANameField = new JTextField("Takım A", 8);

            JLabel teamBNameLabel = new JLabel("Takım B Adı:");
            JTextField teamBNameField = new JTextField("Takım B", 8);

            JButton teamAColorButton = new JButton("A Renk");
            JButton teamBColorButton = new JButton("B Renk");

            // Takım renkleri (seçilebilir)
            final Color[] teamAColor = { new Color(76, 175, 80) };   // varsayılan yeşil
            final Color[] teamBColor = { new Color(33, 150, 243) };  // varsayılan mavi

            // Takım isimleri (seçilebilir)
            final String[] teamAName = { "Takım A" };
            final String[] teamBName = { "Takım B" };

            // Oyun başladı mı? (bazı UI aksiyonları oyun başlamadan çalışmasın)
            final boolean[] started = {false};
            final boolean[] darkMode = {false};
            // updateTeamColors, applyTheme içinde kullanılıyor (aşağıda atanacak)
            final Runnable[] updateTeamColors = new Runnable[1];

            // Skor etiketlerini "badge" gibi göster
            Runnable applyTeamBadgeStyles = () -> {
                // Light/Dark'a göre badge kontrastını otomatik ayarla
                if (!darkMode[0]) {
                    // LIGHT: açık badge + koyu border
                    Color scoreABg = lighten(teamAColor[0], 0.72f);
                    Color scoreBBg = lighten(teamBColor[0], 0.75f);
                    styleBadgeLabel(scoreALabel, scoreABg, darken(teamAColor[0], 0.35f));
                    styleBadgeLabel(scoreBLabel, scoreBBg, darken(teamBColor[0], 0.35f));

                    // Takım etiketi badge stili (aktif takıma göre updateTeamColors içinde güncellenecek)
                    styleBadgeLabel(teamLabel, lighten(teamAColor[0], 0.72f), darken(teamAColor[0], 0.35f));
                } else {
                    // DARK: daha koyu badge + açık yazı + daha görünür border
                    Color scoreABg = darken(teamAColor[0], 0.45f);
                    Color scoreBBg = darken(teamBColor[0], 0.45f);
                    styleBadgeLabel(scoreALabel, scoreABg, Color.WHITE);
                    styleBadgeLabel(scoreBLabel, scoreBBg, Color.WHITE);

                    // Takım etiketi badge stili (aktif takıma göre updateTeamColors içinde güncellenecek)
                    styleBadgeLabel(teamLabel, darken(teamAColor[0], 0.45f), Color.WHITE);
                }
            };

            applyTeamBadgeStyles.run();

            Font topFont = new Font("Arial", Font.BOLD, 19);
            for (JComponent c : new JComponent[]{
                    teamLabel, scoreALabel, scoreBLabel, timeLabel,
                    passLabel, roundLabel, roundStatsLabel,
                    durationLabel, durationBox,
                    roundsLabel, roundsBox,
                    passLimitLabel, passLimitBox,
                    tabooPenaltyLabel, tabooPenaltyBox,
                    teamANameLabel, teamANameField,
                    teamBNameLabel, teamBNameField,
                    teamAColorButton, teamBColorButton,
                    darkModeToggle,
                    startButton
            }) c.setFont(topFont);

            topPanel.add(teamLabel);
            topPanel.add(scoreALabel);
            topPanel.add(scoreBLabel);
            topPanel.add(timeLabel);
            topPanel.add(passLabel);
            topPanel.add(roundLabel);
            topPanel.add(roundStatsLabel);
            topPanel.add(durationLabel);
            topPanel.add(durationBox);

            topPanel.add(roundsLabel);
            topPanel.add(roundsBox);

            topPanel.add(passLimitLabel);
            topPanel.add(passLimitBox);

            topPanel.add(tabooPenaltyLabel);
            topPanel.add(tabooPenaltyBox);

            topPanel.add(teamANameLabel);
            topPanel.add(teamANameField);
            topPanel.add(teamAColorButton);

            topPanel.add(teamBNameLabel);
            topPanel.add(teamBNameField);
            topPanel.add(teamBColorButton);

            // BAŞLAT butonunu üst panelden kaldırdık
            topPanel.add(infoLabel);

            // =========================
            // ORTA PANEL
            // =========================
            JPanel centerPanel = new JPanel();
            centerPanel.setBackground(Color.WHITE);
            centerPanel.setLayout(new OverlayLayout(centerPanel)); // ✅ tek yerde set

            // Kart paneli: yuvarlak köşe + hafif gölge
            JPanel cardPanel = new RoundedShadowPanel(26, 14);
            cardPanel.setPreferredSize(new Dimension(700, 330));
            cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
            cardPanel.setBackground(Color.WHITE);

            JLabel wordLabel = new JLabel("KELİME", SwingConstants.CENTER);
            wordLabel.setFont(new Font("Arial", Font.BOLD, 54));
            wordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            wordLabel.setForeground(new Color(25, 25, 25));

            JLabel tabooList = new JLabel("", SwingConstants.CENTER);
            tabooList.setFont(new Font("Arial", Font.PLAIN, 22));
            tabooList.setAlignmentX(Component.CENTER_ALIGNMENT);
            tabooList.setForeground(new Color(55, 55, 55));

            cardPanel.add(Box.createVerticalStrut(30));
            cardPanel.add(wordLabel);
            cardPanel.add(Box.createVerticalStrut(20));
            cardPanel.add(tabooList);

            // TUR BİTTİ OVERLAY (geri sayım + DEVAM)
            JPanel overlayPanel = new JPanel(new GridBagLayout());
            overlayPanel.setBackground(new Color(0, 0, 0, 180));
            overlayPanel.setVisible(false);

            JPanel overlayContent = new JPanel();
            overlayContent.setOpaque(false);
            overlayContent.setLayout(new BoxLayout(overlayContent, BoxLayout.Y_AXIS));

            JLabel overlayLabel = new JLabel("TUR BİTTİ", SwingConstants.CENTER);
            overlayLabel.setForeground(Color.WHITE);
            overlayLabel.setFont(new Font("Arial", Font.BOLD, 42));
            overlayLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Mini round summary label (inserted after overlayLabel)
            JLabel roundSummaryLabel = new JLabel(" ", SwingConstants.CENTER);
            roundSummaryLabel.setForeground(Color.WHITE);
            roundSummaryLabel.setFont(new Font("Arial", Font.PLAIN, 20));
            roundSummaryLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JButton continueButton = new JButton("DEVAM");
            continueButton.setFont(new Font("Arial", Font.BOLD, 18));
            continueButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            continueButton.setPreferredSize(new Dimension(160, 45));

            overlayContent.add(overlayLabel);
            overlayContent.add(Box.createVerticalStrut(10));
            overlayContent.add(roundSummaryLabel);
            overlayContent.add(Box.createVerticalStrut(20));
            overlayContent.add(continueButton);

            overlayPanel.add(overlayContent);

            // OverlayLayout sırası: alttan üste doğru
            centerPanel.add(cardPanel);     // en altta
            centerPanel.add(overlayPanel);  // tur bitti üstte

            Font btnFont = new Font("Arial", Font.BOLD, 18);
            // 🏁 OYUN SONU OVERLAY
            JPanel gameEndOverlay = new JPanel(new GridBagLayout());
            gameEndOverlay.setBackground(new Color(0, 0, 0, 180));
            gameEndOverlay.setVisible(false);

            JPanel gameEndContent = new JPanel();
            gameEndContent.setOpaque(false);
            gameEndContent.setLayout(new BoxLayout(gameEndContent, BoxLayout.Y_AXIS));
            // Daha uzun metin + butonlar sığsın (özellikle beraberlikte 2 karar butonu)
            gameEndContent.setMinimumSize(new Dimension(560, 360));
            gameEndContent.setPreferredSize(new Dimension(650, 420));
            gameEndContent.setBorder(new EmptyBorder(10, 10, 10, 10));

            JLabel gameEndTitle = new JLabel("OYUN BİTTİ", SwingConstants.CENTER);
            gameEndTitle.setForeground(Color.WHITE);
            gameEndTitle.setFont(new Font("Arial", Font.BOLD, 44));
            gameEndTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel gameEndResult = new JLabel(" ", SwingConstants.CENTER);
            gameEndResult.setForeground(Color.WHITE);
            gameEndResult.setFont(new Font("Arial", Font.BOLD, 22));
            gameEndResult.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel gameEndScores = new JLabel(" ", SwingConstants.CENTER);
            gameEndScores.setForeground(Color.WHITE);
            gameEndScores.setFont(new Font("Arial", Font.PLAIN, 22));
            gameEndScores.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel gameEndStats = new JLabel(" ", SwingConstants.CENTER);
            gameEndStats.setForeground(Color.WHITE);
            gameEndStats.setFont(new Font("Arial", Font.PLAIN, 18));
            gameEndStats.setAlignmentX(Component.CENTER_ALIGNMENT);

            JPanel gameEndButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 10));
            gameEndButtons.setOpaque(false);

            // Beraberlikte kullanıcı seçsin
            JButton endDrawButton = new JButton("BERABERE BİTSİN");
            JButton tieBreakButton = new JButton("TIE-BREAK");

            JButton replayButton = new JButton("TEKRAR OYNA");
            JButton backToSettingsButton = new JButton("YENİ OYUN");

            // Stil
            styleColoredButton(endDrawButton, new Color(255, 193, 7), Color.BLACK);      // Sarı
            styleColoredButton(tieBreakButton, new Color(156, 39, 176), Color.WHITE);   // Mor
            styleColoredButton(replayButton, new Color(76, 175, 80), Color.WHITE);
            styleColoredButton(backToSettingsButton, new Color(33, 150, 243), Color.WHITE);

            endDrawButton.setFont(btnFont);
            tieBreakButton.setFont(btnFont);
            replayButton.setFont(btnFont);
            backToSettingsButton.setFont(btnFont);

            endDrawButton.setPreferredSize(new Dimension(200, 45));
            tieBreakButton.setPreferredSize(new Dimension(160, 45));
            replayButton.setPreferredSize(new Dimension(180, 45));
            backToSettingsButton.setPreferredSize(new Dimension(180, 45));

            endDrawButton.setFocusable(false);
            tieBreakButton.setFocusable(false);
            replayButton.setFocusable(false);
            backToSettingsButton.setFocusable(false);

            // Varsayılan: sadece Replay / Yeni Oyun görünür (beraberlikte karar butonları açılacak)
            endDrawButton.setVisible(false);
            tieBreakButton.setVisible(false);

            gameEndButtons.add(endDrawButton);
            gameEndButtons.add(tieBreakButton);
            gameEndButtons.add(replayButton);
            gameEndButtons.add(backToSettingsButton);

            gameEndContent.add(gameEndTitle);
            gameEndContent.add(Box.createVerticalStrut(14));
            gameEndContent.add(gameEndResult);
            gameEndContent.add(Box.createVerticalStrut(10));
            gameEndContent.add(gameEndScores);
            gameEndContent.add(Box.createVerticalStrut(16));
            gameEndContent.add(gameEndStats);
            gameEndContent.add(Box.createVerticalStrut(14));
            // Butonlar her zaman görünür olsun
            gameEndContent.add(gameEndButtons);

            gameEndOverlay.add(gameEndContent);

            // overlaylar içinde en üstte dursun
            centerPanel.add(gameEndOverlay);

            // =========================
            // ALT PANEL
            // =========================
            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 25));
            bottomPanel.setPreferredSize(new Dimension(0, 100));
            bottomPanel.setBackground(Color.LIGHT_GRAY);

            // =========================
            // THEME (LIGHT / DARK)
            // =========================
            final Color LIGHT_FRAME_BG   = Color.WHITE;
            final Color LIGHT_PANEL_BG   = Color.LIGHT_GRAY;
            final Color LIGHT_CENTER_BG  = Color.WHITE;
            final Color LIGHT_CARD_BG    = Color.WHITE;
            final Color LIGHT_WORD_FG    = new Color(25, 25, 25);
            final Color LIGHT_TABOO_FG   = new Color(55, 55, 55);

            final Color DARK_FRAME_BG    = new Color(30, 30, 30);
            final Color DARK_PANEL_BG    = new Color(45, 45, 45);
            final Color DARK_CENTER_BG   = new Color(30, 30, 30);
            final Color DARK_CARD_BG     = new Color(40, 40, 40);
            final Color DARK_WORD_FG     = Color.WHITE;
            final Color DARK_TABOO_FG    = new Color(200, 200, 200);

            // Süre etiketi için tema-dostu normal renk (applyTheme içinde de kullanılıyor)
            Runnable setTimeLabelNormalColor = () -> {
                timeLabel.setForeground(darkMode[0] ? Color.WHITE : normalTimeColor);
            };


            Runnable applyTheme = () -> {
                if (!darkMode[0]) {
                    // LIGHT MODE
                    frame.getContentPane().setBackground(LIGHT_FRAME_BG);
                    topPanel.setBackground(LIGHT_PANEL_BG);
                    bottomPanel.setBackground(LIGHT_PANEL_BG);
                    centerPanel.setBackground(LIGHT_CENTER_BG);
                    cardPanel.setBackground(LIGHT_CARD_BG);
                    wordLabel.setForeground(LIGHT_WORD_FG);
                    tabooList.setForeground(LIGHT_TABOO_FG);
                    darkModeToggle.setText("🌙 Dark");
                    // Top panel label/text renklerini varsayılan koyu yap
                    Color lightText = Color.BLACK;

                    teamLabel.setForeground(lightText);
                    setTimeLabelNormalColor.run();
                    passLabel.setForeground(lightText);
                    roundLabel.setForeground(lightText);
                    roundStatsLabel.setForeground(lightText);
                    durationLabel.setForeground(lightText);
                    roundsLabel.setForeground(lightText);
                    passLimitLabel.setForeground(lightText);
                    tabooPenaltyLabel.setForeground(lightText);
                    teamANameLabel.setForeground(lightText);
                    teamBNameLabel.setForeground(lightText);
                    infoLabel.setForeground(lightText);

                    // Badge kontrastını güncelle
                    applyTeamBadgeStyles.run();
                    if (updateTeamColors[0] != null) updateTeamColors[0].run();
                } else {
                    // DARK MODE
                    frame.getContentPane().setBackground(DARK_FRAME_BG);
                    topPanel.setBackground(DARK_PANEL_BG);
                    bottomPanel.setBackground(DARK_PANEL_BG);
                    centerPanel.setBackground(DARK_CENTER_BG);
                    cardPanel.setBackground(DARK_CARD_BG);
                    wordLabel.setForeground(DARK_WORD_FG);
                    tabooList.setForeground(DARK_TABOO_FG);
                    darkModeToggle.setText("☀️ Light");
                    // Top panel label/text renklerini beyaz yap
                    Color darkText = Color.WHITE;

                    teamLabel.setForeground(darkText);
                    setTimeLabelNormalColor.run();
                    passLabel.setForeground(darkText);
                    roundLabel.setForeground(darkText);
                    roundStatsLabel.setForeground(darkText);
                    durationLabel.setForeground(darkText);
                    roundsLabel.setForeground(darkText);
                    passLimitLabel.setForeground(darkText);
                    tabooPenaltyLabel.setForeground(darkText);
                    teamANameLabel.setForeground(darkText);
                    teamBNameLabel.setForeground(darkText);
                    infoLabel.setForeground(darkText);

                    // Badge kontrastını güncelle
                    applyTeamBadgeStyles.run();
                    if (updateTeamColors[0] != null) updateTeamColors[0].run();
                }
            };

            // Animasyonlu tema geçişi (fade light ↔ dark)
            final boolean[] themeAnimating = {false};
            Runnable animateThemeToTarget = () -> {
                if (themeAnimating[0]) return;

                // Başlangıç renkleri (mevcut UI'dan oku)
                Color fromFrame  = frame.getContentPane().getBackground();
                Color fromTop    = topPanel.getBackground();
                Color fromBottom = bottomPanel.getBackground();
                Color fromCenter = centerPanel.getBackground();
                Color fromCard   = cardPanel.getBackground();
                Color fromWord   = wordLabel.getForeground();
                Color fromTaboo  = tabooList.getForeground();

                // Hedef renkleri (darkMode[0] state'ine göre)
                Color toFrame  = darkMode[0] ? DARK_FRAME_BG  : LIGHT_FRAME_BG;
                Color toTop    = darkMode[0] ? DARK_PANEL_BG  : LIGHT_PANEL_BG;
                Color toBottom = darkMode[0] ? DARK_PANEL_BG  : LIGHT_PANEL_BG;
                Color toCenter = darkMode[0] ? DARK_CENTER_BG : LIGHT_CENTER_BG;
                Color toCard   = darkMode[0] ? DARK_CARD_BG   : LIGHT_CARD_BG;
                Color toWord   = darkMode[0] ? DARK_WORD_FG   : LIGHT_WORD_FG;
                Color toTaboo  = darkMode[0] ? DARK_TABOO_FG  : LIGHT_TABOO_FG;

                final int steps = 14;      // daha yumuşak geçiş
                final int interval = 18;   // ms

                themeAnimating[0] = true;
                darkModeToggle.setEnabled(false);

                final int[] i = {0};
                Timer themeTimer = new Timer(interval, ev -> {
                    float t = i[0] / (float) steps; // 0..1

                    frame.getContentPane().setBackground(lerpColor(fromFrame, toFrame, t));
                    topPanel.setBackground(lerpColor(fromTop, toTop, t));
                    bottomPanel.setBackground(lerpColor(fromBottom, toBottom, t));
                    centerPanel.setBackground(lerpColor(fromCenter, toCenter, t));
                    cardPanel.setBackground(lerpColor(fromCard, toCard, t));
                    wordLabel.setForeground(lerpColor(fromWord, toWord, t));
                    tabooList.setForeground(lerpColor(fromTaboo, toTaboo, t));

                    // repaint
                    frame.repaint();

                    if (i[0] >= steps) {
                        ((Timer) ev.getSource()).stop();
                        themeAnimating[0] = false;
                        darkModeToggle.setEnabled(true);

                        // Final snap + toggle yazısı
                        applyTheme.run();
                        // Tema animasyonu bitince badge/etiket kontrastını kesinleştir
                        applyTeamBadgeStyles.run();
                        if (updateTeamColors[0] != null) updateTeamColors[0].run();
                    }

                    i[0]++;
                });
                themeTimer.setRepeats(true);
                themeTimer.start();
            };

            darkModeToggle.addActionListener(e -> {
                darkMode[0] = darkModeToggle.isSelected();
                animateThemeToTarget.run();
            });

            JButton correctButton = new JButton("▲ DOĞRU");
            JButton passButton = new JButton("␣ PAS");
            JButton tabooButton = new JButton("▼ TABU");
            JButton newGameButton = new JButton("YENİ OYUN");

            // Yeni: Pause'dan devam etmek için altta buton
            JButton resumeBottomButton = new JButton("DEVAM ET");
            resumeBottomButton.setVisible(false); // başlangıçta gizli

            // Buton renkleri + hover + disabled soluk görünüm
            styleColoredButton(correctButton, new Color(76, 175, 80), Color.WHITE);    // Yeşil
            styleColoredButton(passButton, new Color(255, 193, 7), Color.BLACK);       // Sarı
            styleColoredButton(tabooButton, new Color(244, 67, 54), Color.WHITE);      // Kırmızı
            styleColoredButton(newGameButton, new Color(33, 150, 243), Color.WHITE);  // Mavi
            styleColoredButton(resumeBottomButton, new Color(156, 39, 176), Color.WHITE); // Mor

            for (JButton b : new JButton[]{correctButton, passButton, tabooButton, newGameButton, resumeBottomButton}) {
                b.setFont(btnFont);
                b.setPreferredSize(new Dimension(160, 45));
                b.setFocusable(false);
            }

            correctButton.setToolTipText("Kısayol: ↑");
            passButton.setToolTipText("Kısayol: SPACE");
            tabooButton.setToolTipText("Kısayol: ↓");
            newGameButton.setToolTipText("Yeni oyun");
            resumeBottomButton.setToolTipText("Devam et (ESC / ENTER)");

            bottomPanel.add(correctButton);
            bottomPanel.add(passButton);
            bottomPanel.add(tabooButton);
            bottomPanel.add(newGameButton);
            bottomPanel.add(startButton); // BAŞLAT
            bottomPanel.add(darkModeToggle); // 🌙 Dark / ☀️ Light toggle ALT PANELE TAŞINDI
            bottomPanel.add(resumeBottomButton); // DEVAM ET

            // Focus sorunları
            durationBox.setFocusable(false);
            roundsBox.setFocusable(false);
            passLimitBox.setFocusable(false);
            tabooPenaltyBox.setFocusable(false);
            teamANameField.setFocusable(true);
            teamBNameField.setFocusable(true);
            teamAColorButton.setFocusable(false);
            teamBColorButton.setFocusable(false);
            startButton.setFocusable(false);
            continueButton.setFocusable(false);
            darkModeToggle.setFocusable(false);

            // Start butonuna stil
            styleColoredButton(startButton, new Color(0, 150, 136), Color.WHITE);
            // BAŞLAT butonu başlangıçta görünür olsun
            startButton.setVisible(true);

            // Renk seçimi (oyun başlamadan)
            teamAColorButton.addActionListener(e -> {
                if (started[0]) return;
                Color chosen = JColorChooser.showDialog(frame, "Takım A Rengi", teamAColor[0]);
                if (chosen != null) {
                    teamAColor[0] = chosen;
                    applyTeamBadgeStyles.run();
                    infoLabel.setText("Takım A rengi güncellendi.");
                }
            });

            teamBColorButton.addActionListener(e -> {
                if (started[0]) return;
                Color chosen = JColorChooser.showDialog(frame, "Takım B Rengi", teamBColor[0]);
                if (chosen != null) {
                    teamBColor[0] = chosen;
                    applyTeamBadgeStyles.run();
                    infoLabel.setText("Takım B rengi güncellendi.");
                }
            });

            // =========================
            // OYUN STATE
            // =========================
            int[] scoreA = {0}, scoreB = {0}, timeLeft = {60}, passCount = {0};
            int[] roundDuration = {60}, currentRound = {1};
            String[] team = {"A"};
            boolean[] paused = {false};
            boolean[] tieDecisionPending = {false}; // beraberlikte kullanıcı karar verecek

            // Ayarlar (Start ile set edilir)
            int[] totalRounds = {6};
            int[] passLimit = {3};        // -1 => sınırsız
            int[] tabooPenalty = {-1};    // 0 / -1 / -2

            // Tur içi istatistikler (her tur başında sıfırlanır)
            int[] roundCorrect = {0};
            int[] roundTaboo = {0};
            int[] roundPass = {0};
            int[] roundStartScore = {0}; // bu tur başlamadan önce aktif takımın skoru

            // Oyun geneli takım bazlı istatistikler
            int[] totalCorrectA = {0}, totalCorrectB = {0};
            int[] totalTabooA   = {0}, totalTabooB   = {0};
            int[] totalPassA    = {0}, totalPassB    = {0};

            // (final sabitler yerine, start ile ayarlanacak değişkenler kullanıyoruz)

            durationBox.setSelectedItem(60);
            roundsBox.setSelectedItem(6);
            passLimitBox.setSelectedItem("3");
            tabooPenaltyBox.setSelectedItem("-1");

            infoLabel.setText("Ayarları seçip BAŞLAT'a bas.");

            durationBox.addActionListener(e -> {
                roundDuration[0] = (int) durationBox.getSelectedItem();
                infoLabel.setText("Seçilen süre (" + roundDuration[0] + " sn) bir sonraki turda uygulanacak.");
            });

            // =========================
            // CSV
            // =========================
            List<WordCard> cards = loadCardsFromCsv("words.csv");
            if (cards.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "words.csv bulunamadı veya boş!");
                return;
            }

            List<Integer> deck = new ArrayList<>();
            int[] deckPos = {0};

            // Yakın geçmiş hafızası: son N kart tekrar etmesin
            final int RECENT_LIMIT = Math.min(10, Math.max(1, cards.size() - 1));
            Deque<Integer> recentHistory = new ArrayDeque<>();

            // Aynı tur içinde aynı kart tekrar etmesin
            Set<Integer> roundUsed = new HashSet<>();

            // Takımlar arası "hemen tekrar" engeli: önceki turda çıkan kartlar,
            // yeni turun ilk N kartında çıkmasın
            final int INTERTEAM_BLOCK_FIRST_N = 5;
            Set<Integer> prevRoundShown = new HashSet<>();
            int[] drawsThisRound = {0};

            Runnable shuffleDeck = () -> {
                deck.clear();
                for (int i = 0; i < cards.size(); i++) deck.add(i);
                Collections.shuffle(deck);
                deckPos[0] = 0;
            };

            Runnable showNextCard = () -> {
                // Deck biterse yeniden karıştır
                if (deckPos[0] >= deck.size()) shuffleDeck.run();

                int chosenIndex = -1;

                // 1) Önce: hem recent hem de roundUsed olmayan bir kart bul
                int tries = deck.size();
                while (tries-- > 0) {
                    if (deckPos[0] >= deck.size()) shuffleDeck.run();
                    int idx = deck.get(deckPos[0]++);
                    if (!recentHistory.contains(idx)
                            && !roundUsed.contains(idx)
                            && !(drawsThisRound[0] < INTERTEAM_BLOCK_FIRST_N && prevRoundShown.contains(idx))) {
                        chosenIndex = idx;
                        break;
                    }
                }

                // 2) Bulamazsa: recent kuralını gevşet, ama roundUsed'ı yine de koru
                if (chosenIndex == -1) {
                    tries = deck.size();
                    while (tries-- > 0) {
                        if (deckPos[0] >= deck.size()) shuffleDeck.run();
                        int idx = deck.get(deckPos[0]++);
                        if (!roundUsed.contains(idx)
                                && !(drawsThisRound[0] < INTERTEAM_BLOCK_FIRST_N && prevRoundShown.contains(idx))) {
                            chosenIndex = idx;
                            break;
                        }
                    }
                }
                // 3) Hâlâ yoksa: tur içinde tüm kartlar tüketilmiş olabilir -> roundUsed'ı sıfırla
                if (chosenIndex == -1) {
                    roundUsed.clear();
                    if (deckPos[0] >= deck.size()) shuffleDeck.run();
                    chosenIndex = deck.get(deckPos[0]++);
                }

                // Round-used güncelle
                roundUsed.add(chosenIndex);

                // Recent history güncelle
                recentHistory.addLast(chosenIndex);
                while (recentHistory.size() > RECENT_LIMIT) {
                    recentHistory.removeFirst();
                }

                WordCard c = cards.get(chosenIndex);

                // Kart değişimini animasyonla yap
                animateCardSwap(wordLabel, tabooList, () -> {
                    wordLabel.setText(c.word);
                    String[] shuffledTaboos = shuffledCopy(c.taboo);
                    tabooList.setText(toHtml(shuffledTaboos));
                });

                drawsThisRound[0]++;
            };

            // Oyun başlamadan önce kart yerine "OYUN HAZIR" ekranı
            Runnable showReadyScreen = () -> {
                overlayPanel.setVisible(false);
                gameEndOverlay.setVisible(false);

                cardPanel.setVisible(true);

                wordLabel.setText("OYUN HAZIR");
                wordLabel.setForeground(new Color(25, 25, 25));

                tabooList.setText(toHtml(new String[]{
                        "Ayarları üstten yap",
                        "BAŞLAT'a bas",
                        "",
                        "Kısayollar:",
                        "SPACE = PAS",
                        "↑ = DOĞRU",
                        "↓ = TABU",
                        "ESC = DURAKLAT"
                }));
                tabooList.setForeground(new Color(55, 55, 55));
            };

            shuffleDeck.run();

            // TIMERLAR

            Timer[] gameTimer = new Timer[1];
            Timer[] waitTimer = new Timer[1];
            final int[] wait = {0};

            Runnable refreshScores = () -> {
                scoreALabel.setText("Skor(A): " + scoreA[0]);
                scoreBLabel.setText("Skor(B): " + scoreB[0]);
            };

            updateTeamColors[0] = () -> {
                if (team[0].equals("A")) {
                    teamLabel.setText("Takım: " + teamAName[0]);
                    if (!darkMode[0]) {
                        styleBadgeLabel(teamLabel, lighten(teamAColor[0], 0.72f), darken(teamAColor[0], 0.35f));
                    } else {
                        styleBadgeLabel(teamLabel, darken(teamAColor[0], 0.45f), Color.WHITE);
                    }
                } else {
                    teamLabel.setText("Takım: " + teamBName[0]);
                    if (!darkMode[0]) {
                        styleBadgeLabel(teamLabel, lighten(teamBColor[0], 0.75f), darken(teamBColor[0], 0.35f));
                    } else {
                        styleBadgeLabel(teamLabel, darken(teamBColor[0], 0.45f), Color.WHITE);
                    }
                }
            };

            Runnable updatePassLabel = () -> {
                if (passLimit[0] == -1) {
                    passLabel.setText("Pas: " + passCount[0] + "/∞");
                } else {
                    passLabel.setText("Pas: " + passCount[0] + "/" + passLimit[0]);
                }
            };


            java.util.function.BooleanSupplier canPass = () -> {
                if (passLimit[0] == -1) return true;
                return passCount[0] < passLimit[0];
            };
            Runnable resetRoundStats = () -> {
                roundCorrect[0] = 0;
                roundTaboo[0] = 0;
                roundPass[0] = 0;
            };

            Runnable updateRoundStatsLabel = () -> {
                roundStatsLabel.setText(
                        "Bu Tur: ✓ " + roundCorrect[0] +
                                " | ␣ " + roundPass[0] +
                                " | ✕ " + roundTaboo[0]
                );
            };

            startButton.addActionListener(e -> {
                if (started[0]) return;

                // Ayarları uygula
                totalRounds[0] = (int) roundsBox.getSelectedItem();

                String passSel = (String) passLimitBox.getSelectedItem();
                passLimit[0] = passSel.equals("Sınırsız") ? -1 : Integer.parseInt(passSel);

                tabooPenalty[0] = Integer.parseInt((String) tabooPenaltyBox.getSelectedItem());

                // Takım isimlerini uygula
                String aName = teamANameField.getText() == null ? "" : teamANameField.getText().trim();
                String bName = teamBNameField.getText() == null ? "" : teamBNameField.getText().trim();
                teamAName[0] = aName.isEmpty() ? "Takım A" : aName;
                teamBName[0] = bName.isEmpty() ? "Takım B" : bName;

                // Renk/badge stillerini uygula
                applyTeamBadgeStyles.run();

                // Başlangıç state
                started[0] = true;
                paused[0] = false;

                scoreA[0] = 0;
                scoreB[0] = 0;
                team[0] = "A";
                currentRound[0] = 1;

                refreshScores.run();

                updateTeamColors[0].run();
                roundLabel.setText("Tur: " + currentRound[0] + "/" + totalRounds[0]);

                timeLeft[0] = roundDuration[0];
                setTimeLabelNormalColor.run();
                timeLabel.setText("Süre: " + timeLeft[0]);
                roundStartScore[0] = team[0].equals("A") ? scoreA[0] : scoreB[0];
                timeLabel.setVisible(true);

                passCount[0] = 0;
                updatePassLabel.run();
                resetRoundStats.run();
                updateRoundStatsLabel.run();
                totalCorrectA[0] = totalCorrectB[0] = 0;
                totalTabooA[0]   = totalTabooB[0]   = 0;
                totalPassA[0]    = totalPassB[0]    = 0;
                gameEndStats.setText(" ");
                recentHistory.clear();
                roundUsed.clear();
                prevRoundShown.clear();
                drawsThisRound[0] = 0;

                overlayPanel.setVisible(false);
                resumeBottomButton.setVisible(false);
                gameEndOverlay.setVisible(false);
                cardPanel.setVisible(true);

                // Butonları aç
                correctButton.setEnabled(true);
                tabooButton.setEnabled(true);
                passButton.setEnabled(canPass.getAsBoolean());

                // Ayarları kilitle
                durationBox.setEnabled(false);
                roundsBox.setEnabled(false);
                passLimitBox.setEnabled(false);
                tabooPenaltyBox.setEnabled(false);
                teamANameField.setEnabled(false);
                teamBNameField.setEnabled(false);
                teamAColorButton.setEnabled(false);
                teamBColorButton.setEnabled(false);
                startButton.setEnabled(false);

                // Oyun BAŞLADI: BAŞLAT butonunu gizle
                startButton.setVisible(false);
                bottomPanel.revalidate();
                bottomPanel.repaint();

                shuffleDeck.run();
                cardPanel.setVisible(true);
                showNextCard.run();

                infoLabel.setText("Oyun başladı! Kısayollar: SPACE=PAS, ↑=DOĞRU, ↓=TABU, ESC=DURAKLAT");

                updateTeamColors[0].run();
                gameTimer[0].start();
            });

            // Pause'tan devam (alttaki butonla)
            Runnable resumeGame = () -> {
                paused[0] = false;

                resumeBottomButton.setVisible(false);
                infoLabel.setText(" "); // istersen "Devam edildi" yazabiliriz

                correctButton.setEnabled(true);
                tabooButton.setEnabled(true);
                passButton.setEnabled(canPass.getAsBoolean());

                gameTimer[0].start();

                bottomPanel.revalidate();
                bottomPanel.repaint();
            };

            Runnable replaySameSettings = () -> {
                gameEndOverlay.setVisible(false);
                cardPanel.setVisible(true);

                if (waitTimer[0] != null) waitTimer[0].stop();
                if (gameTimer[0] != null) gameTimer[0].stop();

                started[0] = true;
                paused[0] = false;

                scoreA[0] = 0;
                scoreB[0] = 0;
                team[0] = "A";
                currentRound[0] = 1;

                refreshScores.run();
                updateTeamColors[0].run();
                roundLabel.setText("Tur: " + currentRound[0] + "/" + totalRounds[0]);

                timeLeft[0] = roundDuration[0];
                setTimeLabelNormalColor.run();
                timeLabel.setText("Süre: " + timeLeft[0]);
                timeLabel.setVisible(true);
                roundStartScore[0] = team[0].equals("A") ? scoreA[0] : scoreB[0];

                passCount[0] = 0;
                updatePassLabel.run();
                resetRoundStats.run();
                updateRoundStatsLabel.run();
                totalCorrectA[0] = totalCorrectB[0] = 0;
                totalTabooA[0]   = totalTabooB[0]   = 0;
                totalPassA[0]    = totalPassB[0]    = 0;
                gameEndStats.setText(" ");

                overlayPanel.setVisible(false);
                resumeBottomButton.setVisible(false);

                correctButton.setEnabled(true);
                tabooButton.setEnabled(true);
                passButton.setEnabled(canPass.getAsBoolean());

                // ayarlar kilitli kalsın
                durationBox.setEnabled(false);
                roundsBox.setEnabled(false);
                passLimitBox.setEnabled(false);
                tabooPenaltyBox.setEnabled(false);
                startButton.setEnabled(false);

                // tekrar kontrol yapılarını sıfırla
                recentHistory.clear();
                roundUsed.clear();
                prevRoundShown.clear();
                drawsThisRound[0] = 0;

                shuffleDeck.run();
                showNextCard.run();

                infoLabel.setText("Tekrar başladı! Kısayollar: SPACE=PAS, ↑=DOĞRU, ↓=TABU, ESC=DURAKLAT");
                gameTimer[0].start();
            };

            replayButton.addActionListener(e -> replaySameSettings.run());
            backToSettingsButton.addActionListener(e -> newGameButton.doClick());

            // Yeni tura geç (holder: bazı listener'lar daha önce tanımlandığı için)
            final Runnable[] goNextRoundNow = new Runnable[1];

            // Beraberlik: "Berabere bitsin" -> oyunu berabere bitir ve Replay/Yeni Oyun'u göster
            endDrawButton.addActionListener(e -> {
                if (!gameEndOverlay.isVisible()) return;

                tieDecisionPending[0] = false;

                // Karar verildi: Replay/Yeni Oyun görünsün, karar butonları gizlensin
                endDrawButton.setVisible(false);
                tieBreakButton.setVisible(false);
                replayButton.setVisible(true);
                backToSettingsButton.setVisible(true);

                gameEndButtons.revalidate();
                gameEndButtons.repaint();

                gameEndResult.setText("Berabere!");
                gameEndScores.setText(teamAName[0] + ": " + scoreA[0] + "   |   " + teamBName[0] + ": " + scoreB[0]);
                infoLabel.setText("Oyun berabere bitti.");
            });

            // Beraberlik: "Tie-break" -> 2 ekstra tur ekle ve oyuna devam et
            tieBreakButton.addActionListener(e -> {
                if (!gameEndOverlay.isVisible()) return;

                tieDecisionPending[0] = false;

                // Tie-break: iki takım da 1'er tur daha oynasın (toplam +2 tur)
                totalRounds[0] = totalRounds[0] + 2;

                // End overlay kapat, oyuna dön
                gameEndOverlay.setVisible(false);

                gameEndStats.setText(" ");

                // Kartı ve süreyi geri getir
                cardPanel.setVisible(true);
                timeLabel.setVisible(true);
                setTimeLabelNormalColor.run();

                // Karar butonlarını kapat (bir daha görünmesin)
                endDrawButton.setVisible(false);
                tieBreakButton.setVisible(false);

                // goNextRoundNow yeni turu başlatacak (totalRounds büyüdüğü için artık "game end"e girmez)
                infoLabel.setText("Tie-break! Her iki takım için 1'er tur eklendi.");
                goNextRoundNow[0].run();

                bottomPanel.revalidate();
                bottomPanel.repaint();

                gameEndButtons.revalidate();
                gameEndButtons.repaint();
                gameEndOverlay.revalidate();
                gameEndOverlay.repaint();
            });

            resumeBottomButton.addActionListener(e -> {
                if (paused[0]) resumeGame.run();
            });

            goNextRoundNow[0] = () -> {
                paused[0] = false;
                resumeBottomButton.setVisible(false);

                if (waitTimer[0] != null) waitTimer[0].stop();
                overlayPanel.setVisible(false);
                // Yeni tur başlarken kart tekrar görünsün
                cardPanel.setVisible(true);
                roundSummaryLabel.setText(" ");

                if (currentRound[0] >= totalRounds[0]) {
                    if (gameTimer[0] != null) gameTimer[0].stop();
                    if (waitTimer[0] != null) waitTimer[0].stop();

                    correctButton.setEnabled(false);
                    passButton.setEnabled(false);
                    tabooButton.setEnabled(false);
                    resumeBottomButton.setVisible(false);

                    String result;
                    if (scoreA[0] > scoreB[0]) result = "Kazanan: " + teamAName[0];
                    else if (scoreB[0] > scoreA[0]) result = "Kazanan: " + teamBName[0];
                    else result = "Berabere!";

                    boolean isTie = (scoreA[0] == scoreB[0]);
                    tieDecisionPending[0] = isTie;

                    // Beraberlikte: Replay/Yeni Oyun'u gizle, karar butonlarını göster
                    endDrawButton.setVisible(isTie);
                    tieBreakButton.setVisible(isTie);
                    replayButton.setVisible(!isTie);
                    backToSettingsButton.setVisible(!isTie);

                    // Görünürlük değişince layout'u tazele (özellikle beraberlikte karar butonları için)
                    gameEndButtons.revalidate();
                    gameEndButtons.repaint();
                    gameEndContent.revalidate();
                    gameEndContent.repaint();
                    gameEndOverlay.revalidate();
                    gameEndOverlay.repaint();

                    // Beraberlikte kullanıcıdan seçim iste
                    if (isTie) {
                        gameEndResult.setText("Berabere! Seçim yap:");
                    } else {
                        gameEndResult.setText(result);
                    }
                    gameEndScores.setText(teamAName[0] + ": " + scoreA[0] + "   |   " + teamBName[0] + ": " + scoreB[0]);

                    // ===== OYUN SONU DETAY RAPOR =====
                    int aTotalActions = totalCorrectA[0] + totalPassA[0] + totalTabooA[0];
                    int bTotalActions = totalCorrectB[0] + totalPassB[0] + totalTabooB[0];

                    int aCorrectPct = aTotalActions == 0 ? 0 : Math.round(100f * totalCorrectA[0] / aTotalActions);
                    int aPassPct    = aTotalActions == 0 ? 0 : Math.round(100f * totalPassA[0]    / aTotalActions);
                    int aTabooPct   = aTotalActions == 0 ? 0 : Math.round(100f * totalTabooA[0]   / aTotalActions);

                    int bCorrectPct = bTotalActions == 0 ? 0 : Math.round(100f * totalCorrectB[0] / bTotalActions);
                    int bPassPct    = bTotalActions == 0 ? 0 : Math.round(100f * totalPassB[0]    / bTotalActions);
                    int bTabooPct   = bTotalActions == 0 ? 0 : Math.round(100f * totalTabooB[0]   / bTotalActions);

                    String mostCorrect;
                    if (totalCorrectA[0] > totalCorrectB[0]) mostCorrect = "En çok doğru: " + teamAName[0] + " (" + totalCorrectA[0] + ")";
                    else if (totalCorrectB[0] > totalCorrectA[0]) mostCorrect = "En çok doğru: " + teamBName[0] + " (" + totalCorrectB[0] + ")";
                    else mostCorrect = "En çok doğru: Berabere (" + totalCorrectA[0] + ")";

                    String mostTaboo;
                    if (totalTabooA[0] > totalTabooB[0]) mostTaboo = "En çok tabu: " + teamAName[0] + " (" + totalTabooA[0] + ")";
                    else if (totalTabooB[0] > totalTabooA[0]) mostTaboo = "En çok tabu: " + teamBName[0] + " (" + totalTabooB[0] + ")";
                    else mostTaboo = "En çok tabu: Berabere (" + totalTabooA[0] + ")";

                    gameEndStats.setText(
                            "<html><center>"
                                    + mostCorrect + "<br>"
                                    + mostTaboo + "<br><br>"
                                    + "<b>Oranlar</b><br>"
                                    + teamAName[0] + ": ✓ " + aCorrectPct + "% | ␣ " + aPassPct + "% | ✕ " + aTabooPct + "%<br>"
                                    + teamBName[0] + ": ✓ " + bCorrectPct + "% | ␣ " + bPassPct + "% | ✕ " + bTabooPct + "%"
                                    + "</center></html>"
                    );
                    infoLabel.setText(isTie ? "Berabere! BERABERE BİTSİN veya TIE-BREAK seç." : ("Oyun bitti! " + result));

                    // ⏱ Süre rengini normale döndür (oyun bitti)
                    setTimeLabelNormalColor.run();
                    timeLabel.setVisible(false);

                    // Oyun bitti ekranında kartı gizle (üst üste binmesin)
                    cardPanel.setVisible(false);
                    wordLabel.setText("");
                    tabooList.setText("");

                    gameEndOverlay.setVisible(true);

                    bottomPanel.revalidate();
                    bottomPanel.repaint();
                    return;
                }

                team[0] = team[0].equals("A") ? "B" : "A";
                updateTeamColors[0].run();
                roundStartScore[0] = team[0].equals("A") ? scoreA[0] : scoreB[0];

                currentRound[0]++;
                roundLabel.setText("Tur: " + currentRound[0] + "/" + totalRounds[0]);

                timeLeft[0] = roundDuration[0];
                setTimeLabelNormalColor.run();
                timeLabel.setText("Süre: " + timeLeft[0]);
                infoLabel.setText(" ");

                passCount[0] = 0;
                updatePassLabel.run();

                // Önceki turda çıkanları kaydet (bir sonraki turun ilk 5 kartında gelmesin)
                prevRoundShown.clear();
                prevRoundShown.addAll(roundUsed);

                // Yeni tur istatistikleri sıfırla
                resetRoundStats.run();
                updateRoundStatsLabel.run();
                roundUsed.clear();
                drawsThisRound[0] = 0;

                passButton.setEnabled(true);
                correctButton.setEnabled(true);
                tabooButton.setEnabled(true);

                shuffleDeck.run();
                showNextCard.run();

                updateTeamColors[0].run();
                gameTimer[0].start();

                bottomPanel.revalidate();
                bottomPanel.repaint();
            };

            continueButton.addActionListener(e -> {
                cardPanel.setVisible(true);
                goNextRoundNow[0].run();
            });

            // ENTER = DEVAM (tur bitti ekranında)
            overlayPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke("ENTER"), "continueRound");
            overlayPanel.getActionMap().put("continueRound", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (overlayPanel.isVisible()) goNextRoundNow[0].run();
                }
            });

            gameTimer[0] = new Timer(1000, e -> {
                if (paused[0]) return;

                timeLeft[0]--;
                timeLabel.setText("Süre: " + timeLeft[0]);

                // Son 10 saniye: süre kırmızı olsun
                if (timeLeft[0] <= 10) {
                    timeLabel.setForeground(Color.RED);
                } else {
                    setTimeLabelNormalColor.run();
                }

                // Son 3 saniye: bip sesi
                if (timeLeft[0] <= 3 && timeLeft[0] > 0) {
                    Toolkit.getDefaultToolkit().beep();
                }

                if (timeLeft[0] <= 0) {
                    gameTimer[0].stop();

                    correctButton.setEnabled(false);
                    passButton.setEnabled(false);
                    tabooButton.setEnabled(false);

                    overlayPanel.setVisible(true);
                    // Tur bitti ekranında kart görünmesin
                    cardPanel.setVisible(false);

                    // Mini tur özeti (overlay içinde)
                    int teamEndScore = team[0].equals("A") ? scoreA[0] : scoreB[0];
                    int gained = teamEndScore - roundStartScore[0];
                    String gainedText = (gained >= 0 ? "+" : "") + gained;

                    roundSummaryLabel.setText(
                            "<html><center>"
                                    + "Bu Tur: ✓ " + roundCorrect[0]
                                    + " | ␣ " + roundPass[0]
                                    + " | ✕ " + roundTaboo[0]
                                    + "<br>Tur Puanı: " + gainedText
                                    + "</center></html>"
                    );

                    wait[0] = 10;
                    overlayLabel.setText("TUR BİTTİ (" + wait[0] + ")");

                    waitTimer[0] = new Timer(1000, ev -> {
                        wait[0]--;
                        overlayLabel.setText("TUR BİTTİ (" + wait[0] + ")");
                        if (wait[0] <= 0) goNextRoundNow[0].run();
                    });
                    waitTimer[0].start();
                }
            });

            // =========================
            // BUTONLAR
            // =========================
            correctButton.addActionListener(e -> {
                if (paused[0]) return;
                if (!started[0]) return;
                roundCorrect[0]++;
                updateRoundStatsLabel.run();
                if (team[0].equals("A")) {
                    scoreA[0]++;
                    totalCorrectA[0]++;
                } else {
                    scoreB[0]++;
                    totalCorrectB[0]++;
                }
                refreshScores.run();
                showNextCard.run();
            });

            passButton.addActionListener(e -> {
                if (paused[0]) return;
                if (!started[0]) return;
                roundPass[0]++;
                updateRoundStatsLabel.run();
                passCount[0]++;
                if (team[0].equals("A")) totalPassA[0]++; else totalPassB[0]++;
                updatePassLabel.run();
                if (!canPass.getAsBoolean()) passButton.setEnabled(false);
                showNextCard.run();
            });

            tabooButton.addActionListener(e -> {
                if (paused[0]) return;
                if (!started[0]) return;
                roundTaboo[0]++;
                updateRoundStatsLabel.run();
                if (team[0].equals("A")) {
                    scoreA[0] += tabooPenalty[0];
                    totalTabooA[0]++;
                } else {
                    scoreB[0] += tabooPenalty[0];
                    totalTabooB[0]++;
                }
                refreshScores.run();
                showNextCard.run();
            });

            newGameButton.addActionListener(e -> {
                if (waitTimer[0] != null) waitTimer[0].stop();
                if (gameTimer[0] != null) gameTimer[0].stop();

                started[0] = false;

                // Ayarları tekrar aç
                durationBox.setEnabled(true);
                roundsBox.setEnabled(true);
                passLimitBox.setEnabled(true);
                tabooPenaltyBox.setEnabled(true);
                teamANameField.setEnabled(true);
                teamBNameField.setEnabled(true);
                teamAColorButton.setEnabled(true);
                teamBColorButton.setEnabled(true);
                startButton.setEnabled(true);

                // Oyun butonlarını kapat (Start beklesin)
                correctButton.setEnabled(false);
                passButton.setEnabled(false);
                tabooButton.setEnabled(false);

                paused[0] = false;
                resumeBottomButton.setVisible(false);
                overlayPanel.setVisible(false);
                gameEndOverlay.setVisible(false);
                cardPanel.setVisible(true);

                scoreA[0] = 0;
                scoreB[0] = 0;
                team[0] = "A";
                currentRound[0] = 1;

                updateTeamColors[0].run();
                roundLabel.setText("Tur: " + currentRound[0] + "/" + (int) roundsBox.getSelectedItem());

                timeLeft[0] = roundDuration[0];
                setTimeLabelNormalColor.run();
                timeLabel.setText("Süre: " + timeLeft[0]);
                timeLabel.setVisible(true);
                infoLabel.setText("Ayarları seçip BAŞLAT'a bas.");

                passCount[0] = 0;
                updatePassLabel.run();

                resetRoundStats.run();
                updateRoundStatsLabel.run();
                totalCorrectA[0] = totalCorrectB[0] = 0;
                totalTabooA[0]   = totalTabooB[0]   = 0;
                totalPassA[0]    = totalPassB[0]    = 0;
                gameEndStats.setText(" ");

                // Clear recent history for new game
                recentHistory.clear();
                roundUsed.clear();
                prevRoundShown.clear();
                drawsThisRound[0] = 0;

                shuffleDeck.run();
                showReadyScreen.run();
                refreshScores.run();

                updateTeamColors[0].run();
                // YENİ OYUN'da BAŞLAT butonunu tekrar göster
                startButton.setVisible(true);
                bottomPanel.revalidate();
                bottomPanel.repaint();
            });

            // KLAVYE KISAYOLLARI
            // SPACE = PAS
            frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke("pressed SPACE"), "passAction");
            frame.getRootPane().getActionMap().put("passAction", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!started[0]) return;
                    if (!overlayPanel.isVisible() && !paused[0] && passButton.isEnabled()) {
                        passButton.doClick();
                    }
                }
            });

            // UP = DOĞRU
            frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke("pressed UP"), "correctAction");
            frame.getRootPane().getActionMap().put("correctAction", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!started[0]) return;
                    if (!overlayPanel.isVisible() && !paused[0] && correctButton.isEnabled()) {
                        correctButton.doClick();
                    }
                }
            });

            // DOWN = TABU
            frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke("pressed DOWN"), "tabooAction");
            frame.getRootPane().getActionMap().put("tabooAction", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!started[0]) return;
                    if (!overlayPanel.isVisible() && !paused[0] && tabooButton.isEnabled()) {
                        tabooButton.doClick();
                    }
                }
            });

            // ESC = PAUSE / RESUME
            frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke("pressed ESCAPE"), "togglePause");
            frame.getRootPane().getActionMap().put("togglePause", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (overlayPanel.isVisible() || (gameEndOverlay.isVisible() && tieDecisionPending[0])) return;
                    if (!started[0]) return;

                    if (!paused[0]) {
                        paused[0] = true;
                        gameTimer[0].stop();

                        correctButton.setEnabled(false);
                        passButton.setEnabled(false);
                        tabooButton.setEnabled(false);

                        // Ortada overlay yok: altta butonu aç
                        resumeBottomButton.setVisible(true);
                        pulseButton(resumeBottomButton, new Color(156, 39, 176), Color.WHITE);
                        infoLabel.setText("Duraklatıldı (ESC veya DEVAM ET ile devam).");

                        bottomPanel.revalidate();
                        bottomPanel.repaint();
                    } else {
                        resumeGame.run();
                    }
                }
            });

            // ENTER = DEVAM ET (pause modundayken)
            frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke("pressed ENTER"), "resumeOnEnter");
            frame.getRootPane().getActionMap().put("resumeOnEnter", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Tur bitti ekranındayken ENTER zaten overlayPanel tarafından yönetiliyor
                    if (overlayPanel.isVisible()) return;

                    // Pause modundaysa ENTER ile devam
                    if (paused[0]) {
                        resumeGame.run();
                    }
                }
            });

            // PANELE EKLE

            frame.add(topPanel, BorderLayout.NORTH);
            frame.add(centerPanel, BorderLayout.CENTER);
            frame.add(bottomPanel, BorderLayout.SOUTH);

            refreshScores.run();
            updateTeamColors[0].run();
            resetRoundStats.run();
            updateRoundStatsLabel.run();
            recentHistory.clear();
            roundUsed.clear();
            prevRoundShown.clear();
            drawsThisRound[0] = 0;
            // Start'a basılana kadar oyun başlamasın
            correctButton.setEnabled(false);
            passButton.setEnabled(false);
            tabooButton.setEnabled(false);
            infoLabel.setText("Ayarları seçip BAŞLAT'a bas.");

            showReadyScreen.run();

            updateTeamColors[0].run();
            applyTheme.run();
            frame.setVisible(true);
            frame.getRootPane().requestFocusInWindow();
        });
    }

    // Badge style for score labels
    static void styleBadgeLabel(JLabel label, Color bg, Color fg) {
        label.setOpaque(true);
        label.setBackground(bg);
        label.setForeground(fg);
        // Border: arka plan koyuysa biraz aç, açıkysa biraz koyulaştır
        boolean darkBg = (bg.getRed() + bg.getGreen() + bg.getBlue()) < (3 * 128);
        Color border = darkBg ? lighten(bg, 0.22f) : darken(bg, 0.18f);
        label.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(border, 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
    }

    // UI HELPERS (Hover + Disabled)

    static Color darken(Color c, float factor) {
        factor = Math.max(0f, Math.min(1f, factor));
        int r = Math.max(0, Math.round(c.getRed() * (1f - factor)));
        int g = Math.max(0, Math.round(c.getGreen() * (1f - factor)));
        int b = Math.max(0, Math.round(c.getBlue() * (1f - factor)));
        return new Color(r, g, b);
    }

    static Color lighten(Color c, float factor) {
        factor = Math.max(0f, Math.min(1f, factor));
        int r = Math.min(255, Math.round(c.getRed() + (255 - c.getRed()) * factor));
        int g = Math.min(255, Math.round(c.getGreen() + (255 - c.getGreen()) * factor));
        int b = Math.min(255, Math.round(c.getBlue() + (255 - c.getBlue()) * factor));
        return new Color(r, g, b);
    }

    static Color lerpColor(Color a, Color b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int r = Math.round(a.getRed() + (b.getRed() - a.getRed()) * t);
        int g = Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int bl = Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t);
        int al = Math.round(a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t);
        return new Color(
                Math.max(0, Math.min(255, r)),
                Math.max(0, Math.min(255, g)),
                Math.max(0, Math.min(255, bl)),
                Math.max(0, Math.min(255, al))
        );
    }

    static void pulseButton(JButton button, Color normalBg, Color normalFg) {
        // Stop an existing pulse if any
        Object existing = button.getClientProperty("pulseTimer");
        if (existing instanceof Timer t) {
            t.stop();
        }

        Color pulseBg = lighten(normalBg, 0.28f);

        final int[] tick = {0};
        Timer timer = new Timer(90, e -> {
            // 0: pulse, 1: normal, 2: pulse, 3: normal, then stop
            boolean on = (tick[0] % 2 == 0);
            if (button.isEnabled()) {
                button.setBackground(on ? pulseBg : normalBg);
                button.setForeground(normalFg);
            }
            tick[0]++;
            if (tick[0] >= 4) {
                ((Timer) e.getSource()).stop();
                button.putClientProperty("pulseTimer", null);
                if (button.isEnabled()) {
                    button.setBackground(normalBg);
                    button.setForeground(normalFg);
                }
            }
        });
        button.putClientProperty("pulseTimer", timer);
        timer.setRepeats(true);
        timer.start();
    }

    static void styleColoredButton(JButton button, Color normalBg, Color normalFg) {
        Color hoverBg = darken(normalBg, 0.12f);
        Color disabledBg = new Color(200, 200, 200);
        Color disabledFg = new Color(120, 120, 120);

        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);

        // Initial state
        button.setBackground(normalBg);
        button.setForeground(normalFg);

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) button.setBackground(hoverBg);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (button.isEnabled()) button.setBackground(normalBg);
            }
        });

        // Disabled look (when setEnabled(false) is called)
        button.addPropertyChangeListener("enabled", evt -> {
            if (button.isEnabled()) {
                button.setBackground(normalBg);
                button.setForeground(normalFg);
            } else {
                button.setBackground(disabledBg);
                button.setForeground(disabledFg);
            }
        });
    }

    static List<WordCard> loadCardsFromCsv(String filename) {
        List<WordCard> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.trim().split("\\s*,\\s*");
                if (p.length >= 6) list.add(new WordCard(p[0], new String[]{p[1], p[2], p[3], p[4], p[5]}));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    static String toHtml(String[] t) {
        StringBuilder sb = new StringBuilder("<html>");
        for (String s : t) sb.append("- ").append(s).append("<br>");
        return sb.append("</html>").toString();
    }

    // Tabu kelimeleri (ana kelimeyi etkilemeden) her gösterimde karıştırmak için
    static String[] shuffledCopy(String[] arr) {
        if (arr == null) return new String[0];
        List<String> list = new ArrayList<>();
        for (String s : arr) list.add(s);
        Collections.shuffle(list);
        return list.toArray(new String[0]);
    }

    // CUSTOM PANEL: Rounded corners + shadow

    static class RoundedShadowPanel extends JPanel {
        private final int radius;
        private final int shadowSize;

        RoundedShadowPanel(int radius, int shadowSize) {
            super();
            this.radius = radius;
            this.shadowSize = shadowSize;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Shadow (bottom-right)
                for (int i = shadowSize; i >= 1; i--) {
                    float alpha = 0.06f * (i / (float) shadowSize);
                    g2.setColor(new Color(0, 0, 0, Math.min(255, Math.max(0, Math.round(alpha * 255)))));
                    Shape shadow = new RoundRectangle2D.Float(
                            i, i,
                            w - (2f * i), h - (2f * i),
                            radius, radius
                    );
                    g2.fill(shadow);
                }

                // Main rounded background
                Shape round = new RoundRectangle2D.Float(
                        0, 0,
                        w - shadowSize, h - shadowSize,
                        radius, radius
                );
                g2.setColor(getBackground());
                g2.fill(round);
            } finally {
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }

    static class WordCard {
        String word;
        String[] taboo;
        WordCard(String w, String[] t) { word = w; taboo = t; }
    }

    // CARD ANIMATION: Fade-out -> swap text -> fade-in

    static void animateCardSwap(JLabel wordLabel, JLabel tabooLabel, Runnable swapText) {
        // Stop any ongoing animation on this word label
        Object existing = wordLabel.getClientProperty("cardAnimTimer");
        if (existing instanceof Timer t) {
            t.stop();
        }

        // Base colors (keep RGB, only animate alpha)
        Color baseWord = wordLabel.getForeground();
        Color baseTaboo = tabooLabel.getForeground();

        final int steps = 8;          // smoothness
        final int intervalMs = 22;    // speed

        final int[] phase = {0};      // 0 = fade out, 1 = fade in
        final int[] i = {0};

        Timer timer = new Timer(intervalMs, e -> {
            // t from 0..1
            float t = i[0] / (float) steps;

            if (phase[0] == 0) {
                // fade out: alpha 255 -> 0
                int a = Math.max(0, Math.min(255, Math.round(255 * (1f - t))));
                wordLabel.setForeground(new Color(baseWord.getRed(), baseWord.getGreen(), baseWord.getBlue(), a));
                tabooLabel.setForeground(new Color(baseTaboo.getRed(), baseTaboo.getGreen(), baseTaboo.getBlue(), a));

                if (i[0] >= steps) {
                    // swap at fully faded
                    swapText.run();
                    phase[0] = 1;
                    i[0] = 0;
                    return;
                }
            } else {
                // fade in: alpha 0 -> 255
                int a = Math.max(0, Math.min(255, Math.round(255 * t)));
                wordLabel.setForeground(new Color(baseWord.getRed(), baseWord.getGreen(), baseWord.getBlue(), a));
                tabooLabel.setForeground(new Color(baseTaboo.getRed(), baseTaboo.getGreen(), baseTaboo.getBlue(), a));

                if (i[0] >= steps) {
                    ((Timer) e.getSource()).stop();
                    wordLabel.putClientProperty("cardAnimTimer", null);

                    // ensure fully visible at end
                    wordLabel.setForeground(new Color(baseWord.getRed(), baseWord.getGreen(), baseWord.getBlue(), 255));
                    tabooLabel.setForeground(new Color(baseTaboo.getRed(), baseTaboo.getGreen(), baseTaboo.getBlue(), 255));
                    return;
                }
            }

            i[0]++;
        });

        wordLabel.putClientProperty("cardAnimTimer", timer);
        timer.setRepeats(true);
        timer.start();
    }
}