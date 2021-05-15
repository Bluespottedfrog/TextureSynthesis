import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class Block {
    int endW;
    int endH;
    int startW;
    int startH;

    BufferedImage blk;

    public Block(BufferedImage t, int blockSize) {
        blk = t;
        startW = (int) (Math.random() * (blk.getWidth() - blockSize));
        startH = (int) (Math.random() * (blk.getHeight() - blockSize));
        endW = startW + blockSize;
        endH = startH + blockSize;

        if (endH > blk.getHeight()) {
            endH = blk.getHeight();
        }

        if (endW > blk.getWidth()) {
            endW = blk.getWidth();
        }
    }

    public Block(BufferedImage t, int blockSize, int i) {
        blk = t;
        startW = (int) ((blk.getWidth() - blockSize) - i);
        startH = (int) ((blk.getHeight() - blockSize) - i);
        endW = startW + blockSize;
        endH = startH + blockSize;

        if (endH > blk.getHeight()) {
            endH = blk.getHeight();
        }

        if (endW > blk.getWidth()) {
            endW = blk.getWidth();
        }
    }


    public int width() {
        return blk.getWidth() - startW;
    }

    public int height() {
        return blk.getHeight() - startH;
    }

    public BufferedImage generateBlock() {
        WritableRaster wRaster = blk.copyData(null);
        BufferedImage result = new BufferedImage(endW - startW, endH - startH, blk.getType());

        for (int i = 0; i < endW - startW; i++) {
            for (int j = 0; j < endH - startH; j++) {
                int rgb = blk.getRGB(i + startW, j + startH);
                result.setRGB(i, j, rgb);
            }
        }

        return result;
    }
}
