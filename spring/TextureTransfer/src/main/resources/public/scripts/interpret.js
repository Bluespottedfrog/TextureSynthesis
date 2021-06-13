function interpret(){
  var b64 = document.querySelector('#b64');
  if(b64){
    var image = new Image();
    image.src='data:image/png;base64,' + b64.innerText;

    var container = document.querySelector("#result");
    container.appendChild(image);
  }
  else{
    console.log("is null");
  }
  b64.remove();
}

interpret();
