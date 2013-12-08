$(function() {
  var Soundboard = SoundboardModule(window, jQuery, /\.wav$/);
  var soundboard = new Soundboard();

  soundboard.embed('#soundboard')
});
