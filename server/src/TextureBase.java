import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.WritableRaster;
import java.io.File;

import javax.imageio.ImageIO;

public class TextureBase {
	
	private BufferedImage texture;
	private int width;
	private int height;
	
	public TextureBase() {
		try {
			texture = ImageIO.read(new File("skull.jpg"));
	
		} catch (Exception e) {
			System.out.println("Cannot load the provided image");
		}
		
		width = texture.getWidth();
		height = texture.getHeight();
	}
}
