// will be pulled from backend
let mockEvent = new Map();
mockEvent.set("author", "User A");
mockEvent.set("title", "Farmer's Market");
mockEvent.set("date", "July 1 2020");
mockEvent.set("location", "Mountain View, California");
mockEvent.set("description", "details about event etc etc");
mockEvent.set("category", "food/drink");
mockEvent.set("bookmarks", 10);

let mockEvent2 = new Map();
mockEvent2.set("author", "User B");
mockEvent2.set("title", "World Rainforest Day");
mockEvent2.set("date", "July 1 2020");
mockEvent2.set("location", "Mountain View, California");
mockEvent2.set("description", "details about event etc etc");
mockEvent2.set("category", "nature");
mockEvent2.set("bookmarks", 25);

let mockCurrentUserChallenge = new Map();
mockCurrentUserChallenge.set("id", 0);
mockCurrentUserChallenge.set("step", 2);

// will move to backend eventually, will make challenge & step classes
let mockChallenges = [];
mockChallenges[0] = new Map();
mockChallenges[0].set("title", "gardening");
mockChallenges[0].set("icon", "🌱");
let mockGardeningSteps = ["Plant tomatoes", "Plant a tree", "Save rainwater"];
let mockGardeningStepDescriptions = ["Start a garden in your backyard",
"More trees is always good", "Reuse rainwater for your plants"];
mockChallenges[0].set("steps", mockGardeningSteps);
mockChallenges[0].set("stepDescriptions", mockGardeningStepDescriptions);

let eventCategoryIcons = new Map();
eventCategoryIcons.set("food/drink", "🥑🍋🍏");
eventCategoryIcons.set("nature", "🌲🌱🌳");

function loadPage() {
    const feed = document.getElementById("events-feed");
    feed.appendChild(postEvent(mockEvent));
    feed.appendChild(postEvent(mockEvent2));

    setChallenge(mockCurrentUserChallenge);
}

function postEvent(event) {
    const eventEl = document.createElement('div');
    eventEl.className = "event-post";
    eventEl.appendChild(addEventUserText(event));
    eventEl.appendChild(addEventBookmark(event));
    eventEl.appendChild(addEventMiddleSection(event));
    eventEl.appendChild(addEventInfo(event));
    return eventEl;
}

function addEventBookmark(event) {
    const bookmarkDiv = document.createElement('div');
    bookmarkDiv.style.height = 0;
    const bookmark = document.createElement('img');
    bookmark.className = "event-bookmark";
    // add case for if user has bookmarked this event once users have been created
    bookmark.src = "../resources/bookmark.png";
    bookmarkDiv.appendChild(bookmark);
    bookmarkDiv.appendChild(addEventNumBookmarks(event));
    return bookmarkDiv;
}

function addEventNumBookmarks(event) {
    const bookmarkNum = document.createElement('p');
    bookmarkNum.className = "event-bookmark-num";
    bookmarkNum.innerText = event.get("bookmarks");
    return bookmarkNum;
}

function addEventUserText(event) {
    const eventUser = document.createElement('p');
    eventUser.className = "event-info";
    eventUser.innerText = event.get("author") + " posted an event:";
    return eventUser;
}

function addEventMiddleSection(event) {
    const eventMiddle = document.createElement('div');
    eventMiddle.className = "event-middle";
    eventMiddle.appendChild(addEventInnerCard(event));
    eventMiddle.appendChild(addEventDescription(event));
    return eventMiddle;
}

function addEventInnerCard(event) {
    const eventIcon = document.createElement('div');
    eventIcon.className = "event-icon";
    eventIcon.innerText = eventCategoryIcons.get(event.get("category"));

    const eventTitle = document.createElement('p');
    eventTitle.className = "event-title";
    eventTitle.innerText = event.get("title");

    const eventInnerCard = document.createElement('div');
    eventInnerCard.className = "event-inner-card";
    eventInnerCard.appendChild(eventIcon);
    eventInnerCard.appendChild(eventTitle);
    return eventInnerCard;
}

function addEventDescription(event) {
    const eventDescription = document.createElement("p");
    eventDescription.className = "event-description";
    eventDescription.innerText = "description:\n\n" + event.get("description");
    return eventDescription;
}

function addEventInfo(event) {
    const eventInfo = document.createElement("div");
    eventInfo.className = "event-info";

    const eventLocation = document.createElement("p");
    eventLocation.innerHTML =  "📍&nbsp&nbsp";
    eventLocation.innerText += event.get("location");

    const eventDate = document.createElement("p");
    eventDate.innerHTML =  "📅&nbsp&nbsp";
    eventDate.innerText += event.get("date");

    eventInfo.appendChild(eventLocation);
    eventInfo.appendChild(eventDate);
    return eventInfo;
}

function setChallenge(challenge) {
    const badge = document.getElementById("challenges-badge");
    const icon = document.getElementById("challenges-badge-icon");
    icon.innerText = mockChallenges[challenge.get("id")].get("icon");

    const currentStep = challenge.get("step");
    const totalSteps = mockChallenges[challenge.get("id")].get("steps").length;

    const stepsText = document.getElementById("challenge-steps");
    stepsText.innerText = currentStep + "/" + totalSteps;

    const badgeFilling = document.createElement("div");
    badgeFilling.className = "badge-filling";
    badgeFilling.style.height = 120*(currentStep/totalSteps) + "px";
    badgeFilling.style.bottom = 120*(currentStep/totalSteps) + "px";
    badge.appendChild(badgeFilling);
}