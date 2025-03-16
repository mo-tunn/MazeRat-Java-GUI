import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.Timer;
import java.awt.image.BufferedImage;
import java.util.List;

public class MazeGame extends JPanel {
    private static final int BOYUT = 15;
    private static final int HUCRE = 40;
    private int[][] labirent = new int[BOYUT][BOYUT];
    private int fareX = 0, fareY = 0;
    private int cikisX = BOYUT - 1, cikisY = BOYUT - 1;
    private BufferedImage fareGorsel, cikisGorsel, duvarGorsel, arkaPlanGorsel;
    private boolean[][] ziyaretEdildi;
    private Stack<Point> yolStack;
    private JButton baslatButonu, yenidenOynaButonu;
    private JLabel sureLabel;
    private boolean oyunBasladi = false;
    private Timer sureTimer;
    private int gecenSure;

    public MazeGame() {
        setPreferredSize(new Dimension(BOYUT * HUCRE, BOYUT * HUCRE));
        yukleGorseller();
        labirentiOlustur();
        ziyaretEdildi = new boolean[BOYUT][BOYUT];
        yolStack = new Stack<>();

        baslatButonu = new JButton("Başlat");
        yenidenOynaButonu = new JButton("Yeniden Oyna");
        sureLabel = new JLabel("Süre: 0 sn");

        baslatButonu.addActionListener(e -> {
            if (!oyunBasladi) {
                oyunBasladi = true;
                gecenSure = 0;
                sureLabel.setText("Süre: 0 sn");
                sureTimer.start();
                new Thread(() -> dfs(fareX, fareY)).start();
            }
        });

        yenidenOynaButonu.addActionListener(e -> {
            oyunBasladi = false;
            labirentiOlustur();
            ziyaretEdildi = new boolean[BOYUT][BOYUT];
            yolStack.clear();
            fareX = 0;
            fareY = 0;
            sureTimer.stop();
            gecenSure = 0;
            sureLabel.setText("Süre: 0 sn");
            repaint();
        });

        sureTimer = new Timer(1000, e -> {
            gecenSure++;
            sureLabel.setText("Süre: " + gecenSure + " sn");
        });

        JPanel panel = new JPanel();
        panel.add(baslatButonu);
        panel.add(yenidenOynaButonu);
        panel.add(sureLabel);

        JFrame frame = new JFrame("MazeRat");
        frame.setLayout(new BorderLayout());
        frame.add(this, BorderLayout.CENTER);
        frame.add(panel, BorderLayout.SOUTH);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false); //
        frame.setVisible(true);

    }

    private void yukleGorseller() {
        try {
            fareGorsel = ImageIO.read(new File("src\\assets\\rato.gif"));
            cikisGorsel = ImageIO.read(new File("src\\assets\\exitsign.png"));
            duvarGorsel = ImageIO.read(new File("src\\assets\\black.png"));
            arkaPlanGorsel = ImageIO.read(new File("src\\assets\\wall.png"));
        } catch (Exception e) {
            System.out.println("Görseller yüklenemedi: " + e.getMessage());
        }
    }

    private void labirentiOlustur() {
        for (int y = 0; y < BOYUT; y++) {
            for (int x = 0; x < BOYUT; x++) {
                labirent[y][x] = 1;
            }
        }

        Random rand = new Random();
        Stack<Point> stack = new Stack<>();
        stack.push(new Point(0, 0));
        labirent[0][0] = 0;

        while (!stack.isEmpty()) {
            Point current = stack.peek();
            int x = current.x, y = current.y;

            List<Point> komsular = new ArrayList<>();
            if (x > 1 && labirent[y][x - 2] == 1) komsular.add(new Point(x - 2, y));
            if (x < BOYUT - 2 && labirent[y][x + 2] == 1) komsular.add(new Point(x + 2, y));
            if (y > 1 && labirent[y - 2][x] == 1) komsular.add(new Point(x, y - 2));
            if (y < BOYUT - 2 && labirent[y + 2][x] == 1) komsular.add(new Point(x, y + 2));

            if (!komsular.isEmpty()) {
                Point secilen = komsular.get(rand.nextInt(komsular.size()));
                int nx = secilen.x, ny = secilen.y;

                labirent[ny][nx] = 0;
                labirent[(y + ny) / 2][(x + nx) / 2] = 0;
                stack.push(secilen);
            } else {
                stack.pop();
            }
        }
    }

    private boolean dfs(int x, int y) {
        if (x == cikisX && y == cikisY) {
            sureTimer.stop();
            JOptionPane.showMessageDialog(null, "Fare çıkışı " + gecenSure + " saniyede buldu!", "Oyun Bitti", JOptionPane.INFORMATION_MESSAGE);
            return true;
        }
        if (x < 0 || y < 0 || x >= BOYUT || y >= BOYUT || labirent[y][x] != 0 || ziyaretEdildi[y][x]) return false;

        ziyaretEdildi[y][x] = true;
        yolStack.push(new Point(x, y));
        fareX = x;
        fareY = y;
        repaint();
        try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }

        int[] dx = {0, 0, 1, -1};
        int[] dy = {1, -1, 0, 0};
        List<Integer> yonler = Arrays.asList(0, 1, 2, 3);
        Collections.shuffle(yonler);

        for (int i : yonler) {
            int nx = x + dx[i], ny = y + dy[i];

            if (dfs(nx, ny)) return true;
        }

        yolStack.pop();
        if (!yolStack.isEmpty()) {
            Point onceki = yolStack.peek();
            fareX = onceki.x;
            fareY = onceki.y;
            repaint();
            try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
        }

        ziyaretEdildi[y][x] = false;
        return false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (arkaPlanGorsel != null) {
            g.drawImage(arkaPlanGorsel, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        for (int y = 0; y < BOYUT; y++) {
            for (int x = 0; x < BOYUT; x++) {
                if (labirent[y][x] == 1) {
                    if (duvarGorsel != null) {
                        g.drawImage(duvarGorsel, x * HUCRE, y * HUCRE, HUCRE, HUCRE, this);
                    } else {
                        g.setColor(Color.BLACK);
                        g.fillRect(x * HUCRE, y * HUCRE, HUCRE, HUCRE);
                    }
                }
            }
        }

        g.drawImage(fareGorsel, fareX * HUCRE, fareY * HUCRE, HUCRE, HUCRE, this);
        g.drawImage(cikisGorsel, cikisX * HUCRE, cikisY * HUCRE, HUCRE, HUCRE, this);
    }

    public static void main(String[] args) {
        new MazeGame();
    }
}
