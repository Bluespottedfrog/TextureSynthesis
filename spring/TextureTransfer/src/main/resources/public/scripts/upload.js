
var textureString;
var inputString;

var texturize_submit = document.querySelector("#texturize_submit");

function readTexture(input){

  //if there are files
  if(input.files && input.files[0]){
    var reader = new FileReader();

    reader.onload = function (e) {
      $('#texture').css('background-image', "url(" + e.target.result + ")" );
      textureString = e.target.result;

      if(inputString != null && textureString != null){
        texturize_submit.disabled = false;
      }

    };
    reader.readAsDataURL(input.files[0]);

  }

}

function readInput(input){
  var button = document.querySelector("#texturize");

  //if there are files
  if(input.files && input.files[0]){
    var reader = new FileReader();

    reader.onload = function (e) {
      //$('#input').attr('src', e.target.result);
      $('#input').css('background-image', "url(" + e.target.result + ")" );
      inputString = e.target.result;

      if(textureString != null && inputString != null){
        texturize_submit.disabled = false;
      }

    };
    reader.readAsDataURL(input.files[0]);
  }

}

function sendImages(){

  var label = document.querySelector("#texturize_label");

  if(textureString && inputString){
    var xhttp = new XMLHttpRequest();
    xhttp.open("POST", "/texture_transfer", true);
    xhttp.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    xhttp.send("imageFile=" + "penis" + "&textureFile=" + "penis");
    console.log("Sent");

  }
  else if(inputString == null && textureString == null){
    label.textContent = "Missing a texture & input!";
  }
  else if(inputString == null){
    label.textContent = "Missing an input!";
  }
  else{
    label.textContent = "Missing a texture!";
  }


}

function hoverButton(name){
  var uploadButton = name;
  uploadButton.classList.add("pseudo-hover");
}

function hoverOut(name){
  var uploadButton = name;
  uploadButton.classList.remove("pseudo-hover");
}

function assign(image){
  var texture = document.querySelector("#texture");


  texture.style.backgroundImage = "url(" + "res/textures/" + image.id + ")";
  closeMenu(document.querySelector("#menu"));
  console.log(image.id);
}
