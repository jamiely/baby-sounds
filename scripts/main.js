var SoundboardModule = function(root, $) {

  function stripWav(item) {
    return item.replace(/\.wav$/, '');
  }

  function soundElementFilenameWithoutExt(soundElement) {
    return stripWav(soundElement.data('filename')); 
  }

  function isChineseSoundElement(soundEl) {
    return isChineseFilename(soundElementFilenameWithoutExt($(soundEl)));
  }

  function trans(fn) {
    return ({
      'duck': 'duck (pronounced ah)',
      'banana': 'banana (pronounced geng jieh)',
      'aunt': 'aunt (pronounced ea)',
      'leg': 'kah',
      'cat': 'meow'
    }[fn]) || fn;
  }

  function isChineseFilename(filename) {
    return ['duck', 'banana', 'aunt', 'leg', 'cat'].indexOf(stripWav(filename)) != -1;
  }

  function pretty(str) {
    return str.
      replace(/^\w/, function(a) { return a.toUpperCase(); }).
      replace(/_(\w)/g, function(all, letter){return ' ' + letter.toUpperCase()});
  }

  function soundElementToTouchElement(soundElement) {
    var audioElement = $(soundElement.children('audio').first());
    var filename = soundElementFilenameWithoutExt(soundElement);

    var title = "Click to play " + pretty(trans(filename));

    return $('<div />').
      addClass('touch-sound').
      html(pretty(filename)).
      attr('title', title).
      click(function() {
        audioElement.each(function(_, el) {
          el.play();
        });
      });
  }

  function soundElementsByLangauge() {
    var all = $('.sounds > .sound');
    // better as partition.
    return {
      chinese: all.filter(function() {
        return isChineseSoundElement(this)
      }),
      english: all.filter(function() {
        return !isChineseSoundElement(this);
      })
    };
  }

  function heading(content) {
    return $('<h1 />').html(content);
  }

  function embedTouchElements(soundboardEl) {
    var byLang = soundElementsByLangauge();
    for(var lang in byLang) {
      soundboardEl.append(heading(lang))
      byLang[lang].each(function (_, soundEl) {
        soundboardEl.append(soundElementToTouchElement($(soundEl)));
      });
    }
  }

  return function() {
    this.embed = function(el) {
      embedTouchElements($(el));
    }
  };
}

$(function() {
  var Soundboard = SoundboardModule(window, jQuery);
  var soundboard = new Soundboard();

  soundboard.embed('#soundboard')
});
