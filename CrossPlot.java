package bezier;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class CrossPlot extends JFrame implements KeyListener, MouseListener {
    private class MyPoint {
        double x;
        double y;

        public MyPoint(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    private final List<MyPoint> points;
    private final List<MyPoint> xFunc;
    private final List<MyPoint> yFunc;
    private JPanel contentPane;

    private static final int FRAME_WIDTH = 1200;
    private static final int FRAME_HEIGHT = 700;
    private static final int POINTSROKE = 10;
    private static final int LINESTROKE = 5;
    private static final int BEZIERSTROKE = 4;

    private static final int DECASTELJOSTEPS = 100;

    private boolean xFuncFlag = false;
    private boolean yFuncFlag = false;

    private boolean pointsFlag = true;
    private boolean linesFlag = true;
    private boolean curveFlag = true;
    static BufferStrategy bs;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        CrossPlot frame = new CrossPlot();
        frame.setVisible(true);
        frame.createBufferStrategy(2);
        bs = frame.getBufferStrategy();
        frame.paint();
        frame.paint();
    }

    public CrossPlot() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(false);
        setBounds(100, 100, FRAME_WIDTH, FRAME_HEIGHT);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0,0));
        contentPane.setBackground(Color.BLACK);
        setContentPane(contentPane);
        contentPane.setVisible(true);
        contentPane.addKeyListener(this);
        contentPane.addMouseListener(this);
        contentPane.setFocusable(true);
        contentPane.setFocusTraversalKeysEnabled(false);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        points = new ArrayList<>();
        xFunc = new ArrayList<>();
        yFunc = new ArrayList<>();
    }

    private void setCoordinateLines(Graphics2D g2) {
        g2.setColor(Color.lightGray);
        g2.fillRect(0, 0, FRAME_WIDTH, FRAME_HEIGHT);
        g2.setColor(Color.BLACK);
        g2.drawLine(FRAME_WIDTH / 2, 0, FRAME_WIDTH / 2, FRAME_HEIGHT);
        g2.drawLine(0, FRAME_HEIGHT / 2, FRAME_WIDTH, FRAME_HEIGHT / 2);

        // Draw arrowheads at the CrossPlot of the Y-axis and on the right of the X-axis
        drawArrow(g2, FRAME_WIDTH / 2, 32, -Math.PI / 2 + Math.PI);  // CrossPlot of Y-axis
        drawArrow(g2, FRAME_WIDTH - 7, FRAME_HEIGHT / 2, 0 + Math.PI); // Right of X-axis
    }
    private void drawArrow(Graphics2D g, int x, int y, double angle) {
        int arrowSize = 15;

        for (int i = -1; i <= 1; i += 2) {
            int dx = (int) (arrowSize * Math.cos(angle + i * Math.PI / 4));
            int dy = (int) (arrowSize * Math.sin(angle + i * Math.PI / 4));
            g.drawLine(x, y, x + dx, y + dy);
        }
    }
    private void setUpLabels(Graphics2D g2d){
        Font customFont = new Font("Arial", Font.BOLD, 20);
        g2d.setFont(customFont);
        g2d.setColor(Color.BLACK);
        g2d.drawString("X", FRAME_WIDTH - 30, (FRAME_HEIGHT/2) - 15);
        g2d.drawString("Y", FRAME_WIDTH / 2 + 15, 55);
        g2d.drawString("T", 20, (FRAME_HEIGHT/2) - 15);
        g2d.drawString("T", FRAME_WIDTH / 2 + 15, FRAME_HEIGHT - 20);
    }

    private void setUserGuide(Graphics2D g2d){
        Font customFont = new Font("Arial", Font.BOLD, 20);
        g2d.setFont(customFont);
        g2d.setColor(Color.BLACK);
        g2d.drawString("USER GUIDE", 100, (FRAME_HEIGHT/2) + 40);

        customFont = new Font("Arial", Font.BOLD, 15);
        g2d.setFont(customFont);
        g2d.drawString("Click with left button to draw a point", 100, (FRAME_HEIGHT/2) + 70);
        g2d.drawString("Click with right button to delete the last point", 100, (FRAME_HEIGHT/2) + 100);
        g2d.drawString("Press x to show/hide the x(t)", 100, (FRAME_HEIGHT/2) + 130);
        g2d.drawString("Press y to show/hide the y(t)", 100, (FRAME_HEIGHT/2) + 160);
        g2d.drawString("Press p to show/hide the points", 100, (FRAME_HEIGHT/2) + 190);
        g2d.drawString("Press l to show/hide the polygon", 100, (FRAME_HEIGHT/2) + 220);
        g2d.drawString("Press c to show/hide the bezier curve", 100, (FRAME_HEIGHT/2) + 250);
    }

    public void paint() {
        Graphics2D g2 = null;

        do {
            try {
                g2 = (Graphics2D) bs.getDrawGraphics();
                draw(g2);
            } finally {
                g2.dispose();
            }
            bs.show();
        } while (bs.contentsLost());
    }

    public void draw(Graphics2D g2) {
        if (g2 == null) {
            return;
        }
        setCoordinateLines(g2);
        setUpLabels(g2);
        setUserGuide(g2);

        if (linesFlag) {
            drawLine(g2, points);
        }
        if (curveFlag) {
            drawBezierCurve(g2, points);
        }
        if (pointsFlag) {
            drawPoints(g2, points);
        }
        if (xFuncFlag) {
            drawXPts(g2);
        }
        if (yFuncFlag) {
            drawYPts(g2);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }


    @Override
    public void keyReleased(KeyEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void keyTyped(KeyEvent e) {
        char keyChar = e.getKeyChar();
        switch (keyChar) {
            case 'x' -> xFuncFlag = !xFuncFlag;
            case 'y' -> yFuncFlag = !yFuncFlag;
            case 'p' -> pointsFlag = !pointsFlag;
            case 'l' -> linesFlag = !linesFlag;
            case 'c' -> curveFlag = !curveFlag;
        }
        paint();
    }


    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        int btnIdx = e.getButton();
        if (btnIdx == MouseEvent.BUTTON1) {
            Point p = e.getPoint();
            p.x += 10;
            p.y += 30;
            if (!checkIsInFirstQuadrant(p.x, p.y)) {
                return;
            }
            this.points.add(new MyPoint(p.x, p.y));
        } else if (btnIdx == MouseEvent.BUTTON3) {
            if (points.size() == 0) {
                return;
            }
            points.remove(points.size() - 1);
        }
        paint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    private boolean checkIsInFirstQuadrant(int x, int y) {
        return x >= FRAME_WIDTH / 2 && y <= FRAME_HEIGHT / 2;
    }

    private void drawLine(Graphics2D g2d, List<MyPoint> myPoints) {
        if (myPoints.size() > 1) {

            g2d.setStroke(new BasicStroke(LINESTROKE));
            g2d.setColor(Color.yellow);
            MyPoint prevPoint = myPoints.getFirst();
            for (int i = 1; i < myPoints.size(); i++) {
                MyPoint currentPoint = myPoints.get(i);
                g2d.drawLine((int) prevPoint.x, (int) prevPoint.y, (int) currentPoint.x, (int) currentPoint.y);
                prevPoint = currentPoint;
            }
        }
    }

    private void drawPoints(Graphics2D g2d, List<MyPoint> myPoints) {
        g2d.setStroke(new BasicStroke(POINTSROKE));
        g2d.setColor(Color.blue);
        for (var it : myPoints) {
            g2d.fillOval((int) it.x - POINTSROKE / 2, (int) it.y - POINTSROKE / 2, POINTSROKE, POINTSROKE);
        }
    }

    private void drawBezierCurve(Graphics2D g2d, List<MyPoint> myPoints) {
        if (myPoints.size() < 2) {
            return;
        }

        List<MyPoint> curvePoints = calculateBezierCurve(myPoints);
        int[] xPoints = curvePoints.stream().mapToInt(point -> (int) point.x).toArray();
        int[] yPoints = curvePoints.stream().mapToInt(point -> (int) point.y).toArray();
        g2d.setStroke(new BasicStroke(BEZIERSTROKE));
        g2d.setColor(Color.RED);
        g2d.drawPolyline(xPoints, yPoints, curvePoints.size());
    }

    private List<MyPoint> calculateBezierCurve(List<MyPoint> myPoints) {
        List<MyPoint> curvePoints = new ArrayList<>();

        for (int i = 0; i <= DECASTELJOSTEPS; i++) {
            double t = (double) i / DECASTELJOSTEPS;
            MyPoint curvePoint = deCasteljauAlgorithm(t, myPoints);
            curvePoints.add(curvePoint);
        }

        return curvePoints;
    }

    private MyPoint deCasteljauAlgorithm(double t, List<MyPoint> myPoints) {
        List<MyPoint> pointsCopy = new ArrayList<>(myPoints);

        while (pointsCopy.size() > 1) {
            List<MyPoint> newPoints = new ArrayList<>();
            for (int i = 0; i < pointsCopy.size() - 1; i++) {
                double x = ((1 - t) * pointsCopy.get(i).x + t * pointsCopy.get(i + 1).x);
                double y = ((1 - t) * pointsCopy.get(i).y + t * pointsCopy.get(i + 1).y);
                newPoints.add(new MyPoint(x, y));
            }
            pointsCopy = new ArrayList<>(newPoints);
        }
        return pointsCopy.getFirst();
    }

    private void drawXPts(Graphics2D g2d) {
        computeXFuncPts();
        if (linesFlag) {
            drawLine(g2d, xFunc);
        }
        if (curveFlag) {
            drawBezierCurve(g2d, xFunc);
        }
        if (pointsFlag) {
            drawPoints(g2d, xFunc);
        }
    }

    private void drawYPts(Graphics2D g2d) {
        computeYFuncPts();
        if (linesFlag) {
            drawLine(g2d, yFunc);
        }
        if (curveFlag) {
            drawBezierCurve(g2d, yFunc);
        }
        if (pointsFlag) {
            drawPoints(g2d, yFunc);
        }
    }

    private void computeXFuncPts() {
        // Get the number of control points + 1
        int size = points.size() + 1;

        // Number of points of x(t) function = number of control points
        xFunc.clear(); // Clear previous values if any
        for (int i = 0; i < size - 1; i++) {
            xFunc.add(new MyPoint(0, 0));
        }
        for (int i = 1; i <= size - 1; i++) {
            // Calculate y coordinate for x(t) function
            double yCoordinate = (FRAME_HEIGHT / 2.0) + i * ((FRAME_HEIGHT / 2.0) / size);

            xFunc.get(i - 1).y = yCoordinate;
            xFunc.get(i - 1).x = points.get(i - 1).x;
        }
    }

    private void computeYFuncPts() {
        // Get the number of control points + 1
        int size = points.size() + 1;

        // Number of points of y(t) function = number of control points
        yFunc.clear(); // Clear previous values if any
        for (int i = 0; i < size - 1; i++) {
            yFunc.add(new MyPoint(0, 0));
        }
        for (int i = 1; i <= size - 1; i++) {
            // Calculate x coordinate for y(t) function
            double xCoordinate = (FRAME_WIDTH / 2.0) - i * ((FRAME_WIDTH / 2.0) / size);

            yFunc.get(i - 1).x = xCoordinate;
            yFunc.get(i - 1).y = points.get(i - 1).y;
        }
    }
}