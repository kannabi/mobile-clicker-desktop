var stompClient = null;
var presentationInfo = null;

function connect() {
    var socket = new SockJS('/listener');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/'+ id + '/' + userId, function (greeting) {
            console.log(greeting);
        });
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    console.log("Disconnected");
}

function startPresentation() {
    $.post("http://localhost:8080/start_presentation", {'filePath': 'C:\\Users\\hekpo\\Downloads\\Presentation_SchukinVV.pdf'})
        .done(function (e) {
            presentationInfo = e;
            console.log(e);
            console.log('It was presentation info');
            // var image = new Image();
            // image.src =
            // document.body.appendChild(image);
            $('#slide_img').attr('src', 'data:image/png;base64,' + e.firstPage.imageBase64String);
        })
        .fail(function (error) {
            console.log("Error");
            console.log(error);
        });
}

function bye() {
    $.post("http://localhost:8080/bye", {})
        .done(function (e) {
            console.log(e);
            // id = e.translationId;
        })
        .fail(function (error) {
            console.log("Error");
            console.log(error);
        });
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#start" ).click(function() { startPresentation(); });

});

window.onload = function() {
    window.addEventListener("beforeunload", function (e) {
        bye();
    });
};