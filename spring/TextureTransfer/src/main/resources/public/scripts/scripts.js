var grid = document.querySelector('.heroGrid');

if(grid){
  var gridw = grid.clientWidth;
  var gridh = grid.clientHeight;

  var rows = gridh/50;
  var cols = gridw/50;



var textWrapper = document.querySelector('.ani .letters');
textWrapper.innerHTML = textWrapper.textContent.replace(/\S/g, "<span class='letter'>$&</span>");

for(var i = 0; i < rows; i++){
  var rowContainer = document.createElement("div");
  rowContainer.style.display = "flex";
  rowContainer.style.width = gridw + "px";
  for(var j = 0; j < cols; j++){
    var pixel = document.createElement("div");
    pixel.className = "pixel";
    pixel.style.width = "50px";
    pixel.style.height = "50px";
    rowContainer.appendChild(pixel);
  }
  grid.appendChild(rowContainer);
}

var gridAnimation = anime({
  targets:'.pixel',
  scale:[
    {value: 0, easing: 'easeOutSine', duration: 500},
  ],
  delay: anime.stagger(100, {grid: [rows, cols], from: 'center'})
});




var textAnimation = anime.timeline({loop: false})
  .add({
    targets: '.ani .letter',
    translateY: ["2em", 0],
    translateZ: 0,
    duration: 800,
    delay: (el, i) => 80 * i
  });

var arrows = anime({
  targets: '.arrows .down',
  duration: 1000,
  loop: true,
  easing: 'easeInOutQuad',
  keyframes: [
    {translateY: 10},
    {translateY: 0}
  ],
  delay: 500
});

gridAnimation.play();
textAnimation.play();
arrows.play();

}

function mousemovement(event){
  var hero_img = document.querySelector('.hero_img');
  if(hero_img){
    hero_img.style.transform = "translate(" + event.clientX + "px " + event.clientY + "px);";
  }
}

function closeMenu(id){
  var menu = id;
  menu.style.display = "none";
}

function openMenu(id, imageName){
  var menu = id;
  menu.style.display = "flex";
  if(imageName != ''){
    var image = document.querySelector('#expandImage');
    image.setAttribute("src", imageName);
  }
}


document.addEventListener("mousemove", mousemovement);

function animateLogo(){
  var logo = document.querySelector("#logo");

  var anim = bodyMovin(logo, false);

  logo.addEventListener('mouseenter', () => {
  	anim.setDirection(1);
  	anim.play();
  });

  logo.addEventListener('mouseleave', () => {
  	anim.pause();
  });

}

function bodyMovin(logo, autoplay){
  console.log(logo);
  var anim = bodymovin.loadAnimation({
    container: logo,
    path: 'res/AnimatedLogo.json',
    renderer: 'svg',
    loop: true,
    autoplay: autoplay
  });

  return anim;
}

animateLogo();

//animation.play();
