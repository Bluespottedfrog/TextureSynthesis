//test image
const input = new Image();
const texture = new Image();

class Block{
  constructor(source, blocksize){
    this.src = source;
    this.startW = Math.random * source.width - blocksize;
    this.startH = Math.random * source.height - blocksize;
    this.endW = Math.min(startW + blockSize, source.width);
    this.endH = Math.min(startH + blockSize, source.height);
  }

  getBlock(){
    
  }
}

input.src = 'https://static.wikia.nocookie.net/dragons/images/e/ed/Chinese-dragon-red.jpg/';
texture.src = 'https://i.pinimg.com/736x/64/14/98/641498d8c60f30f2d4b725dce69c6bfd.jpg';
