let mockEvent = new Map();
mockEvent.set("author", "User 1");
mockEvent.set("title", "Farmer's Market");
mockEvent.set("date", "July 1 2020");
mockEvent.set("location", "Mountain View, California");
mockEvent.set("description", "details about event etc etc");

function postEvent(event) {
    const eventEl = document.createElement('div');
    eventEl.className = "event-post";
}

functiton addEventUserText(event) {
    const eventUser = document.createElement('p');
    eventUser.innerText = event.author + " posted an event:";
}