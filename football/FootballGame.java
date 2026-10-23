import javax.sound.sampled.AudioFormat;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Soccer Ball Showdown - 2 player local football.
 * P1 (red):  WASD + hold/release F
 * P2 (blue): Arrows + hold/release ENTER   (or vs Bot via menu)
 */
public class FootballGame extends JPanel implements ActionListener, KeyListener, MouseListener {

    static final int W = 960, H = 640;
    static final int GOAL_W = 150, GOAL_DEPTH = 30;
    static final int GOAL_TOP = H / 2 - GOAL_W / 2;
    static final int GOAL_BOT = H / 2 + GOAL_W / 2;
    static final int WIN_SCORE = 5;

    double bx = W / 2.0, by = H / 2.0, bvx = 0, bvy = 0;
    Player p1, p2;
    int score1 = 0, score2 = 0;
    int shake = 0, flash = 0, tick = 0;
    String lastEvent = "Kick off!";

    enum State { MENU, PLAY, REPLAY, WIN }
    State state = State.MENU;

    // replay
    final List<double[]> snapshots = new ArrayList<>();
    double[][] replayFrames;
    int replayIdx = 0;

    // bot
    boolean botMode = false;
    int botLevel = 1; // 0 easy, 1 medium, 2 hard

    // events
    String activeEvent = null;
    int eventTicksLeft = 0, nextEventTick = 900;
    double windX = 0, windY = 0;

    // particles / trail
    final List<Particle> particles = new CopyOnWriteArrayList<>();
    final List<Ring> rings = new CopyOnWriteArrayList<>();
    final List<double[]> trail = new ArrayList<>();

    boolean fullscreen = false;
    static JFrame frame;

    FootballGame() {
        setPreferredSize(new Dimension(W, H));
        setBackground(new Color(34, 139, 34));
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        p1 = new Player(150, H / 2.0, new Color(220, 60, 60), KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_F);
        p2 = new Player(W - 150, H / 2.0, new Color(60, 110, 230), KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_ENTER);
        new Timer(16, this).start();
    }

    // ---------------- Sound ----------------
    static void beep(int freq, int ms, float vol, int delayMs) {
        new Thread(() -> {
            try {
                Thread.sleep(delayMs);
                int rate = 22050;
                byte[] buf = new byte[rate * ms / 1000];
                for (int i = 0; i < buf.length; i++)
                    buf[i] = (byte) (Math.sin(2 * Math.PI * freq * i / (double) rate) * 127 * vol * (1 - i / (double) buf.length));
                AudioFormat f = new AudioFormat(rate, 8, 1, true, false);
                javax.sound.sampled.Clip c = javax.sound.sampled.AudioSystem.getClip();
                c.open(f, buf, 0, buf.length);
                c.start();
            } catch (Exception ignored) {}
        }).start();
    }
    static void sndKick(double charge) { beep((int)(140 + charge * 160), 120, 0.6f, 0); }
    static void sndGoal() { beep(523, 150, 0.6f, 0); beep(659, 150, 0.6f, 120); beep(784, 300, 0.6f, 240); }
    static void sndWin() { int[] n = {523, 659, 784, 1046, 784, 1046}; for (int i = 0; i < n.length; i++) beep(n[i], 200, 0.6f, i * 160); }
    static void sndEvent() { beep(880, 100, 0.4f, 0); beep(880, 100, 0.4f, 150); }

    // ---------------- Player ----------------
    class Player {
        double x, y, vx = 0, vy = 0;
        final double r = 20;
        double charge = 0;
        boolean charging = false;
        final int up, down, left, right, kick;
        final Color color;
        double mx = 0, my = 0; // movement input (set by AI or human)

        Player(double x, double y, Color c, int up, int down, int left, int right, int kick) {
            this.x = x; this.y = y; this.color = c;
            this.up = up; this.down = down; this.left = left; this.right = right; this.kick = kick;
        }

        boolean hasBall() {
            return Math.hypot(bx - x, by - y) < r + 13 + 40;
        }

        void update() {
            double ax = mx, ay = my;
            if (ax != 0 && ay != 0) { ax *= 0.7071; ay *= 0.7071; }
            vx += ax * 0.7;
            vy += ay * 0.7;
            double sp = Math.hypot(vx, vy), max = 5.2;
            if (sp > max) { vx *= max / sp; vy *= max / sp; }
            vx *= 0.82; vy *= 0.82;
            x += vx; y += vy;
            x = Math.max(r, Math.min(W - r, x));
            y = Math.max(r, Math.min(H - r, y));
            if (charging && hasBall() && charge < 1) charge = Math.min(1, charge + 0.022);
        }
    }

    void humanInput(Player p, java.util.Set<Integer> keys) {
        p.mx = (keys.contains(p.left) ? -1 : 0) + (keys.contains(p.right) ? 1 : 0);
        p.my = (keys.contains(p.up) ? -1 : 0) + (keys.contains(p.down) ? 1 : 0);
    }

    // ---------------- Bot ----------------
    void botInput() {
        double maxSpd = botLevel == 0 ? 3.0 : botLevel == 1 ? 4.3 : 5.2;
        double tx, ty;
        boolean chaseBall = bx > W * 0.35 || Math.abs(bvx) > 3;
        if (chaseBall) {
            // approach ball from behind (opposite of our goal) so we shoot towards goal
            double gx = W, gy = H / 2.0;
            double dx = bx - gx, dy = by - gy;
            double d = Math.max(1, Math.hypot(dx, dy));
            tx = bx + dx / d * 26;
            ty = by + dy / d * 26;
        } else {
            tx = Math.min(bx + 130, W - 80);
            ty = H / 2.0 + (by - H / 2.0) * 0.5;
        }
        double dx = tx - p2.x, dy = ty - p2.y;
        p2.mx = Math.abs(dx) > 10 ? Math.signum(dx) : 0;
        p2.my = Math.abs(dy) > 10 ? Math.signum(dy) : 0;
        // cap energy for easy bot
        double sp = Math.hypot(p2.vx, p2.vy);
        if (sp > maxSpd) { p2.vx *= maxSpd / sp; p2.vy *= maxSpd / sp; }

        // kick logic
        double dBall = Math.hypot(bx - p2.x, by - p2.y);
        if (dBall < p2.r + 13 + 34) {
            if (!p2.charging) { p2.charging = true; p2.charge = 0; }
            double releaseAt = botLevel == 0 ? 0.05 : botLevel == 1 ? 0.5 : 0.9;
            if (p2.charge >= releaseAt) { kick(p2); }
        } else if (p2.charging && botLevel == 0) {
            p2.charging = false; p2.charge = 0;
        }
    }

    // ---------------- Ball / physics ----------------
    void collidePlayers() {
        double dx = p2.x - p1.x, dy = p2.y - p1.y, d = Math.hypot(dx, dy), min = p1.r + p2.r;
        if (d > 0 && d < min) {
            double push = (min - d) / 2;
            p1.x -= dx / d * push; p1.y -= dy / d * push;
            p2.x += dx / d * push; p2.y += dy / d * push;
            double nx = dx / d, ny = dy / d;
            double rel = (p2.vx - p1.vx) * nx + (p2.vy - p1.vy) * ny;
            if (rel < 0) {
                p1.vx += rel * nx; p1.vy += rel * ny;
                p2.vx -= rel * nx; p2.vy -= rel * ny;
            }
        }
    }

    void updateBall() {
        // events
        if (activeEvent != null && activeEvent.equals("VENTO")) { bvx += windX; bvy += windY; }
        bx += bvx; by += bvy;
        double fric = activeEvent != null && activeEvent.equals("LAMA") ? 0.94 :
                activeEvent != null && activeEvent.equals("GELO") ? 0.997 : 0.985;
        bvx *= fric; bvy *= fric;
        if (Math.abs(bvx) < 0.05) bvx = 0;
        if (Math.abs(bvy) < 0.05) bvy = 0;
        double r = 13;

        if (by < r) { by = r; bvy = -bvy * 0.75; }
        if (by > H - r) { by = H - r; bvy = -bvy * 0.75; }
        if (bx < r) {
            if (by > GOAL_TOP + r && by < GOAL_BOT - r) { goal(2); return; }
            bx = r; bvx = -bvx * 0.75;
        }
        if (bx > W - r) {
            if (by > GOAL_TOP + r && by < GOAL_BOT - r) { goal(1); return; }
            bx = W - r; bvx = -bvx * 0.75;
        }
        checkPost(0, GOAL_TOP); checkPost(0, GOAL_BOT);
        checkPost(W, GOAL_TOP); checkPost(W, GOAL_BOT);

        collide(p1); collide(p2);

        if (Math.hypot(bvx, bvy) > 7) {
            trail.add(new double[]{bx, by});
            if (trail.size() > 10) trail.remove(0);
        } else if (!trail.isEmpty()) trail.remove(0);
    }

    void checkPost(double px, double py) {
        double dx = bx - px, dy = by - py, d = Math.hypot(dx, dy), r = 13;
        if (d > 0 && d < r) {
            bx += dx / d * (r - d); by += dy / d * (r - d);
            double dot = bvx * dx / d + bvy * dy / d;
            bvx -= 2 * dot * dx / d; bvy -= 2 * dot * dy / d; bvx *= 0.7; bvy *= 0.7;
        }
    }

    void collide(Player p) {
        double dx = bx - p.x, dy = by - p.y, d = Math.hypot(dx, dy), min = p.r + 13;
        if (d > 0 && d < min) {
            bx += dx / d * (min - d); by += dy / d * (min - d);
            double nx = dx / d, ny = dy / d;
            double rel = (bvx - p.vx) * nx + (bvy - p.vy) * ny;
            if (rel < 0) { bvx -= 1.8 * rel * nx; bvy -= 1.8 * rel * ny; }
        }
    }

    void kick(Player p) {
        double dx = bx - p.x, dy = by - p.y, d = Math.hypot(dx, dy);
        if (d < p.r + 13 + 34) {
            if (d < 1) { dx = (p == p1) ? 1 : -1; dy = 0; d = 1; }
            double power = 7 + p.charge * 17;
            bvx = dx / d * power + p.vx * 0.6;
            bvy = dy / d * power + p.vy * 0.6;
            sndKick(p.charge);
            burst(bx, by, p.charge > 0.55 ? Color.YELLOW : Color.ORANGE, p.charge > 0.55 ? 24 : 10);
            rings.add(new Ring(bx, by, p.charge > 0.55 ? Color.YELLOW : Color.WHITE));
            if (p.charge > 0.55) { lastEvent = "SUPER CHUTE!"; shake = 10; flash = 10; }
            else lastEvent = "Chute!";
        }
        p.charge = 0; p.charging = false;
    }

    void goal(int who) {
        if (who == 1) { score1++; lastEvent = "GOL! Jogador 1 marcou!"; }
        else if (who == 2) { score2++; lastEvent = "GOL! Jogador 2 marcou!"; }
        if (who != 0) { sndGoal(); shake = 14; }
        replayFrames = snapshots.toArray(new double[0][]);
        replayIdx = 0;
        if (who != 0) state = State.REPLAY;
        else resetPositions();
        if (score1 >= WIN_SCORE || score2 >= WIN_SCORE) { state = State.WIN; sndWin(); }
    }

    void resetPositions() {
        p1.x = 150; p1.y = H / 2.0; p1.vx = p1.vy = 0;
        p2.x = W - 150; p2.y = H / 2.0; p2.vx = p2.vy = 0;
        bx = W / 2.0; by = H / 2.0; bvx = 0; bvy = 0;
        p1.charge = p2.charge = 0; p1.charging = p2.charging = false;
        trail.clear();
    }

    // ---------------- Events ----------------
    void updateEvents() {
        if (activeEvent != null) {
            eventTicksLeft--;
            if (eventTicksLeft <= 0) { activeEvent = null; windX = windY = 0; lastEvent = ""; }
        } else if (tick >= nextEventTick) {
            String[] evs = {"VENTO", "LAMA", "GELO"};
            activeEvent = evs[new Random().nextInt(evs.length)];
            eventTicksLeft = 480;
            nextEventTick = tick + 900 + new Random().nextInt(900);
            sndEvent();
            if (activeEvent.equals("VENTO")) {
                double a = Math.random() * 2 * Math.PI;
                windX = Math.cos(a) * 0.09; windY = Math.sin(a) * 0.09;
                lastEvent = "EVENTO: VENTO FORTE!";
            } else if (activeEvent.equals("LAMA")) lastEvent = "EVENTO: LAMA NO CAMPO!";
            else lastEvent = "EVENTO: CAMPO GELADO!";
        }
    }

    // ---------------- Particles ----------------
    static class Particle {
        double x, y, vx, vy; int life; Color c;
        Particle(double x, double y, double vx, double vy, Color c) { this.x = x; this.y = y; this.vx = vx; this.vy = vy; this.life = 30; this.c = c; }
    }
    static class Ring {
        double x, y, r = 10; int life = 20; Color c;
        Ring(double x, double y, Color c) { this.x = x; this.y = y; this.c = c; }
    }
    void burst(double x, double y, Color c, int n) {
        for (int i = 0; i < n; i++) {
            double a = Math.random() * 2 * Math.PI, s = 2 + Math.random() * 5;
            particles.add(new Particle(x, y, Math.cos(a) * s, Math.sin(a) * s, c));
        }
    }
    void updateParticles() {
        particles.forEach(p -> { p.x += p.vx; p.y += p.vy; p.vx *= 0.9; p.vy *= 0.9; p.life--; });
        particles.removeIf(p -> p.life <= 0);
        rings.forEach(r -> { r.r += 4; r.life--; });
        rings.removeIf(r -> r.life <= 0);
    }

    // ---------------- Loop ----------------
    final java.util.Set<Integer> keys = new java.util.HashSet<>();

    @Override
    public void actionPerformed(ActionEvent e) {
        tick++;
        if (state == State.PLAY) {
            humanInput(p1, keys);
            if (botMode) botInput(); else humanInput(p2, keys);
            p1.update(); p2.update();
            collidePlayers();
            updateBall();
            updateEvents();
            snapshots.add(new double[]{p1.x, p1.y, p2.x, p2.y, bx, by});
            if (snapshots.size() > 120) snapshots.remove(0);
        } else if (state == State.REPLAY) {
            replayIdx += 1;
            if (replayIdx >= replayFrames.length && replayIdx > 90) {
                if (state != State.WIN) { resetPositions(); state = State.PLAY; snapshots.clear(); }
            }
            updateParticles();
        }
        if (shake > 0) shake--;
        if (flash > 0) flash--;
        updateParticles();
        repaint();
    }

    // ---------------- Input ----------------
    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        boolean repeat = keys.contains(k);
        keys.add(k);
        if (repeat) return;
        if (state == State.PLAY) {
            if (k == p1.kick && p1.hasBall()) { p1.charging = true; p1.charge = 0; }
            if (!botMode && k == p2.kick && p2.hasBall()) { p2.charging = true; p2.charge = 0; }
        }
        if (k == KeyEvent.VK_R) {
            score1 = 0; score2 = 0; activeEvent = null; state = State.MENU; lastEvent = "Menu";
        }
        if (k == KeyEvent.VK_F11) toggleFullscreen();
        if (k == KeyEvent.VK_ESCAPE && state == State.PLAY) state = State.MENU;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int k = e.getKeyCode();
        keys.remove(k);
        if (state == State.PLAY) {
            if (k == p1.kick && p1.charging) kick(p1);
            if (!botMode && k == p2.kick && p2.charging) kick(p2);
        }
    }

    @Override public void keyTyped(KeyEvent e) {}

    // ---------------- Menu buttons ----------------
    // logical rects
    Rectangle[] menuBtns = {
            new Rectangle(330, 190, 300, 48),   // 2 players
            new Rectangle(330, 255, 300, 48),   // bot easy
            new Rectangle(330, 320, 300, 48),   // bot medium
            new Rectangle(330, 385, 300, 48),   // bot hard
            new Rectangle(330, 450, 300, 48),   // fullscreen
    };
    String[] menuLabels() {
        return new String[]{
                "2 Jogadores (mesmo teclado)",
                "Contra Bot - Fácil",
                "Contra Bot - Médio",
                "Contra Bot - Difícil",
                fullscreen ? "Tela cheia: LIGADA" : "Tela cheia: DESLIGADA"
        };
    }

    void toggleFullscreen() {
        fullscreen = !fullscreen;
        frame.setExtendedState(fullscreen ? JFrame.MAXIMIZED_BOTH : JFrame.NORMAL);
        frame.setVisible(true);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        double s = scale();
        double lx = (e.getX() - offsetX()) / s;
        double ly = (e.getY() - offsetY()) / s;
        if (state == State.WIN) { state = State.MENU; return; }
        if (state != State.MENU) return;
        String[] labels = menuLabels();
        for (int i = 0; i < menuBtns.length; i++) {
            if (menuBtns[i].contains(lx, ly)) {
                if (i == 0) { botMode = false; startMatch(); }
                else if (i == 1) { botMode = true; botLevel = 0; startMatch(); }
                else if (i == 2) { botMode = true; botLevel = 1; startMatch(); }
                else if (i == 3) { botMode = true; botLevel = 2; startMatch(); }
                else toggleFullscreen();
            }
        }
    }

    void startMatch() {
        score1 = 0; score2 = 0; activeEvent = null; eventTicksLeft = 0;
        nextEventTick = tick + 700; snapshots.clear();
        resetPositions();
        state = State.PLAY; lastEvent = "Kick off!";
    }

    double scale() { return Math.min(getWidth() / (double) W, getHeight() / (double) H); }
    double offsetX() { return (getWidth() - W * scale()) / 2; }
    double offsetY() { return (getHeight() - H * scale()) / 2; }

    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    // ---------------- Drawing ----------------
    void drawBall(Graphics2D g, double x, double y, double r, Color base, Color spot) {
        g.setColor(base);
        g.fillOval((int)(x - r), (int)(y - r), (int)(2 * r), (int)(2 * r));
        g.setColor(spot);
        int[] px = new int[5], py = new int[5];
        for (int i = 0; i < 5; i++) {
            double a = Math.PI / 2 + i * 2 * Math.PI / 5;
            px[i] = (int)(x + r * 0.35 * Math.cos(a));
            py[i] = (int)(y - r * 0.35 * Math.sin(a));
        }
        g.fillPolygon(px, py, 5);
        for (int i = 0; i < 5; i++) {
            double a = Math.PI / 2 + i * 2 * Math.PI / 5 + Math.PI / 5;
            g.fillOval((int)(x + r * 0.8 * Math.cos(a)) - 3, (int)(y - r * 0.8 * Math.sin(a)) - 3, 6, 6);
        }
        g.setColor(Color.DARK_GRAY);
        g.setStroke(new BasicStroke(2));
        g.drawOval((int)(x - r), (int)(y - r), (int)(2 * r), (int)(2 * r));
    }

    void drawField(Graphics2D g) {
        for (int i = 0; i < 6; i++) {
            g.setColor(i % 2 == 0 ? new Color(44, 160, 44) : new Color(34, 139, 34));
            g.fillRect(i * W / 6, 0, W / 6, H);
        }
        // event tints
        if (activeEvent != null) {
            if (activeEvent.equals("LAMA")) {
                g.setColor(new Color(120, 80, 30, 70)); g.fillRect(0, 0, W, H);
            } else if (activeEvent.equals("GELO")) {
                g.setColor(new Color(150, 220, 255, 60)); g.fillRect(0, 0, W, H);
            }
        }
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(3));
        g.drawRect(8, 8, W - 16, H - 16);
        g.drawLine(W / 2, 8, W / 2, H - 8);
        g.drawOval(W / 2 - 70, H / 2 - 70, 140, 140);
        g.drawRect(8, H / 2 - 110, 90, 220);
        g.drawRect(W - 98, H / 2 - 110, 90, 220);
        g.setColor(Color.WHITE);
        g.fillRect(0, GOAL_TOP, GOAL_DEPTH, GOAL_W);
        g.fillRect(W - GOAL_DEPTH, GOAL_TOP, GOAL_DEPTH, GOAL_W);
        g.setColor(Color.LIGHT_GRAY);
        for (int y = GOAL_TOP; y <= GOAL_BOT; y += 15) {
            g.drawLine(0, y, GOAL_DEPTH, y);
            g.drawLine(W - GOAL_DEPTH, y, W, y);
        }
    }

    void drawCharge(Graphics2D g, Player p) {
        if (!p.charging) return;
        int w = 44, h = 8, x = (int)p.x - w / 2, y = (int)p.y - (int)p.r - 18;
        g.setColor(Color.DARK_GRAY); g.fillRect(x, y, w, h);
        g.setColor(p.charge > 0.55 ? Color.YELLOW : new Color(120, 220, 120));
        g.fillRect(x, y, (int)(w * p.charge), h);
        g.setColor(Color.WHITE); g.drawRect(x, y, w, h);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        double s = scale();
        g.translate(offsetX(), offsetY());
        g.scale(s, s);
        if (shake > 0) g.translate((Math.random() - 0.5) * 12, (Math.random() - 0.5) * 12);

        if (state == State.MENU) { drawMenu(g); return; }
        if (state == State.WIN) { drawWin(g); return; }

        drawField(g);

        if (state == State.REPLAY) {
            drawReplay(g);
            return;
        }

        // trail
        for (int i = 0; i < trail.size(); i++) {
            double[] t = trail.get(i);
            g.setColor(new Color(255, 255, 255, 30 + i * 15));
            g.fillOval((int)t[0] - 8, (int)t[1] - 8, 16, 16);
        }
        if (flash > 0) {
            g.setColor(new Color(255, 255, 120, flash * 20));
            g.fillOval((int)bx - 40, (int)by - 40, 80, 80);
        }
        for (Ring r : rings) {
            g.setColor(new Color(r.c.getRed(), r.c.getGreen(), r.c.getBlue(), Math.max(0, r.life * 10)));
            g.setStroke(new BasicStroke(4));
            g.drawOval((int)(r.x - r.r), (int)(r.y - r.r), (int)(2 * r.r), (int)(2 * r.r));
        }
        for (Particle p : particles) {
            g.setColor(p.c);
            g.fillOval((int)p.x - 3, (int)p.y - 3, 6, 6);
        }

        drawBall(g, bx, by, 13, Color.WHITE, Color.BLACK);
        drawBall(g, p1.x, p1.y, p1.r, p1.color, new Color(120, 0, 0));
        drawBall(g, p2.x, p2.y, p2.r, p2.color, new Color(0, 20, 130));
        drawCharge(g, p1); drawCharge(g, p2);

        // HUD
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 40));
        g.drawString(score1 + "  -  " + score2, W / 2 - 70, 55);
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        String ev = activeEvent != null ? "Evento: " + activeEvent : lastEvent;
        g.drawString(ev, W / 2 - g.getFontMetrics().stringWidth(ev) / 2, H - 50);
        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g.drawString("P1: WASD + segurar/soltar F   |   P2: Setas + segurar/soltar ENTER   |   R: menu   |   F11: tela cheia", 40, H - 20);
    }

    void drawReplay(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, W, H);
        int rw = 560, rh = 373, rx = (W - rw) / 2, ry = (H - rh) / 2;
        g.setColor(new Color(20, 80, 20));
        g.fillRect(rx, ry, rw, rh);
        g.setColor(Color.WHITE);
        g.drawRect(rx, ry, rw, rh);
        if (replayFrames != null && replayFrames.length > 0) {
            double[] fr = replayFrames[Math.min(replayIdx, replayFrames.length - 1)];
            double sx = rw / (double) W, sy = rh / (double) H;
            drawBall(g, rx + fr[0] * sx, ry + fr[1] * sy, p1.r * sx, p1.color, new Color(120, 0, 0));
            drawBall(g, rx + fr[2] * sx, ry + fr[3] * sy, p2.r * sx, p2.color, new Color(0, 20, 130));
            drawBall(g, rx + fr[4] * sx, ry + fr[5] * sy, 13 * sx, Color.WHITE, Color.BLACK);
        }
        g.setColor(Color.YELLOW);
        g.setFont(new Font("SansSerif", Font.BOLD, 26));
        g.drawString("REPLAY DO GOL!", rx + 10, ry - 12);
    }

    void drawMenu(Graphics2D g) {
        drawField(g);
        g.setColor(new Color(0, 0, 0, 140));
        g.fillRect(0, 0, W, H);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 52));
        g.drawString("SOCCER BALL SHOWDOWN", 180, 120);
        String[] labels = menuLabels();
        for (int i = 0; i < menuBtns.length; i++) {
            Rectangle r = menuBtns[i];
            g.setColor(new Color(255, 255, 255, 30));
            g.fillRoundRect(r.x, r.y, r.width, r.height, 12, 12);
            g.setColor(Color.WHITE);
            g.drawRoundRect(r.x, r.y, r.width, r.height, 12, 12);
            g.setFont(new Font("SansSerif", Font.BOLD, 20));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(labels[i], r.x + (r.width - fm.stringWidth(labels[i])) / 2, r.y + 32);
        }
        g.setFont(new Font("SansSerif", Font.PLAIN, 15));
        g.drawString("Segure o botão de chute perto da bola para carregar o SUPER CHUTE!", 190, 550);
        g.drawString("Primeiro a " + WIN_SCORE + " vence. F11 = tela cheia.", 330, 580);
    }

    void drawWin(Graphics2D g) {
        drawField(g);
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, 0, W, H);
        g.setColor(Color.YELLOW);
        g.setFont(new Font("SansSerif", Font.BOLD, 48));
        String w = score1 > score2 ? "JOGADOR 1 VENCEU!" : "JOGADOR 2 VENCEU!";
        g.drawString(w, W / 2 - g.getFontMetrics().stringWidth(w) / 2, H / 2 - 20);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.PLAIN, 22));
        String sub = botMode ? (score1 > score2 ? "Você venceu o bot!" : "O bot venceu!") : "Placar final: " + score1 + " - " + score2;
        g.drawString(sub, W / 2 - g.getFontMetrics().stringWidth(sub) / 2, H / 2 + 25);
        g.drawString("Clique ou pressione R para voltar ao menu", W / 2 - 210, H / 2 + 80);
    }

    public static void main(String[] args) {
        frame = new JFrame("Soccer Ball Showdown");
        FootballGame game = new FootballGame();
        frame.add(game);
        frame.pack();
        frame.setResizable(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setExtendedState(JFrame.NORMAL);
        frame.setVisible(true);
    }
}
