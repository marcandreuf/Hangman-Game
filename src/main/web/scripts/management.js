function initManagement(){   
    $('#btnPlaying').click(function(){
        loadDashboard('playing');
    });
    $('#btnAll').click(function(){
        loadDashboard('all');
    });
}


function loadDashboard(state) {
    
    $.ajax({
        url: "rest/games/query?state="+state,
        async: false,
        contentType: 'application/json; charset=utf-8',
        type: 'GET',
        success: function(data, textStatus, jqXHR) {
            
            $('#gamesTable > tbody').empty();
            
            if(data.length !== ''){
                var jsonGame;
                var $tbody = $('<tbody>').appendTo('#gamesTable');
                $.each(data.list, function(i, row) {
                   jsonGame = JSON.parse(row);
                    $('<tr>').attr('id', i)
                            .append($('<td>').text(jsonGame.id))
                            .append($('<td>').text(jsonGame.wordToGuess))
                            .append($('<td>').text(jsonGame.attempts))
                            .append($('<td>').text(jsonGame.state))
                            .appendTo($tbody); 
                });
            }
        }
    });
}