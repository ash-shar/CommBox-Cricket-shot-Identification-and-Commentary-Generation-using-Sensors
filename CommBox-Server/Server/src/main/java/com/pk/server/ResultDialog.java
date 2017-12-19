package com.pk.server;

import javax.swing.*;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


public class ResultDialog extends JDialog {
    public ResultDialog(JFrame parent, String message, String IMG_PATH) {

        super(parent, "Nice Shot !");
        JLabel img_label = new JLabel("");
        System.out.println("creating the window..");
        // set the position of the window
        // Create a message
        JPanel messagePane = new JPanel();
        messagePane.setBackground(Color.WHITE);

        messagePane.add(new JLabel(message));



        // get content pane, which is usually the
        // Container of all the dialog's components.

        Box box = new Box(BoxLayout.Y_AXIS);

        box.add(Box.createVerticalGlue());
        box.add(messagePane);



        try {
            BufferedImage img = ImageIO.read(new File(IMG_PATH));
            ImageIcon icon = new ImageIcon(img);
            img_label = new JLabel(icon);
        } catch (IOException e) {
            e.printStackTrace();
        }


        box.add(Box.createVerticalGlue());

        JPanel messagePane1 = new JPanel();

        messagePane1.setBackground(Color.WHITE);
        messagePane1.add(img_label);
        box.add(messagePane1);
        box.add(Box.createVerticalGlue());

        getContentPane().add(box);

//        // Create a button
//        JPanel buttonPane = new JPanel();
//        JButton button = new JButton("PK");
//        buttonPane.add(button);
//        // set action listener on the butto
//        button.addActionListener(new MyActionListener());
//        getContentPane().add(buttonPane, BorderLayout.PAGE_END);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setVisible(true);
    }
}
