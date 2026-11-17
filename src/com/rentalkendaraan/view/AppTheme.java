package com.rentalkendaraan.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * Kelas utilitas tema visual modern untuk seluruh aplikasi.
 * Menyediakan palet warna, komponen kustom, dan helper styling.
 */
public final class AppTheme {

    // =========================================================
    // PALET WARNA
    // =========================================================

    /** Background utama - Soft Off-White/Lavender Tint */
    public static final Color BG_MAIN        = new Color(0xF6F5FA);
    /** Header / NavBar - Deep Soft Lavender */
    public static final Color HEADER         = new Color(0x6C63FF);
    /** Header hover */
    public static final Color HEADER_HOVER   = new Color(0x5A52E0);
    /** Card / panel konten */
    public static final Color CARD_BG        = Color.WHITE;
    /** Sage Green - Tersedia / Sukses */
    public static final Color SAGE_GREEN     = new Color(0x4CAF82);
    /** Coral/Pink - Hapus / Disewa */
    public static final Color CORAL          = new Color(0xFF7A8A);
    /** Amber - Ubah */
    public static final Color AMBER          = new Color(0xFFB74D);
    /** Teks judul gelap */
    public static final Color TEXT_DARK      = new Color(0x2D2B55);
    /** Teks sekunder/abu */
    public static final Color TEXT_MUTED     = new Color(0x8B8FA8);
    /** Border halus */
    public static final Color BORDER_SUBTLE  = new Color(0xE8E5F4);
    /** Baris tabel zebra */
    public static final Color ROW_ALT        = new Color(0xF2F0FF);
    /** Baris tabel dipilih */
    public static final Color ROW_SELECTED   = new Color(0xD4D0FF);
    /** Field focus border */
    public static final Color FOCUS_BORDER   = new Color(0x6C63FF);

    // =========================================================
    // FONT
    // =========================================================
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BOLD    = new Font("Segoe UI", Font.BOLD, 13);

    private AppTheme() {}

    // =========================================================
    // BRAND BADGE (Vector Car Icon)
    // =========================================================

    /**
     * Komponen logo vektor mobil modern dengan sudut melengkung.
     * Menggantikan emoji teks sistem agar tampilan tajam dan estetik di semua resolusi.
     */
    public static class CarBrandBadge extends JComponent {
        private static final long serialVersionUID = 1L;
        private final int size;
        private final Color badgeBg;
        private final Color carColor;

        public CarBrandBadge(int size, Color badgeBg, Color carColor) {
            this.size = size;
            this.badgeBg = badgeBg;
            this.carColor = carColor;
            setPreferredSize(new Dimension(size, size));
            setMinimumSize(new Dimension(size, size));
            setMaximumSize(new Dimension(size, size));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            int w = getWidth();
            int h = getHeight();

            // Background badge melengkung
            if (badgeBg != null) {
                g2.setColor(badgeBg);
                int arc = (int) (size * 0.30);
                g2.fillRoundRect(0, 0, w, h, arc, arc);
            }

            // Gambar vektor mobil proporsional di tengah
            double s = size / 32.0;
            g2.translate((w - 32 * s) / 2.0, (h - 32 * s) / 2.0);
            g2.scale(s, s);

            g2.setColor(carColor);

            // Bodi utama & atap mobil menggunakan Path2D
            java.awt.geom.Path2D.Double body = new java.awt.geom.Path2D.Double();
            // Bumper depan
            body.moveTo(4, 18.5);
            body.lineTo(4, 15.5);
            // Kap mesin (hood)
            body.curveTo(4.2, 14.5, 5.5, 14.0, 7.5, 14.0);
            body.lineTo(9.5, 14.0);
            // Kaca depan (windshield)
            body.curveTo(10.5, 14.0, 11.5, 12.5, 13.0, 9.8);
            body.curveTo(13.5, 9.0, 14.2, 8.5, 15.2, 8.5);
            // Atap (roof)
            body.lineTo(21.0, 8.5);
            // Kaca belakang
            body.curveTo(22.2, 8.5, 23.0, 9.2, 23.8, 10.5);
            body.lineTo(25.5, 14.0);
            // Bagasi & belakang (trunk)
            body.curveTo(26.5, 14.2, 27.5, 14.6, 28.0, 15.5);
            body.lineTo(28.0, 18.5);
            // Bawah bodi belakang
            body.lineTo(24.5, 18.5);
            // Celah roda belakang
            body.curveTo(24.5, 16.5, 20.5, 16.5, 20.5, 18.5);
            // Lantai tengah bodi
            body.lineTo(11.5, 18.5);
            // Celah roda depan
            body.curveTo(11.5, 16.5, 7.5, 16.5, 7.5, 18.5);
            body.lineTo(4, 18.5);
            body.closePath();
            g2.fill(body);

            // Jendela mobil (cutout halus)
            Color winColor = (badgeBg != null) ? badgeBg : Color.WHITE;
            g2.setColor(winColor);

            // Jendela depan
            java.awt.geom.Path2D.Double winFront = new java.awt.geom.Path2D.Double();
            winFront.moveTo(13.2, 10.0);
            winFront.lineTo(16.5, 10.0);
            winFront.lineTo(16.5, 13.2);
            winFront.lineTo(10.8, 13.2);
            winFront.closePath();
            g2.fill(winFront);

            // Jendela belakang
            java.awt.geom.Path2D.Double winRear = new java.awt.geom.Path2D.Double();
            winRear.moveTo(17.8, 10.0);
            winRear.lineTo(20.5, 10.0);
            winRear.lineTo(22.8, 13.2);
            winRear.lineTo(17.8, 13.2);
            winRear.closePath();
            g2.fill(winRear);

            // Roda mobil (lingkaran)
            g2.setColor(carColor);
            g2.fill(new java.awt.geom.Ellipse2D.Double(7.5, 16.5, 4.0, 4.0));
            g2.fill(new java.awt.geom.Ellipse2D.Double(20.5, 16.5, 4.0, 4.0));

            // Velg tengah roda (dot)
            g2.setColor(winColor);
            g2.fill(new java.awt.geom.Ellipse2D.Double(8.7, 17.7, 1.6, 1.6));
            g2.fill(new java.awt.geom.Ellipse2D.Double(21.7, 17.7, 1.6, 1.6));

            g2.dispose();
        }
    }

    // =========================================================
    // ROUNDED BORDER
    // =========================================================

    /** Border melengkung dengan bayangan halus */
    public static class RoundedBorder extends AbstractBorder {
        private static final long serialVersionUID = 1L;
        private final int radius;
        private final Color borderColor;
        private final int thickness;

        public RoundedBorder(int radius, Color borderColor, int thickness) {
            this.radius = radius;
            this.borderColor = borderColor;
            this.thickness = thickness;
        }

        @Override
        public void paintBorder(java.awt.Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x + 1, y + 1, w - 2, h - 2, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(java.awt.Component c) {
            return new Insets(thickness + 4, thickness + 10, thickness + 4, thickness + 10);
        }

        @Override
        public Insets getBorderInsets(java.awt.Component c, Insets insets) {
            insets.left = insets.right = thickness + 10;
            insets.top = insets.bottom = thickness + 4;
            return insets;
        }
    }

    // =========================================================
    // SHADOW PANEL (card dengan drop shadow)
    // =========================================================

    /** Panel kartu putih dengan sudut melengkung dan drop shadow */
    public static class CardPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private final int arcSize;
        private final int shadowSize;

        public CardPanel(int arcSize, int shadowSize) {
            this.arcSize = arcSize;
            this.shadowSize = shadowSize;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(
                    shadowSize, shadowSize, shadowSize + 2, shadowSize + 2));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int x = shadowSize;
            int y = shadowSize;
            int w = getWidth() - shadowSize * 2 - 2;
            int h = getHeight() - shadowSize * 2 - 2;

            // drop shadow bertingkat
            for (int i = shadowSize; i > 0; i--) {
                float alpha = 0.04f * (shadowSize - i + 1);
                g2.setColor(new Color(0, 0, 0, Math.min(255, (int)(alpha * 255))));
                g2.fillRoundRect(x + i, y + i, w, h, arcSize + i, arcSize + i);
            }

            // background putih kartu
            g2.setColor(CARD_BG);
            g2.fillRoundRect(x, y, w, h, arcSize, arcSize);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // =========================================================
    // MODERN BUTTON
    // =========================================================

    /**
     * Tombol kustom dengan sudut melengkung, hover animasi,
     * dan kursor tangan.
     */
    public static class ModernButton extends JButton {
        private static final long serialVersionUID = 1L;

        private final Color baseColor;
        private final Color hoverColor;
        private final Color pressColor;
        private Color currentColor;
        private float hoverProgress = 0f;
        private Timer hoverTimer;

        public ModernButton(String text, Color base, Color hover) {
            super(text);
            this.baseColor = base;
            this.hoverColor = hover;
            this.pressColor = hover.darker();
            this.currentColor = base;

            setForeground(Color.WHITE);
            setFont(FONT_BOLD);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setOpaque(false);
            setPreferredSize(new Dimension(getPreferredSize().width + 24,
                    Math.max(36, getPreferredSize().height)));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (isEnabled()) animateHover(true);
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    if (isEnabled()) animateHover(false);
                }
                @Override
                public void mousePressed(MouseEvent e) {
                    if (isEnabled()) {
                        currentColor = pressColor;
                        repaint();
                    }
                }
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (isEnabled()) {
                        currentColor = hoverColor;
                        repaint();
                    }
                }
            });
        }

        private void animateHover(boolean entering) {
            if (hoverTimer != null && hoverTimer.isRunning()) {
                hoverTimer.stop();
            }
            hoverTimer = new Timer(12, e -> {
                if (entering) {
                    hoverProgress = Math.min(1f, hoverProgress + 0.12f);
                } else {
                    hoverProgress = Math.max(0f, hoverProgress - 0.12f);
                }
                currentColor = interpolateColor(baseColor, hoverColor, hoverProgress);
                repaint();
                if ((entering && hoverProgress >= 1f) || (!entering && hoverProgress <= 0f)) {
                    ((Timer) e.getSource()).stop();
                }
            });
            hoverTimer.start();
        }

        private Color interpolateColor(Color c1, Color c2, float t) {
            int r = (int) (c1.getRed()   + t * (c2.getRed()   - c1.getRed()));
            int g = (int) (c1.getGreen() + t * (c2.getGreen() - c1.getGreen()));
            int b = (int) (c1.getBlue()  + t * (c2.getBlue()  - c1.getBlue()));
            return new Color(
                    Math.max(0, Math.min(255, r)),
                    Math.max(0, Math.min(255, g)),
                    Math.max(0, Math.min(255, b)));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (!isEnabled()) {
                g2.setColor(new Color(0xE2E0EE));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(0xA5A1BA));
            } else {
                g2.setColor(currentColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(getForeground());
            }

            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int tx = (getWidth() - fm.stringWidth(getText())) / 2;
            int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(getText(), tx, ty);

            g2.dispose();
        }
    }

    // =========================================================
    // GHOST BUTTON (outline style)
    // =========================================================

    public static class GhostButton extends JButton {
        private static final long serialVersionUID = 1L;

        private final Color borderColor;
        private Color currentBg;
        private final Color hoverBg;

        public GhostButton(String text, Color borderColor) {
            super(text);
            this.borderColor = borderColor;
            this.hoverBg = new Color(borderColor.getRed(), borderColor.getGreen(),
                    borderColor.getBlue(), 25);
            this.currentBg = new Color(0, 0, 0, 0);

            setForeground(borderColor);
            setFont(FONT_BOLD);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setOpaque(false);
            setPreferredSize(new Dimension(getPreferredSize().width + 20, 36));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { if (isEnabled()) { currentBg = hoverBg; repaint(); } }
                @Override public void mouseExited(MouseEvent e) { if (isEnabled()) { currentBg = new Color(0,0,0,0); repaint(); } }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (!isEnabled()) {
                g2.setColor(new Color(0xE8E6F0));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 10, 10);
                g2.setColor(new Color(0xB5B2C5));
            } else {
                g2.setColor(currentBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 10, 10);
                g2.setColor(getForeground());
            }

            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int tx = (getWidth() - fm.stringWidth(getText())) / 2;
            int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(getText(), tx, ty);
            g2.dispose();
        }
    }

    // =========================================================
    // MODERN TEXT FIELD
    // =========================================================

    /** TextField dengan border melengkung dan efek fokus */
    public static class ModernTextField extends JTextField {
        private static final long serialVersionUID = 1L;
        private boolean focused = false;

        public ModernTextField() {
            super();
            init();
        }

        public ModernTextField(int cols) {
            super(cols);
            init();
        }

        private void init() {
            setOpaque(false);
            setFont(FONT_BODY);
            setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            setBackground(new Color(0xF8F7FE));
            addFocusListener(new java.awt.event.FocusAdapter() {
                @Override public void focusGained(java.awt.event.FocusEvent e) { focused = true; repaint(); }
                @Override public void focusLost(java.awt.event.FocusEvent e) { focused = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isEnabled() ? getBackground() : new Color(0xF0EFF8));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.setColor(focused ? FOCUS_BORDER : BORDER_SUBTLE);
            g2.setStroke(new BasicStroke(focused ? 2f : 1.5f));
            g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        public boolean isOpaque() { return false; }
    }

    // =========================================================
    // STATUS BADGE
    // =========================================================

    /** Badge status berwarna (Tersedia / Disewa) */
    public static class StatusBadge extends JLabel {
        private static final long serialVersionUID = 1L;
        private Color bgColor;

        public StatusBadge(String text, Color bgColor) {
            super(text, CENTER);
            this.bgColor = bgColor;
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // =========================================================
    // HELPER METHODS
    // =========================================================

    /** Terapkan styling modern pada JTable */
    public static void styleTable(JTable table) {
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(CARD_BG);
        table.setFont(FONT_BODY);
        table.setForeground(TEXT_DARK);
        table.setSelectionBackground(ROW_SELECTED);
        table.setSelectionForeground(TEXT_DARK);
        table.setFocusable(true);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setReorderingAllowed(false);

        // Zebra striping renderer
        DefaultTableCellRenderer zebraRenderer = new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public java.awt.Component getTableCellRendererComponent(
                    JTable tbl, Object value, boolean selected, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(tbl, value, selected, focus, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                setFont(FONT_BODY);
                if (selected) {
                    setBackground(ROW_SELECTED);
                    setForeground(TEXT_DARK);
                } else {
                    setBackground(row % 2 == 0 ? CARD_BG : ROW_ALT);
                    setForeground(TEXT_DARK);
                }
                return this;
            }
        };

        table.setDefaultRenderer(Object.class, zebraRenderer);
        table.setDefaultRenderer(Number.class, zebraRenderer);

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public java.awt.Component getTableCellRendererComponent(
                    JTable tbl, Object value, boolean selected, boolean focus, int row, int col) {
                JLabel lbl = new JLabel(value == null ? "" : value.toString());
                lbl.setBackground(new Color(0xEDE9FF));
                lbl.setForeground(HEADER);
                lbl.setFont(FONT_BOLD);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER_SUBTLE),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)));
                lbl.setOpaque(true);
                return lbl;
            }
        });
        header.setBackground(new Color(0xEDE9FF));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER_SUBTLE));
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
    }

    /** Style JComboBox */
    public static void styleComboBox(JComboBox<?> combo) {
        combo.setFont(FONT_BODY);
        combo.setBackground(new Color(0xF8F7FE));
        combo.setForeground(TEXT_DARK);
        combo.setMaximumRowCount(8);
        combo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        combo.setBorder(new RoundedBorder(10, BORDER_SUBTLE, 1));
    }

    /** Style JSpinner */
    public static void styleSpinner(JSpinner spinner) {
        spinner.setFont(FONT_BODY);
        spinner.setBorder(new RoundedBorder(10, BORDER_SUBTLE, 1));
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setFont(FONT_BODY);
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField()
                .setBackground(new Color(0xF8F7FE));
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setBorder(
                BorderFactory.createEmptyBorder(4, 8, 4, 8));
    }

    /** Label form kecil */
    public static JLabel makeFormLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(TEXT_MUTED);
        return lbl;
    }

    /** Separator tipis */
    public static javax.swing.JSeparator makeSeparator() {
        javax.swing.JSeparator sep = new javax.swing.JSeparator();
        sep.setForeground(BORDER_SUBTLE);
        return sep;
    }

    /** Styled ScrollPane tanpa border */
    public static javax.swing.JScrollPane makeScrollPane(JComponent view) {
        javax.swing.JScrollPane sp = new javax.swing.JScrollPane(view,
                javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setBackground(CARD_BG);
        sp.getViewport().setBackground(CARD_BG);
        sp.getVerticalScrollBar().setUnitIncrement(16);

        // Style vertical scrollbar
        sp.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                this.thumbColor = new Color(0xC4BFF5);
                this.trackColor = new Color(0xF0EFF8);
            }
            @Override protected JButton createDecreaseButton(int o) {
                return makeZeroButton();
            }
            @Override protected JButton createIncreaseButton(int o) {
                return makeZeroButton();
            }
        });

        // Style horizontal scrollbar
        sp.getHorizontalScrollBar().setUnitIncrement(16);
        sp.getHorizontalScrollBar().setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                this.thumbColor = new Color(0xC4BFF5);
                this.trackColor = new Color(0xF0EFF8);
            }
            @Override protected JButton createDecreaseButton(int o) {
                return makeZeroButton();
            }
            @Override protected JButton createIncreaseButton(int o) {
                return makeZeroButton();
            }
        });
        return sp;
    }

    private static JButton makeZeroButton() {
        JButton b = new JButton();
        b.setPreferredSize(new Dimension(0, 0));
        b.setMinimumSize(new Dimension(0, 0));
        b.setMaximumSize(new Dimension(0, 0));
        return b;
    }

    // =========================================================
    // PULSE DOT (berkedip)
    // =========================================================

    /** Indikator titik hijau berkedip untuk status koneksi */
    public static class PulseDot extends JPanel {
        private static final long serialVersionUID = 1L;
        private float alpha = 1f;
        private boolean fading = false;
        private final Timer pulseTimer;

        public PulseDot() {
            setOpaque(false);
            setPreferredSize(new Dimension(10, 10));

            pulseTimer = new Timer(40, e -> {
                if (!fading) {
                    alpha -= 0.05f;
                    if (alpha <= 0.3f) fading = true;
                } else {
                    alpha += 0.05f;
                    if (alpha >= 1f) { alpha = 1f; fading = false; }
                }
                repaint();
            });
            pulseTimer.start();
        }

        public void stop() { pulseTimer.stop(); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(SAGE_GREEN.getRed(), SAGE_GREEN.getGreen(),
                    SAGE_GREEN.getBlue(), (int)(alpha * 255)));
            g2.fillOval(1, 1, 8, 8);
            g2.dispose();
        }
    }

    // =========================================================
    // ANIMATED COUNTER LABEL
    // =========================================================

    /** Label yang menampilkan angka dengan animasi counter dari 0 ke target */
    public static class AnimatedCounter extends JLabel {
        private static final long serialVersionUID = 1L;
        private long targetValue = 0;
        private long currentValue = 0;
        private Timer animTimer;
        private final String prefix;
        private final String suffix;
        private final java.util.function.LongFunction<String> formatter;

        public AnimatedCounter(String prefix, String suffix,
                java.util.function.LongFunction<String> formatter) {
            super("0");
            this.prefix = prefix == null ? "" : prefix;
            this.suffix = suffix == null ? "" : suffix;
            this.formatter = formatter != null ? formatter
                    : (v -> this.prefix + v + this.suffix);
            setFont(new Font("Segoe UI", Font.BOLD, 26));
            setForeground(TEXT_DARK);
        }

        public void animateTo(long target) {
            if (animTimer != null && animTimer.isRunning()) animTimer.stop();
            final long start = 0;
            this.targetValue = target;
            this.currentValue = start;
            final long range = target - start;
            final long[] step = {0};
            final int STEPS = 30;

            animTimer = new Timer(16, e -> {
                step[0]++;
                float progress = (float) step[0] / STEPS;
                // easing: cubic out
                float eased = 1 - (float)Math.pow(1 - progress, 3);
                currentValue = start + (long)(range * eased);
                setText(formatter.apply(currentValue));
                if (step[0] >= STEPS) {
                    currentValue = targetValue;
                    setText(formatter.apply(targetValue));
                    ((Timer) e.getSource()).stop();
                }
            });
            animTimer.start();
        }

        /** Set nilai langsung tanpa animasi */
        public void setValue(long value) {
            if (animTimer != null) animTimer.stop();
            currentValue = value;
            targetValue = value;
            setText(formatter.apply(value));
        }
    }

    // =========================================================
    // GRADIENT PANEL untuk background header
    // =========================================================

    public static class GradientHeader extends JPanel {
        private static final long serialVersionUID = 1L;
        private final Color color1;
        private final Color color2;

        public GradientHeader(Color color1, Color color2) {
            this.color1 = color1;
            this.color2 = color2;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), 0, color2);
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }
}
