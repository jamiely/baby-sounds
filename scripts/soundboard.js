var SoundboardModule = function(root, $, regexFormat) {

  regexFormat = regexFormat || /\.mp3$/;

  function stripWav(item) {
    return item.
        replace(/sounds\//, '').
        replace(regexFormat, '');
  }

  function soundElementFilenameWithoutExt(soundElement) {
    return stripWav(soundElement.data('filename')).replace('2014/', '');
  }

  function isChineseSoundElement(soundEl) {
    return isChineseFilename(soundElementFilenameWithoutExt($(soundEl)));
  }

  function trans(fn) {
    return ({
      'duck': 'duck (pronounced ah)',
      'banana': 'banana (pronounced geng jieh)',
      'aunt': 'aunt (pronounced ea)',
      'leg': 'leg (pronounced kah)',
      'cat': 'cat (pronounced meow)',
      'puzzle': 'puzzle (she says "baga")',
      'baba_sit_here_chinese': 'daddy sit here (pronounced baba jaw here)',
      'dog_wo_wo': 'dog (she says "wo wo" like "woof woof")',
      'baaa': 'like a sheep',
      'fall_down_chinese': 'fell down (pronounced bwah oh)',
      'fly_chinese': 'fly, as in the insect (pronounced ho seng)',
      'good_boy_maybe': 'we\'re not sure what this is, but we think it is "good boy"',
      'im_not_a_baby_im_leona': 'I\'m Not a Baby, I\'m Leona',
      'milk_chinese': 'milk (pronounced ni ni)',
      'mommy_sit_chair': 'mommy sit on the chair (pronounced mommy jaw ehn)',
      'mommy_sing_chinese': 'mommy, sing (pronounced mommy chang gaw)',
      'orange_chinese': 'orange (pronounced ga)',
      'no_no_no_chinese': 'no, no, no! (pronounced mi mi mi)'
    }[fn]) || fn;
  }

  function isChineseFilename(filename) {
    return ['duck', 'banana', 'aunt', 'leg', 'cat',
      'baba_sit_here_chinese', 'catch_mommy_chinese',
      'dog_wo_wo', 'fall_down_chinese', 'fly_chinese',
      'milk_chinese', 'mommy_sit_chair_chinese',
      'mommy_sing_chinese', 'orange_chinese', 'no_no_no_chinese'
    ].indexOf(stripWav(filename)) != -1;
  }

  function pretty(str) {
    return str.
      replace('2014/', '').
      replace(/^\w/, function(a) { return a.toUpperCase(); }).
      replace(/_(\w)/g, function(all, letter){return ' ' + letter.toUpperCase()});
  }

  function shorten(str) {
    return str.substr(0, 15);
  }

  function simplify(fn) {
    return {
      'baba_sit_here_chinese': 'baba_sit',
      'baba_sit_here2': 'baba_sit_here',
      'catch_mommy_chinese': 'catch_mommy',
      'cock_a_doodle_doo': 'cock_a_doodle',
      'dog_wo_wo': 'dog',
      'duck_english': 'duck',
      'fall_down_chinese': 'fell_down',
      'fly_chinese': 'Fly (Insect)',
      'good_boy_maybe': 'Good boy?',
      'i_cant_reach_it': 'I Can\'t Reach',
      'im_not_a_baby_im_leona': 'I\'m Not a Baby',
      'hurray_i_say_yay': 'Hurray!',
      'milk_chinese': 'milk',
      'mommy_sit_chair_chinese': 'mommy_sit',
      'mommy_sing_chinese': 'Mommy, Sing',
      'orange_chinese': 'Orange',
      'no_no_no_chinese': 'No No No!',
      'thats_dog': 'That\'s Dog',
      'where_are_you': 'Where Are You?',
      'minni2': 'Minnie B',
      'minnie': 'Minnie A',
      'laugh_mama': '(Laugh) Mama A',
      'laugh_mama2': '(Laugh) Mama B',
      'no_way': 'No Way A',
      'no_way1': 'No Way B',
      'oh_man': 'Oh Man!',
      'no_more': 'No More A',
      'no_more1': 'No More B'
    }[fn] || fn;
  }

  function soundElementToTouchElement(soundElement) {
    var audioElement = $(soundElement.children('audio').first()).clone().hide();
    var filename = soundElementFilenameWithoutExt(soundElement);

    var title = "Click to play " + pretty(trans(filename));

    return $('<div />').
      addClass('touch-sound').
      html(shorten(pretty(simplify(filename)))).
      attr('title', title).
      append(audioElement).
      click(function() {
        audioElement.each(function(_, el) {
          el.load();
          el.play();
        });
      });
  }

  function soundElements() {
    return $('.sounds > .sound');
  }

  function soundElementLang(soundElement) {
    return isChineseSoundElement(soundElement) ? 'chinese' : 'english';
  }

  function heading(content) {
    return $('<h1 />').html(content);
  }

  function embedTouchElements(soundboardEl) {
    soundElements().each(function (_, soundEl) {
      var el = soundElementToTouchElement($(soundEl)).
        addClass("lang-" + soundElementLang(soundEl));
      var year = $(soundEl).data('year');
      if(year) {
        el.addClass('year-' + year);
      }
      // If there is a specific container for the year,
      // then the elements get put inside there.
      var appendEl = 
        soundboardEl.find('.year-container.year-container-' + year) ||
        soundboardEl;
      appendEl.append(el);
    });
  }

  return function() {
    this.embed = function(el) {
      embedTouchElements($(el));
    }
  };
}

