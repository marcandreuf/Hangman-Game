
// GLOBAL CONSTANTS
var CONFIG_LOCALSTORAGE_CURRENTSTATE_KEY = "hangmanGameState";

// Global variables
var canvas = document.getElementById('stage');
var word = document.getElementById('word');
var letters = document.getElementById('letters');
var game;




function init() {
    var helptext = $('#helptext');
    var w = screen.availWidth <= 800 ? screen.availWidth : 800;
	
    // Hide the loading message and display the control buttons
    $('#loading').hide();
    $('#play').css('display', 'inline-block').click(newGame);
    $('#clear').css('display', 'inline-block').click(resetScore);
    
    // Help button
    $('#help').click(function(e) {
       $('body').append('<div id="mask"></div>');
       helptext.show().css('margin-left', (w-300)/2 + 'px');
    });
    
    $('#close').click(function(e) {
        $('#mask').remove();
        helptext.hide();
    });
    
    // Rescale the canvas if the screen is wider than 700px
    if (screen.innerWidth >= 700) {
        canvas.getContext('2d').scale(1.5, 1.5);
    }
    
    createNewGameObject();    
    if (localStorage.getItem(CONFIG_LOCALSTORAGE_CURRENTSTATE_KEY) !== null) {
        var currentState = localStorage.getItem(CONFIG_LOCALSTORAGE_CURRENTSTATE_KEY);
        var storedGame = JSON.parse(currentState);
        game.id = storedGame.id;
        game.wordToGuess = storedGame.wordToGuess;
        game.attempts = storedGame.attempts;
        game.state = storedGame.state;
        game.score.win = storedGame.score.win;
        game.score.lost = storedGame.score.lost;
        showScore();
    }else{
        showScore();
    }
}

function createNewGameObject() {   
    game = new Object();
    game.id="";
    game.wordToGuess = "";
    game.attempts="";
    game.state="";
    game.score = new Object();
    game.score.win=0;
    game.score.lost=0;
    game.wordLength = function (){
        return this.wordToGuess.length;
    };
    game.badGuesses = function (){
        var count = 0;
        var currentChar;
        for(var i=0; i<this.attempts.length; i++){
            currentChar = this.attempts[i].toLocaleLowerCase();
            if(this.wordToGuess.indexOf(currentChar)===-1){
                count++;
                
            }
        }
        return count;
    };
    game.correctGuesses = function () {
        var count = 0;
        var currentChar;
        for(var i=0; i<this.attempts.length; i++){
            currentChar = this.attempts[i].toLocaleLowerCase();
            count = count + this.countOccurances(this.wordToGuess, currentChar, 0);
        }
        return count;
    };
    game.countOccurances = function (string, char, from) {
        var countChar=0;
        var occurance=0;
        if(from < string.length){
            occurance = string.indexOf(char, from);
            if(occurance !== -1){ 
                countChar = 1 + this.countOccurances(string, char, occurance+1);
            }
        }
        return countChar;
    };
}

function newGame() {
    var placeholders = '';
    var frag = document.createDocumentFragment();
    var abc = ['A','B','C','D','E','F','G','H','I','J','K','L','M',
           'N','O','P','Q','R','S','T','U','V','W','X','Y','Z'];
    
    newGameInstance();
    saveLocalState();
    
    // create row of underscores the same length as letters to guess
    for (var i = 0; i < game.wordLength(); i++) {
        placeholders += '_';
    }
    word.innerHTML = placeholders;
    
    // create an alphabet pad to select letters
    letters.innerHTML = '';
    for (i = 0; i < 26; i++) {
        var div = document.createElement('div');
        div.style.cursor = 'pointer';
        div.innerHTML = abc[i];
        div.onclick = getLetter;
        frag.appendChild(div);
    }
    letters.appendChild(frag);
    drawCanvas();
    window.scrollBy(0, 200);
}

function newGameInstance(){
    game.id = "";
    game.wordToGuess = getWord();
    game.attempts = "";
    game.state = "new";
    
    $.ajax({
        url: "rest/games/game",
        async: false,
        contentType: 'application/json; charset=utf-8',
        type: 'POST',
        data: JSON.stringify(game),
        success: function(data, textStatus, jqXHR) {
           game.id = data.id;
        }
    });
}

function updateRemoteState(){
    $.ajax({
        url: "rest/games/game",
        contentType: 'application/json; charset=utf-8',
        type: 'PUT',
        data: JSON.stringify(game)
    });
}

function saveLocalState(){
    localStorage.setItem(CONFIG_LOCALSTORAGE_CURRENTSTATE_KEY, JSON.stringify(game) );
}

// Get selected letter and remove it from the alphabet pad
function getLetter() {
    game.state = "playing";
    game.attempts = game.attempts.concat(this.innerHTML.toLocaleLowerCase());
    checkLetter(this.innerHTML);
    this.innerHTML = '&nbsp;';
    this.style.cursor = 'default';
    this.onclick = null;
}

// Check whether selected letter is in the word to be guessed
function checkLetter(letter) {
    var placeholders = word.innerHTML;
    var wrongGuess = true;
    // split the placeholders into an array
   placeholders = placeholders.split('');
   // loop through the array
   for (var i = 0; i < game.wordLength(); i++) {
      // if the selected letter matches one in the word to guess,
      // replace the underscore and increase the number of correct guesses
      if (game.wordToGuess.charAt(i) === letter.toLowerCase()) {
         placeholders[i] = letter;
         wrongGuess = false;
      }
   }

    if (wrongGuess) {
      drawCanvas();
      saveLocalState();
      updateRemoteState();
   }else{
       if (game.correctGuesses() === game.wordLength()) {
          drawCanvas();
       }
       saveLocalState()
       updateRemoteState();
   }

    word.innerHTML = placeholders.join('');
}

// Draw the canvas
function drawCanvas() {
    var c = canvas.getContext('2d');
    // reset the canvas and set basic styles
    canvas.width = canvas.width;
    c.lineWidth = 10;
    c.strokeStyle = 'green';
    c.font = 'bold 24px Optimer, Arial, Helvetica, sans-serif';
    c.fillStyle = 'red';
    // draw the ground
    drawLine(c, [20,190], [180,190]);
    // start building the gallows if there's been a bad guess
    if(game.badGuesses() > 0) {
        // create the upright
        c.strokeStyle = '#A52A2A';
        drawLine(c, [30,185], [30,10]);
        if (game.badGuesses() > 1) {
            // create the arm of the gallows
            c.lineTo(150,10);
            c.stroke();
        }
        if (game.badGuesses() > 2) {
            c.strokeStyle = 'black';
            c.lineWidth = 3;
            // draw rope
            drawLine(c, [145,15], [145,30]);
            // draw head
            c.beginPath();
            c.moveTo(160, 45);
            c.arc(145, 45, 15, 0, (Math.PI/180)*360);
            c.stroke(); 
        }
        if (game.badGuesses() > 3) {
            // draw body
            drawLine(c, [145,60], [145,130]);
        }
        if (game.badGuesses() > 4) {
            // draw left arm
            drawLine(c, [145,80], [110,90]);
        }
        if (game.badGuesses() > 5) {
            // draw right arm
            drawLine(c, [145,80], [180,90]);
        }
        if (game.badGuesses() > 6) {
            // draw left leg
            drawLine(c, [145,130], [130,170]);
        }
        if (game.badGuesses() > 7) {
            game.state = "lost";
            // draw right leg and end game
            drawLine(c, [145,130], [160,170]);
            c.fillText('Game over', 45, 110);
            // remove the alphabet pad
            letters.innerHTML = '';
            game.score.lost++;
            setTimeout(showResult, 2000);
        }
    }
    // if the word has been guessed correctly, display message,
    // update score of games won, and then show score after 2 seconds
    if (game.correctGuesses() === game.wordLength()) {
        game.state = "win";
        letters.innerHTML = '';
        c.fillText('You won!', 45,110);
        game.score.win++;
        setTimeout(showResult, 2000);
    }  
   
}

function drawLine(context, from, to) {
    context.beginPath();
    context.moveTo(from[0], from[1]);
    context.lineTo(to[0], to[1]);
    context.stroke();
}

// When the game is over, display missing letters in red
function showResult() {
    var placeholders = word.innerHTML;
    placeholders = placeholders.split('');
    for (i = 0; i < game.wordLength(); i++) {
        if (placeholders[i] === '_') {
        placeholders[i] = '<span style="color:red">' + 
             game.wordToGuess.charAt(i).toUpperCase() + '</span>';
        }
    }
    word.innerHTML = placeholders.join('');
    setTimeout(showScore,200);
}

// Display the score in the canvas
function showScore() {
    var won = game.score.win;
    var lost = game.score.lost;
    var c = canvas.getContext('2d');
    // clear the canvas
    canvas.width = canvas.width;
    c.font = 'bold 24px Optimer, Arial, Helvetica, sans-serif';
    c.fillStyle = 'red';
    c.textAlign = 'center';
    c.fillText('YOUR SCORE', 100, 50);
    c.font = 'bold 18px Optimer, Arial, Helvetica, sans-serif';
    c.fillText('Won: ' + won + ' Lost: ' + lost, 100, 80);
}


function resetScore() {
    word.innerHTML = "";
    localStorage.removeItem(CONFIG_LOCALSTORAGE_CURRENTSTATE_KEY);
    game.state = "deleted";
    updateRemoteState();
    setTimeout(init,200);
}

// Select random word to guess
function getWord() {
  //Todo, put the diccionary in another file and load the array split by coma.
  var a = new Array('abate','aberrant','abscond','accolade','acerbic','acumen','adulation','adulterate','aesthetic','aggrandize','alacrity','alchemy','amalgamate','ameliorate','amenable','anachronism','anomaly','approbation','archaic','arduous','ascetic','assuage','astringent','audacious','austere','avarice','aver','axiom','bolster','bombast','bombastic','bucolic','burgeon','cacophony','canon','canonical','capricious','castigation','catalyst','caustic','censure','chary','chicanery','cogent','complaisance','connoisseur','contentious','contrite','convention','convoluted','credulous','culpable','cynicism','dearth','decorum','demur','derision','desiccate','diatribe','didactic','dilettante','disabuse','discordant','discretion','disinterested','disparage','disparate','dissemble','divulge','dogmatic','ebullience','eccentric','eclectic','effrontery','elegy','eloquent','emollient','empirical','endemic','enervate','enigmatic','ennui','ephemeral','equivocate','erudite','esoteric','eulogy','evanescent','exacerbate','exculpate','exigent','exonerate','extemporaneous','facetious','fallacy','fawn','fervent','filibuster','flout','fortuitous','fulminate','furtive','garrulous','germane','glib','grandiloquence','gregarious','hackneyed','halcyon','harangue','hedonism','hegemony','heretical','hubris','hyperbole','iconoclast','idolatrous','imminent','immutable','impassive','impecunious','imperturbable','impetuous','implacable','impunity','inchoate','incipient','indifferent','inert','infelicitous','ingenuous','inimical','innocuous','insipid','intractable','intransigent','intrepid','inured','inveigle','irascible','laconic','laud','loquacious','lucid','luminous','magnanimity','malevolent','malleable','martial','maverick','mendacity','mercurial','meticulous','misanthrope','mitigate','mollify','morose','mundane','nebulous','neologism','neophyte','noxious','obdurate','obfuscate','obsequious','obstinate','obtuse','obviate','occlude','odious','onerous','opaque','opprobrium','oscillation','ostentatious','paean','parody','pedagogy','pedantic','penurious','penury','perennial','perfidy','perfunctory','pernicious','perspicacious','peruse','pervade','pervasive','phlegmatic','pine','pious','pirate','pith','pithy','placate','platitude','plethora','plummet','polemical','pragmatic','prattle','precipitate','precursor','predilection','preen','prescience','presumptuous','prevaricate','pristine','probity','proclivity','prodigal','prodigious','profligate','profuse','proliferate','prolific','propensity','prosaic','pungent','putrefy','quaff','qualm','querulous','query','quiescence','quixotic','quotidian','rancorous','rarefy','recalcitrant','recant','recondite','redoubtable','refulgent','refute','relegate','renege','repudiate','rescind','reticent','reverent','rhetoric','salubrious','sanction','satire','sedulous','shard','solicitous','solvent','soporific','sordid','sparse','specious','spendthrift','sporadic','spurious','squalid','squander','static','stoic','stupefy','stymie','subpoena','subtle','succinct','superfluous','supplant','surfeit','synthesis','tacit','tenacity','terse','tirade','torpid','torque','tortuous','tout','transient','trenchant','truculent','ubiquitous','unfeigned','untenable','urbane','vacillate','variegated','veracity','vexation','vigilant','vilify','virulent','viscous','vituperate','volatile','voracious','waver','zealous');
  return a[parseInt(Math.random()* a.length)];
}