package com.texture.TextureTransfer;

import org.apache.tomcat.util.codec.binary.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.awt.image.BufferedImage;
import java.io.*;
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

    public static BufferedImage base64ToImage(String data) throws IOException {
        String base64Image = data.split(",")[1];
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
        return ImageIO.read(new ByteArrayInputStream(imageBytes));
    }

    public static String imageToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return Base64.getEncoder().encodeToString(out.toByteArray());
    }

    public static void main(String[] args) {
        SpringApplication.run(TextureTransferApplication.class, args);
    }

    //Convert BufferedImage into base64 then send the string to frontend
    //Might need to send post data as b64, as multipart form upload saves to e
    @PostMapping("/texture_transfer")
    public String uploadImage(@RequestParam("textureFile") MultipartFile textureFile, @RequestParam("imageFile") MultipartFile imageFile, Model model) {
        try {
            BufferedImage file = multipartToImage(imageFile);
            BufferedImage texture = multipartToImage(textureFile);

            String b64 = performTextureTransfer(file, texture);
            model.addAttribute("image", b64);
        } catch (IOException e) {
            // TODO: handle errors
        }

        return "texture_transfer";
    }

    private BufferedImage multipartToImage(MultipartFile f) throws IOException {
        String sb = "data:image/png;base64," + StringUtils.newStringUtf8(Base64.getEncoder().encode(f.getBytes()));
        return base64ToImage(sb);
    }

    private String performTextureTransfer(BufferedImage input, BufferedImage texture) throws IOException {
        TextureTransfer textureTransfer = new TextureTransfer(texture, input, 3);
        return imageToBase64(textureTransfer.generateTexture());
    }

    //Redirect to texture_transfer.html
    @RequestMapping("/texture_transfer")
    public String toTextureTransfer() {
        return "texture_transfer";
    }

    //Redirect to home
    @RequestMapping("/index")
    public String backHome() {
        return "index";
    }

    //Redirect to index.html
    @GetMapping
    public String goHome() {
        return "index";
    }
}
