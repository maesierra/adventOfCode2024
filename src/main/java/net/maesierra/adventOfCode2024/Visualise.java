package net.maesierra.adventOfCode2024;


import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

import static javax.swing.SwingUtilities.invokeLater;

public class Visualise extends Runner{

    private static final int SPEED = 500;

    public static void main(String[] args) {
        invokeLater(() -> {
            try {
                run(args, (data, input) -> {
                    JFrame frame = new JFrame("Day %s".formatted(data.day()));
                    Consumer<Graphics2D> paintAction = switch (data.part()) {
                        case "1" -> data.solution().visualisePart1(input);
                        case "2" -> data.solution().visualisePart2(input);
                        default -> throw new IllegalStateException("Unexpected value: " + data.part());
                    };
                    frame.add(new JPanel() {
                        @Override
                        public void paint(Graphics g) {
                            paintAction.accept((Graphics2D) g);
                        }
                    });



                    frame.setSize(2000, 1800);
                    frame.setVisible(true);
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                    Timer timer = new Timer(SPEED, (event) -> {
                        frame.repaint();
                    });
                    timer.start();
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }});
    }
}
