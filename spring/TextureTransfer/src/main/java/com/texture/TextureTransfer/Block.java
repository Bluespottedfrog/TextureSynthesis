package com.texture.TextureTransfer;

import java.awt.image.BufferedImage;

public class Block {
    int endW;
    int endH;
    int startW;
    int startH;

    BufferedImage src;

    public Block(BufferedImage src, int blockSize) {
        this.src = src;
        startW = (int) (Math.random() * (src.getWidth() - blockSize));
        startH = (int) (Math.random() * (src.getHeight() - blockSize));
        endW = Math.min(startW + blockSize, src.getWidth());
        endH = Math.min(startH + blockSize, src.getHeight());
    }

    public BufferedImage getBlock() {
        BufferedImage result = new BufferedImage(endW - startW, endH - startH, src.getType());

        for (int i = 0; i < endW - startW; i++) {
            for (int j = 0; j < endH - startH; j++) {
                int rgb = src.getRGB(i + startW, j + startH);
                result.setRGB(i, j, rgb);
            }
        }

        return result;
    }
}
