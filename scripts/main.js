$(function() {
  var Soundboard = SoundboardModule(window, jQuery, /\.mp3$/);
  var soundboard = new Soundboard();

  soundboard.embed('#soundboard')
});
