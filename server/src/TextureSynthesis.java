import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Arrays;

public class TextureSynthesis {
	
	int rows, cols;
	BufferedImage txt;
	BufferedImage[][] patchArray;
	BufferedImage[] bestBlocks;
	
	BufferedImage genesis;
	
	int blockSize;
	double tolerance;
	int overlap;
	
	
	public TextureSynthesis(BufferedImage texture, int blockSize) {
		txt = texture;
		rows = 10;
		cols = 10;
		patchArray = new BufferedImage[rows][cols];
		this.blockSize = blockSize;
		overlap = blockSize/6;
		tolerance = 100;
	}
	
	private void fillPatch() {
		
		Block block = new Block(txt, blockSize);
		BufferedImage genesis = block.generateBlock();
		int sampleSize = 100;
		
		for(int j = 0; j < rows; j++) {
			for(int i = 0; i < cols; i++) {
				
			
				//TEMP: If the image is the first in the row or column, then generate a block
				if(j == 0 && i > 0) {
					genesis = matchBlock(patchArray[j][i - 1],  null, sampleSize);
				}
				else if(j > 0 && i == 0) {
					genesis = matchBlock(null, patchArray[j - 1][i] , sampleSize);			
				}
				else if(j > 0 && i > 0) {
			
					genesis = matchBlock(patchArray[j][i - 1], patchArray[i - 1][j], sampleSize);
					
				}

				patchArray[j][i] = genesis;
				
			}
		}
	}
	public BufferedImage randomize() {
		BufferedImage result = new BufferedImage(blockSize * rows, blockSize * cols, txt.getType());	
		
		for(int j = 0; j < rows; j++) {
			for(int i = 0; i < cols; i++) {
				BufferedImage tile = new Block(txt, blockSize).generateBlock();
				
				for(int x = 0; x < blockSize; x++) {
					for(int y = 0; y < blockSize; y++) {
						
						result.setRGB(x + blockSize * i, y + blockSize * j , tile.getRGB(x, y));					

					}
				}//END OF SMALLER LOOP
			}
		}
		
		return result;
		
	}
	public BufferedImage generateNoFill() {
		
		
		BufferedImage result = new BufferedImage(blockSize * rows, blockSize * cols, txt.getType());	
		fillPatch();
		
		//Fill with everything
		for(int j = 0; j < rows; j++) {
			//Width - num of cols
			for(int i = 0; i < cols; i++) {	
				
				BufferedImage tile = patchArray[j][i];
				
				for(int x = 0; x < blockSize; x++) {
					for(int y = 0; y < blockSize; y++) {
						
						result.setRGB(x + (blockSize - overlap) * i, y + (blockSize - overlap) * j , tile.getRGB(x, y));					

					}
				}//END OF SMALLER LOOP
				
				
			}
		}

		
		
		
		return result; 
		
	}

	public BufferedImage generateTexture() {
		
		
		BufferedImage result = new BufferedImage(blockSize * rows, blockSize * cols, txt.getType());	
		fillPatch();
		
		//Fill with everything
		for(int j = 0; j < rows; j++) {
			//Width - num of cols
			for(int i = 0; i < cols; i++) {	
				
				BufferedImage tile = patchArray[j][i];
				
				for(int x = 0; x < blockSize; x++) {
					for(int y = 0; y < blockSize; y++) {
						
						result.setRGB(x + (blockSize - overlap) * i, y + (blockSize - overlap) * j , tile.getRGB(x, y));					

					}
				}//END OF SMALLER LOOP
				
				
			}
		}// END OF LARGER LOOP
		
		//Fill horizontal overlap
		//Height - num of rows
		for(int j = 0; j < rows; j++) {
			//Width - num of cols
			for(int i = 1; i < cols; i++) {	
				
				BufferedImage tile = patchArray[j][i];
				
				for(int y = 0; y < blockSize; y++) {
					for(int x = 0; x < overlap; x++) {
						
						double[][] costPath = minCostPath(patchArray[j][i - 1], tile, blockSize, overlap, 1);
						int rgb = getOverlapColor(patchArray[j][i - 1], tile,costPath, y, x, 1);
						result.setRGB(x + (blockSize - overlap) * i, y + (blockSize - overlap) * j , rgb);					

					}
				}//END OF SMALLER LOOP
				
				
			}
		}// END OF LARGER LOOP
		
		//Fill vertical overlap
		//Height - num of rows
		for(int j = 1; j < rows; j++) {
			//Width - num of cols
			for(int i = 0; i < cols; i++) {	
				
				BufferedImage tile = patchArray[j][i];
				
				for(int y = 0; y < overlap; y++) {
					for(int x = 0; x < blockSize; x++) {
						
						double[][] costPath = minCostPath(patchArray[j-1][i], tile, overlap, blockSize, 2);
						int rgb = getOverlapColor(patchArray[j-1][i], tile,costPath, y, x, 2);
						result.setRGB(x + (blockSize - overlap) * i, y + (blockSize - overlap) * j , rgb);					

					}
				}//END OF SMALLER LOOP
				
				
			}
		}// END OF LARGER LOOP
		
		
		
		return result; 
		
	}
	

	private BufferedImage matchBlock(BufferedImage genesisW, BufferedImage genesisH, int sampleSize) {
		
		Block block = new Block(txt, blockSize);
		int index = 0;
		BufferedImage[] sampleBlocks = new BufferedImage[sampleSize];
		double[] error = new double[sampleSize];
		double[] errorH = new double[sampleSize];
		double[] errorAvg = new double[sampleSize];
		
		//Fill an array of length sampleSize samples of blocks that could match the genesis
		for(int i = 0; i < sampleSize; i++) {
			block = new Block(txt, blockSize);
			sampleBlocks[i] = block.generateBlock();
		}
		
		
		//Find the error difference between genesis and samples
		
		
		for(int f = 0; f < sampleSize; f++) {
	
			double errorNum = 0;
			double errorNumH = 0;
			
			//Iterate through the overlap (left to right) and compare the colors
			if(genesisW != null) {
				for(int i = 0; i < overlap; i++) {
					for(int j = 0; j < blockSize; j++) {
						
						int x = i + blockSize - overlap ;
						double res = calculateError(x, j, i, j, genesisW, sampleBlocks[f]);

						errorNum += res;
						
					}
				}
			}

			
			//Iterate through the overlap (top down) and compare the colors
			if(genesisH != null) {
				for(int i = 0; i < overlap; i++) {
					for(int j = 0; j < blockSize; j++) {
						
						int y = i + blockSize - overlap;
	
						double res = calculateError(j, y, j, i, genesisH, sampleBlocks[f]);
						
						errorNumH += res;
						
					}
				}
			}
			
			if(genesisW != null) {
				
				errorNum /= overlap*blockSize;
				error[f] = errorNum;
			}
			
			if(genesisH != null) {
				errorNumH /= overlap*blockSize;
				errorH[f] = errorNumH;
			}
			
		}
		
		if(genesisW != null && genesisH != null) {
			for(int i = 0; i < errorAvg.length; i++) {
				errorAvg[i] = (error[i] + errorH[i])/2;
			}
		}

		
		//Find the block that is the best match based on error
		for(int i = 0; i < sampleBlocks.length; i++) {
			
			if(genesisW != null && genesisH == null) {
				if(error[i] < error[index]) {
					index = i;
				}
			}
			else if(genesisW == null && genesisH != null) {
				if(errorH[i] < errorH[index]) {
					index = i;
				}
			}
			else {
				if(errorAvg[i] < errorAvg[index]) {
					index = i;
				}
			}
			
		}
		
	
		return sampleBlocks[index];
		
	}// END OF MATCHBLOCK
	
	private double calculateError(int x, int y, int x2, int y2, BufferedImage genesis, BufferedImage sample) {
		double res;
		Color genC = new Color(genesis.getRGB(x, y));
		Color exC = new Color(sample.getRGB(x2,y2));
		
		int r1,r2;
		int g1,g2;
		int b1,b2;
		
		r1 = genC.getRed(); r2 = exC.getRed();
		g1 = genC.getRed(); g2 = exC.getRed();
		b1 = genC.getBlue(); b2 = exC.getBlue();
		
		res = Math.pow(((r2 - r1) + (g2 - g1) + (b2 - b1)), 2);
		
		res /= 5852.25;
		
		return res;
		
	}//END OF BLOCK
	
	//Function that returns a 2D matrix of the cut region
	private double[][] minCostPath(BufferedImage b1, BufferedImage b2, int height, int width, int side) {
		

		double[][] errorArray = new double[height][width];
		double[][] costArray = new double[height][width];
		double[][] costPath = new double[height][width];
		
		for(int j = 0; j < height; j++) {
			for(int i = 0; i < width; i++) {
				int x = i;
				
				if(side == 1) {
					x = i + blockSize - overlap;
					errorArray[j][i] = calculateError(x, j, i, j, b1, b2);
				}
				else if(side == 2){
					errorArray[j][i] = calculateError(x, j + blockSize - overlap, i, j, b1, b2);
				}
				else {
					x = i + blockSize - overlap;
					errorArray[j][i] = calculateError(x, j + blockSize - overlap, i, j, b1, b2);
				}
				//Determine error
				
				
			}
		}
		
		costArray = findPath(errorArray);

		
		//System.out.println(Arrays.deepToString(costArray));
		
		int y = costArray.length - 1;
		int x = costArray[0].length - 1;
		
		costPath[y][x] = 1;
		
		while(x >= 0 || y >= 0) {
			//System.out.println("x: " + x + " y: " + y);
			if(y > 0) {
				if(x > 0) {
					if(costArray[y - 1][x] <= costArray[y][x - 1]) {
						costPath[y - 1][x] = 1;
						y--;
					}
				}
				else {
					costPath[y - 1][x] = 1;
					y--;
				}
			}
			
			if(x > 0) {
				if(y > 0) {
					if(costArray[y - 1][x] >= costArray[y][x - 1]) {
						costPath[y][x - 1] = 1;
						x--;
					}
				}
				else {
					costPath[y][x - 1] = 1;
					x--;
				}
			}
			
			if(x == 0 && y == 0) {
				costPath[x][y] = 1;
				x--; y--;
			}
			

		}
		
		
		//Fill
		for(int j = 0; j < costPath.length; j++) {
			boolean flag = false;
			for(int i = 0; i < costPath[0].length; i++) {
				if(costPath[j][i] == 1)
				flag = true;
		
				
				if(flag) 
				costPath[j][i] = 1;
				
			}
		}
		
		//System.out.println("DONE");
		return costPath;
		

		
		
	} // END OF MINCOSTPATH FUNCTION
	

	// =========================================
	
	private double[][] findPath(double[][] costMaze) {
		double[][] result = new double[costMaze.length][costMaze[0].length];
		for(int i = 0; i < result.length; i++) {
			for(int j = 0; j < result[0].length; j++) {
				result[i][j] = costMaze[i][j];
				
				if(i > 0 && j > 0) {
					double temp = Math.min(result[i - 1][j], result[i][j - 1]);
					temp = Math.min(temp, result[i - 1][j - 1]);
					result[i][j] += Math.min(result[i - 1][j], result[i][j - 1]);
				}
				else if(i > 0) {
					result[i][j] += result[i - 1][j];
				}
				else if(j > 0) {
					result[i][j] += result[i][j - 1];
				}
				
			}
		}
		
		return result;
		
	}// END OF FIND PATH
	

	
	
	private int getOverlapColor(BufferedImage b1, BufferedImage b2, double[][] costPath, int j, int i, int side) {
		if(side == 1) {
			if(costPath[j][i] == 1)
				return b2.getRGB(i, j);
				//return new Color(255,255,255).getRGB();
			return b1.getRGB(i + (blockSize - overlap), j);
			//return new Color(255,255,255).getRGB();
		}
		
		else{
			if(costPath[j][i] == 1)
				return b1.getRGB(i, j + blockSize - overlap);
			return b2.getRGB(i, j);
			
		}
		

		
	}

	
}
