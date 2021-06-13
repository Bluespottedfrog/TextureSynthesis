package com.texture.TextureTransfer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;


import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@SpringBootApplication
public class TextureTransferApplication {
    private static final int BUFFER_SIZE = 8192;

	BufferedImage blockTexture;
	BufferedImage targetImage;
	
	TextureTransfer textureTransfer;
	BufferedImage finalOutput;
	
	int width;
	int height;
	
	public static void main(String[] args) {
		SpringApplication.run(TextureTransferApplication.class, args);
	}
	
	//Convert BufferedImage into base64 then send the string to frontend
	@PostMapping("/texture_transfer")
	public String uploadImage(@RequestParam("textureFile") MultipartFile textureFile, @RequestParam("imageFile") MultipartFile imageFile, Model model) throws IOException {
		
		File file = toFile(imageFile);
		File texture = toFile(textureFile);
		String b64 = "";
		
		if(file != null) {
			finalOutput(texture, file);
			b64 = imageToBase64(finalOutput);
			model.addAttribute("image", b64);
		}
		
		return "texture_transfer";
	}
	
	//Function that converts a submitted MultipartFile into a File object, which is then converted to BufferedImage
	private File toFile(MultipartFile f) throws IOException {
		File file = new File(f.getOriginalFilename());
		file.createNewFile();
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(f.getBytes());
		fos.close();
		return file;
	}
	
	//Read both input files then perform texture transfer
	private void finalOutput(File textureFile, File inputFile) {
		try {
			blockTexture = ImageIO.read(textureFile);
			targetImage = ImageIO.read(inputFile);
			
			width = blockTexture.getWidth();
			height = blockTexture.getHeight();
			
			textureTransfer = new TextureTransfer(blockTexture, targetImage,5);
			finalOutput = textureTransfer.generateTexture();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
    public static String imageToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(BUFFER_SIZE);
        ImageIO.write(image, "png", out);
        return Base64.getEncoder().encodeToString(out.toByteArray());
    }
    
    //Redirect to texture_transfer.html
    @RequestMapping("/texture_transfer")
    public String defectDetails() {
        return "texture_transfer";
    }
    
    //Redirect to index.html
    @GetMapping
    public String goHome() {
        return "index";
    }


}
