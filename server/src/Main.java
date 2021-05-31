import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

class Main extends JFrame implements ActionListener {

    JPanel images;
    JFileChooser fileChooser;
    JButton button;

    BufferedImage blockTexture;
    BufferedImage originalImage;

    BufferedImage patch;

    TextureSynthesis test;
    String filepath;

    int width; // width of the image
    int height; // height of the image

    boolean display = false;

    public Main() {
        images = new JPanel();
        images.setBounds(100, 0, 650, 750);

        fileChooser = new JFileChooser();
        button = new JButton("Upload Image");
        button.setVisible(true);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.BLACK);
        images.setBackground(Color.BLACK);

        //this.add(panel, BorderLayout.CENTER);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setBackground(Color.BLACK);
        this.setLayout(null);
        this.setTitle("Image Quilting");
        this.setSize(750, 750);
        this.setVisible(true);

        buttonPanel.setBounds(30, 30, 200, 60);
        this.add(buttonPanel);

        buttonPanel.add(button);
        button.setPreferredSize(new Dimension(200, 50));
        this.add(images);

        button.addActionListener(this);

        //Anonymous inner-class listener to terminate program
        this.addWindowListener(
                new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                }
        );
    }

    @Override
    public void paint(Graphics g) {
        super.paintComponents(g);
        this.getContentPane().setBackground(Color.BLACK);
        //If working with different images, this may need to be adjusted

        int w = width;
        int h = height;

        g.drawImage(originalImage, 25, 150, w, h, images);

        g.drawImage(patch, 50 + w, 150, patch.getWidth(), patch.getHeight(), images);

        g.setColor(Color.WHITE);
        Font f1 = new Font("Verdana", Font.PLAIN, 13);
        g.setFont(f1);
        g.drawString("Input Image", 25, 145);
        g.drawString("Output - Minimum Boundary Cut", 50 + w, 145);
    }

// =======================================================

    public static void main(String[] args) {
        //instantiate this object
        Main img = new Main();
        img.setBackground(Color.BLACK);

        //render the image
        img.repaint();
    }

    public void display(File file) {
        try {
            blockTexture = ImageIO.read(file);
            originalImage = ImageIO.read(file);
            // TODO: Hardcoded targetImage for now
            BufferedImage targetImage = ImageIO.read(getClass().getResource("/starry.jpg"));

            width = originalImage.getWidth();
            height = originalImage.getHeight();

            test = new TextureSynthesis(blockTexture, targetImage);
            patch = test.generateTexture();
            repaint();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }


    }


    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
        if (e.getSource() == button) {
            display = false;
            JFileChooser file = new JFileChooser();
            file.setCurrentDirectory(new File(System.getProperty("user.home")));
            FileNameExtensionFilter filter = new FileNameExtensionFilter("*.Images", "jpg", "gif", "png");
            file.addChoosableFileFilter(filter);
            int result = file.showSaveDialog(null);

            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = file.getSelectedFile();
                filepath = selectedFile.getAbsolutePath();
                display(selectedFile);
            } else if (result == JFileChooser.CANCEL_OPTION) {
                System.out.println("No File Selected");
            }
        }
    }
}

// =======================================================//
