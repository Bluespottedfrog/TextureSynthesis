;(function($, win) {
  $.fn.inViewport = function(cb) {
     return this.each(function(i,el){
       function visPx(){
         var H = $(this).height(),
             r = el.getBoundingClientRect(), t=r.top, b=r.bottom;
         return cb.call(el, Math.max(0, t>0? H-t : (b<H?b:H)));
       } visPx();
       $(win).on("resize scroll", visPx);
     });
  };
}(jQuery, window));


$(".hidden").inViewport(function(px){
    if(px) {
      var sway = anime({
        targets: '.hidden',
        easing: 'easeInOutQuad',
        duration: 5000,
        loop: false,
        keyframes: [
          {translateX: -100},
          {translateX: -20},
          {translateX: -100},
          {translateX: -20}
        ]
      });
    } ;
});

$(".grid-item").inViewport(function(px){
    if(px) {
      var animation = anime({
        targets: '.grid-item',
        opacity: 1,
        translateY: 20,
        delay: anime.stagger(100),
        easing: 'easeInOutQuad',
      });
    } ;
});

$(".fadein-1").inViewport(function(px){
    if(px) {
      $(".fadein-1").addClass("triggered");
    } ;
});

$(".fadein-2").inViewport(function(px){
    if(px) {
      $(".fadein-2").addClass("triggered");
    } ;
});

$(".fadein-3").inViewport(function(px){
    if(px) {
      $(".fadein-3").addClass("triggered");
    } ;
});
