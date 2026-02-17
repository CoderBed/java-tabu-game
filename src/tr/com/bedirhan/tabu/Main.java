package tr.com.bedirhan.tabu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
            JLabel passLabel = new JLabel("Pas: 0/3");
            JLabel roundLabel = new JLabel("Tur: 1/6");

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

            Font topFont = new Font("Arial", Font.BOLD, 18);
            for (JComponent c : new JComponent[]{
                    teamLabel, scoreALabel, scoreBLabel, timeLabel,
                    passLabel, roundLabel,
                    durationLabel, durationBox,
                    roundsLabel, roundsBox,
                    passLimitLabel, passLimitBox,
                    tabooPenaltyLabel, tabooPenaltyBox,
                    startButton
            }) c.setFont(topFont);

            topPanel.add(teamLabel);
            topPanel.add(scoreALabel);
            topPanel.add(scoreBLabel);
            topPanel.add(timeLabel);
            topPanel.add(passLabel);
            topPanel.add(roundLabel);
            topPanel.add(durationLabel);
            topPanel.add(durationBox);

            topPanel.add(roundsLabel);
            topPanel.add(roundsBox);

            topPanel.add(passLimitLabel);
            topPanel.add(passLimitBox);

            topPanel.add(tabooPenaltyLabel);
            topPanel.add(tabooPenaltyBox);

            topPanel.add(startButton);
            topPanel.add(infoLabel);

            // =========================
            // ORTA PANEL
            // =========================
            JPanel centerPanel = new JPanel();
            centerPanel.setBackground(Color.WHITE);
            centerPanel.setLayout(new OverlayLayout(centerPanel)); // ✅ tek yerde set

            JPanel cardPanel = new JPanel();
            cardPanel.setPreferredSize(new Dimension(700, 330));
            cardPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 3));
            cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
            cardPanel.setBackground(Color.WHITE);

            JLabel wordLabel = new JLabel("KELİME", SwingConstants.CENTER);
            wordLabel.setFont(new Font("Arial", Font.BOLD, 54));
            wordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel tabooList = new JLabel("", SwingConstants.CENTER);
            tabooList.setFont(new Font("Arial", Font.PLAIN, 22));
            tabooList.setAlignmentX(Component.CENTER_ALIGNMENT);

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

            JButton continueButton = new JButton("DEVAM");
            continueButton.setFont(new Font("Arial", Font.BOLD, 18));
            continueButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            continueButton.setPreferredSize(new Dimension(160, 45));

            overlayContent.add(overlayLabel);
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
            gameEndContent.setPreferredSize(new Dimension(520, 300));

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

            JPanel gameEndButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 10));
            gameEndButtons.setOpaque(false);

            JButton replayButton = new JButton("TEKRAR OYNA");
            JButton backToSettingsButton = new JButton("YENİ OYUN");

            styleColoredButton(replayButton, new Color(76, 175, 80), Color.WHITE);
            styleColoredButton(backToSettingsButton, new Color(33, 150, 243), Color.WHITE);

            replayButton.setFont(btnFont);
            backToSettingsButton.setFont(btnFont);
            replayButton.setPreferredSize(new Dimension(180, 45));
            backToSettingsButton.setPreferredSize(new Dimension(180, 45));
            replayButton.setFocusable(false);
            backToSettingsButton.setFocusable(false);

            gameEndButtons.add(replayButton);
            gameEndButtons.add(backToSettingsButton);

            gameEndContent.add(gameEndTitle);
            gameEndContent.add(Box.createVerticalStrut(18));
            gameEndContent.add(gameEndResult);
            gameEndContent.add(Box.createVerticalStrut(10));
            gameEndContent.add(gameEndScores);
            gameEndContent.add(Box.createVerticalStrut(20));
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

            JButton correctButton = new JButton("DOĞRU");
            JButton passButton = new JButton("PAS");
            JButton tabooButton = new JButton("TABU");
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

            bottomPanel.add(correctButton);
            bottomPanel.add(passButton);
            bottomPanel.add(tabooButton);
            bottomPanel.add(newGameButton);
            bottomPanel.add(resumeBottomButton); // yeni oyun yanında

            // Focus sorunları
            durationBox.setFocusable(false);
            roundsBox.setFocusable(false);
            passLimitBox.setFocusable(false);
            tabooPenaltyBox.setFocusable(false);
            startButton.setFocusable(false);
            continueButton.setFocusable(false);

            // Start butonuna stil
            styleColoredButton(startButton, new Color(0, 150, 136), Color.WHITE);

            // =========================
            // OYUN STATE
            // =========================
            int[] scoreA = {0}, scoreB = {0}, timeLeft = {60}, passCount = {0};
            int[] roundDuration = {60}, currentRound = {1};
            String[] team = {"A"};
            boolean[] paused = {false};
            boolean[] started = {false};

            // Ayarlar (Start ile set edilir)
            int[] totalRounds = {6};
            int[] passLimit = {3};        // -1 => sınırsız
            int[] tabooPenalty = {-1};    // 0 / -1 / -2

            // Tur içi istatistikler (her tur başında sıfırlanır)
            int[] roundCorrect = {0};
            int[] roundTaboo = {0};
            int[] roundPass = {0};

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
                wordLabel.setText(c.word);
                tabooList.setText(toHtml(c.taboo));
                drawsThisRound[0]++;
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

            startButton.addActionListener(e -> {
                if (started[0]) return;

                // Ayarları uygula
                totalRounds[0] = (int) roundsBox.getSelectedItem();

                String passSel = (String) passLimitBox.getSelectedItem();
                passLimit[0] = passSel.equals("Sınırsız") ? -1 : Integer.parseInt(passSel);

                tabooPenalty[0] = Integer.parseInt((String) tabooPenaltyBox.getSelectedItem());

                // Başlangıç state
                started[0] = true;
                paused[0] = false;

                scoreA[0] = 0;
                scoreB[0] = 0;
                team[0] = "A";
                currentRound[0] = 1;

                refreshScores.run();

                teamLabel.setText("Takım: " + team[0]);
                roundLabel.setText("Tur: " + currentRound[0] + "/" + totalRounds[0]);

                timeLeft[0] = roundDuration[0];
                timeLabel.setText("Süre: " + timeLeft[0]);

                passCount[0] = 0;
                updatePassLabel.run();
                resetRoundStats.run();
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
                startButton.setEnabled(false);

                shuffleDeck.run();
                showNextCard.run();

                infoLabel.setText("Oyun başladı! Kısayollar: SPACE=PAS, ↑=DOĞRU, ↓=TABU, ESC=DURAKLAT");

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
                teamLabel.setText("Takım: " + team[0]);
                roundLabel.setText("Tur: " + currentRound[0] + "/" + totalRounds[0]);

                timeLeft[0] = roundDuration[0];
                timeLabel.setText("Süre: " + timeLeft[0]);

                passCount[0] = 0;
                updatePassLabel.run();
                resetRoundStats.run();

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

            resumeBottomButton.addActionListener(e -> {
                if (paused[0]) resumeGame.run();
            });

            // Yeni tura geç
            Runnable goNextRoundNow = () -> {
                paused[0] = false;
                resumeBottomButton.setVisible(false);

                if (waitTimer[0] != null) waitTimer[0].stop();
                overlayPanel.setVisible(false);

                if (currentRound[0] >= totalRounds[0]) {
                    if (gameTimer[0] != null) gameTimer[0].stop();
                    if (waitTimer[0] != null) waitTimer[0].stop();

                    correctButton.setEnabled(false);
                    passButton.setEnabled(false);
                    tabooButton.setEnabled(false);
                    resumeBottomButton.setVisible(false);

                    String result;
                    if (scoreA[0] > scoreB[0]) result = "Kazanan: Takım A";
                    else if (scoreB[0] > scoreA[0]) result = "Kazanan: Takım B";
                    else result = "Berabere!";

                    gameEndResult.setText(result);
                    gameEndScores.setText("Skor(A): " + scoreA[0] + "   |   Skor(B): " + scoreB[0]);
                    infoLabel.setText("Oyun bitti! " + result);

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
                teamLabel.setText("Takım: " + team[0]);

                currentRound[0]++;
                roundLabel.setText("Tur: " + currentRound[0] + "/" + totalRounds[0]);

                timeLeft[0] = roundDuration[0];
                timeLabel.setText("Süre: " + timeLeft[0]);
                infoLabel.setText(" ");

                passCount[0] = 0;
                updatePassLabel.run();

                // Önceki turda çıkanları kaydet (bir sonraki turun ilk 5 kartında gelmesin)
                prevRoundShown.clear();
                prevRoundShown.addAll(roundUsed);

                // Yeni tur istatistikleri sıfırla
                resetRoundStats.run();
                roundUsed.clear();
                drawsThisRound[0] = 0;

                passButton.setEnabled(true);
                correctButton.setEnabled(true);
                tabooButton.setEnabled(true);

                shuffleDeck.run();
                showNextCard.run();

                gameTimer[0].start();

                bottomPanel.revalidate();
                bottomPanel.repaint();
            };

            continueButton.addActionListener(e -> goNextRoundNow.run());

            // ENTER = DEVAM (tur bitti ekranında)
            overlayPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke("ENTER"), "continueRound");
            overlayPanel.getActionMap().put("continueRound", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (overlayPanel.isVisible()) goNextRoundNow.run();
                }
            });

            gameTimer[0] = new Timer(1000, e -> {
                if (paused[0]) return;

                timeLeft[0]--;
                timeLabel.setText("Süre: " + timeLeft[0]);

                if (timeLeft[0] <= 0) {
                    gameTimer[0].stop();

                    correctButton.setEnabled(false);
                    passButton.setEnabled(false);
                    tabooButton.setEnabled(false);

                    overlayPanel.setVisible(true);

                    // Tur özeti (10 sn ekranda kalsın)
                    int teamScore = team[0].equals("A") ? scoreA[0] : scoreB[0];
                    infoLabel.setText(
                            "Tur " + currentRound[0] + " bitti | Takım " + team[0]
                                    + " | Skor: " + teamScore
                                    + " | Doğru: " + roundCorrect[0]
                                    + " | Tabu: " + roundTaboo[0]
                                    + " | Pas: " + roundPass[0]
                    );

                    wait[0] = 10;
                    overlayLabel.setText("TUR BİTTİ (" + wait[0] + ")");

                    waitTimer[0] = new Timer(1000, ev -> {
                        wait[0]--;
                        overlayLabel.setText("TUR BİTTİ (" + wait[0] + ")");
                        if (wait[0] <= 0) goNextRoundNow.run();
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
                if (team[0].equals("A")) scoreA[0]++;
                else scoreB[0]++;
                refreshScores.run();
                showNextCard.run();
            });

            passButton.addActionListener(e -> {
                if (paused[0]) return;
                if (!started[0]) return;
                roundPass[0]++;
                passCount[0]++;
                updatePassLabel.run();
                if (!canPass.getAsBoolean()) passButton.setEnabled(false);
                showNextCard.run();
            });

            tabooButton.addActionListener(e -> {
                if (paused[0]) return;
                if (!started[0]) return;
                roundTaboo[0]++;
                if (team[0].equals("A")) scoreA[0] += tabooPenalty[0];
                else scoreB[0] += tabooPenalty[0];
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

                teamLabel.setText("Takım: " + team[0]);
                roundLabel.setText("Tur: " + currentRound[0] + "/" + (int) roundsBox.getSelectedItem());

                timeLeft[0] = roundDuration[0];
                timeLabel.setText("Süre: " + timeLeft[0]);
                infoLabel.setText("Ayarları seçip BAŞLAT'a bas.");

                passCount[0] = 0;
                updatePassLabel.run();

                resetRoundStats.run();

                // Clear recent history for new game
                recentHistory.clear();
                roundUsed.clear();
                prevRoundShown.clear();
                drawsThisRound[0] = 0;

                shuffleDeck.run();
                showNextCard.run();
                refreshScores.run();

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
                    if (overlayPanel.isVisible() || gameEndOverlay.isVisible()) return;
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

            // =========================
            // PANELE EKLE
            // =========================
            frame.add(topPanel, BorderLayout.NORTH);
            frame.add(centerPanel, BorderLayout.CENTER);
            frame.add(bottomPanel, BorderLayout.SOUTH);

            refreshScores.run();
            resetRoundStats.run();
            recentHistory.clear();
            roundUsed.clear();
            prevRoundShown.clear();
            drawsThisRound[0] = 0;
            // Start'a basılana kadar oyun başlamasın
            correctButton.setEnabled(false);
            passButton.setEnabled(false);
            tabooButton.setEnabled(false);
            infoLabel.setText("Ayarları seçip BAŞLAT'a bas.");

            wordLabel.setText("BAŞLAT");
            tabooList.setText(toHtml(new String[]{"Ayarları seç", "BAŞLAT'a bas", "SPACE=PAS", "↑=DOĞRU", "↓=TABU"}));

            frame.setVisible(true);
            frame.getRootPane().requestFocusInWindow();
        });
    }

    // =========================
    // UI HELPERS (Hover + Disabled)
    // =========================
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

    static class WordCard {
        String word;
        String[] taboo;
        WordCard(String w, String[] t) { word = w; taboo = t; }
    }
}