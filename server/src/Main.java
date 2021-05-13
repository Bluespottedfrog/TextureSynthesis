
/*File MatteManipulations.java

 IAT455 - Workshop week 5
 Matte Creation and Manipulations

 **********************************************************/
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

class Main extends JFrame implements ActionListener{ 

	JPanel panel;
	JPanel images;
	JFileChooser fileChooser;
	JButton button;
	JLabel label;
	Block testBlock;
	
	
	BufferedImage blockTexture;
	BufferedImage block;
	BufferedImage originalImage;
	
	BufferedImage patch;
	BufferedImage noOverlap;
	BufferedImage randomed;
	
	BufferedImage grid;
	TextureSynthesis test;
	String filepath;
	
	int width; // width of the image
	int height; // height of the image
	
	boolean display = false;

	public Main() {

		
		images = new JPanel();
		images.setBounds(100, 0, 650, 750);
		
		fileChooser = new JFileChooser();
		button 		= new JButton("Upload Image");
		button.setVisible(true);

		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBackground(Color.BLACK);
		images.setBackground(Color.BLACK);
		
		
		
		//this.add(panel, BorderLayout.CENTER);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setBackground(Color.BLACK);
		this.setLayout(null);
		this.setTitle("Image Quilting");
		this.setSize(750,750);
		this.setVisible(true);
		
		buttonPanel.setBounds(30, 30, 200, 60);
		this.add(buttonPanel);
		
		
		
		buttonPanel.add(button);
		button.setPreferredSize(new Dimension(200, 50));
		this.add(images);

		
		button.addActionListener(this);
		


		
		//Anonymous inner-class listener to terminate program
		
		this.addWindowListener(
				new WindowAdapter(){//anonymous class definition
					public void windowClosing(WindowEvent e){
						System.exit(0);//terminate the program
					}//end windowClosing()
				}//end WindowAdapter
			);//end addWindowListener
			
	}// end constructor
	

@Override
public void paint(Graphics g) {
	super.paintComponents(g);
		this.getContentPane().setBackground(Color.BLACK);
	//If working with different images, this may need to be adjusted
	
		int w = width  ; 
		int h = height  ;

		g.drawImage(originalImage,25,150,w, h,images);
		
		
		g.drawImage(patch,50 + w, 150, patch.getWidth(), patch.getHeight(),images);
		g.drawImage(noOverlap, 50 + w + patch.getWidth(), 150, noOverlap.getWidth(), noOverlap.getHeight(),images);
		g.drawImage(randomed,50 + w + 2*patch.getWidth(), 150, patch.getWidth(), patch.getHeight(),images);
	
	    g.setColor(Color.WHITE);
	    Font f1 = new Font("Verdana", Font.PLAIN, 13); 
	    g.setFont(f1); 
	    g.drawString("Input Image", 25, 145);
	    g.drawString("Output - Minimum Boundary Cut", 50 + w, 145);
	    g.drawString("Output - Sum of Squared Difference", 50 + w + patch.getWidth(), 145);
	    g.drawString("Output - Random Selection", 50 + w + patch.getWidth() * 2, 145);
	    System.out.println("working");

    

}
// =======================================================

	public static void main(String[] args) {
		
		//instantiate this object

		Main img = new Main();
		img.setBackground(Color.BLACK);
		
		//render the image
		img.repaint();
	}// end main
	
	public boolean display(File file) {
		boolean worked = false;
		
		try {
			blockTexture = ImageIO.read(file);
			originalImage = ImageIO.read(file);
			worked = true;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		width = originalImage.getWidth();
		height = originalImage.getHeight();
		testBlock = new Block(blockTexture, 50);
		block = testBlock.generateBlock();
		
		test = new TextureSynthesis(blockTexture, 50);
		patch = test.generateTexture();
		randomed = test.randomize();
		noOverlap = test.generateNoFill();
		repaint();
		return worked;
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource() == button)	{
			display = false;
			JFileChooser file = new JFileChooser();
			file.setCurrentDirectory(new File(System.getProperty("user.home")));
			FileNameExtensionFilter filter = new FileNameExtensionFilter("*.Images", "jpg", "gif", "png");
			file.addChoosableFileFilter(filter);
			int result = file.showSaveDialog(null);
		
			if(result == JFileChooser.APPROVE_OPTION) {
				File selectedFile = file.getSelectedFile();
				filepath = selectedFile.getAbsolutePath();
				display(selectedFile);
	
			}
			else if(result == JFileChooser.CANCEL_OPTION) {
				System.out.println("No File Selected");

			}
		}
		
	}




}



// =======================================================//
