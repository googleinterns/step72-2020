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
mockChallenges[0].set("id", 0);
mockChallenges[0].set("title", "Gardening");
mockChallenges[0].set("icon", "🌱");
let mockGardeningSteps = ["Plant tomatoes", "Plant a tree", "Save rainwater"];
let mockGardeningStepDescriptions = ["Start a garden in your backyard",
"More trees are always good", "Reuse rainwater for your plants"];
let mockGardeningStepResources = ["Here is a link for more information",
"Here is a link for more information", "Here is a link for more information"];
mockChallenges[0].set("steps", mockGardeningSteps);
mockChallenges[0].set("descriptions", mockGardeningStepDescriptions);
mockChallenges[0].set("resources", mockGardeningStepResources);

mockChallenges[1] = new Map();
mockChallenges[1].set("id", 1);
mockChallenges[1].set("title", "Recycling");
mockChallenges[1].set("icon", "♻️");
let mockRecyclingSteps = ["Reuse", "Reduce","Recycle"];
let mockRecyclingStepDescriptions = ["Description for step 1",
"Description for step 2", "Description for step 3"];
let mockRecyclingStepResources = ["Here is a link for more information",
"Here is a link for more information", "Here is a link for more information"];
mockChallenges[1].set("steps", mockRecyclingSteps);
mockChallenges[1].set("descriptions", mockRecyclingStepDescriptions);
mockChallenges[1].set("resources", mockRecyclingStepResources);

mockChallenges[2] = new Map();
mockChallenges[2].set("id", 2);
mockChallenges[2].set("title", "Food");
mockChallenges[2].set("icon", "🥑");
let mockFoodSteps = ["Food step 1", "Food step 2", "Food step 3"];
let mockFoodStepDescriptions = ["food description 1",
"food description 2", "food description 3"];
let mockFoodStepResources = ["Here is a link for more information",
"Here is a link for more information", "Here is a link for more information"];
mockChallenges[2].set("steps", mockFoodSteps);
mockChallenges[2].set("descriptions", mockFoodStepDescriptions);
mockChallenges[2].set("resources", mockFoodStepResources);

let mockUser = new Map();
mockUser.set("name", "User A");
mockUser.set("currentChallengeId", 1);
// array where indices correspond to challenge id and value = how many steps have been completed
mockUser.set("challengeStatuses", [1, 0, 2]);

let eventCategoryIcons = new Map();
eventCategoryIcons.set("food/drink", "🥑🍋🍏");
eventCategoryIcons.set("nature", "🌲🌱🌳");

function loadPage() {
    const feed = document.getElementById("events-feed");
    feed.appendChild(postEvent(mockEvent));
    feed.appendChild(postEvent(mockEvent2));

    setChallengeBox(mockCurrentUserChallenge);

    setChallengesNavBar(mockUser, mockChallenges);
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

function setChallengeBox(challenge) {
    const badge = document.getElementById("challenges-badge");
    const icon = document.getElementById("challenges-badge-icon");
    icon.innerText = mockChallenges[challenge.get("id")].get("icon");

    const currentStep = challenge.get("step");
    const totalSteps = mockChallenges[challenge.get("id")].get("steps").length;

    const stepsText = document.getElementById("challenge-steps");
    stepsText.innerText = currentStep + "/" + totalSteps;

    badge.appendChild(fillBadge(currentStep, totalSteps));
}

function fillBadge(currentStep, totalSteps) {
    const badgeFilling = document.createElement("div");
    badgeFilling.className = "badge-filling";
    badgeFilling.style.height = 120*(currentStep/totalSteps) + "px";
    badgeFilling.style.bottom = 120*(currentStep/totalSteps) + "px";
    if (currentStep/totalSteps == 1) {
        badgeFilling.style.borderRadius = "10px 10px 10px 10px";
    }
    else {
        badgeFilling.style.borderRadius = "0 0 10px 10px";
    }
    return badgeFilling;
}

function setChallengesNavBar(user, challenges) {
    const navBar = document.getElementById("challenges-nav-bar");
    for (challenge of challenges) {
        navBar.appendChild(createChallengeNavBarItem(user, challenge));

        const itemBackground = document.createElement('div');
        itemBackground.className = "challenges-nav-bar-item-background";
        let percentDone = user.get("challengeStatuses")[challenge.get("id")] / challenge.get("steps").length;
        console.log("percent done" + percentDone + challenge.get("title"));
        itemBackground.style.width = percentDone*100+"%";
        navBar.appendChild(itemBackground);
    }
}

function createChallengeNavBarItem(user, challenge) {
    const item = document.createElement('div');
    item.className = "challenges-nav-bar-item";

    const title = document.createElement('p');
    title.className = "challenges-nav-bar-item-title";
    title.innerText = challenge.get("title");
    item.appendChild(title);

    const icon = document.createElement('p');
    icon.className = "challenges-nav-bar-item-icon";
    icon.innerText = challenge.get("icon");
    item.appendChild(icon);

    item.addEventListener("click", () => {
        boldCurrentChallengeTitle(item);
        showChallengeInfo(user, challenge);
    });
    return item;
}

function boldCurrentChallengeTitle(chosenItem) {
    const items = document.getElementsByClassName("challenges-nav-bar-item");
    for (item of items) {
        item.style.fontWeight = "normal";
    }
    chosenItem.style.fontWeight = "bold";
}

function showChallengeInfo(user, challenge) {
    console.log("clicked " + challenge.get("title"));
    const header = document.getElementById("challenges-main-panel-header");
    let displayedStep = Math.min(user.get("challengeStatuses")[challenge.get("id")]+1, challenge.get("steps").length);
    let stepsText = displayedStep + "/" + challenge.get("steps").length;
    header.innerText = challenge.get("icon") + " " + challenge.get("title") + " " + stepsText;

    const step = document.getElementById("challenges-main-panel-step");
    step.innerText = challenge.get("steps")[displayedStep-1];

    const description = document.getElementById("challenges-main-panel-description");
    description.innerText = challenge.get("descriptions")[displayedStep-1];

    const resources = document.getElementById("challenges-main-panel-resources");
    resources.innerText = challenge.get("resources")[displayedStep-1];

    createModalChallengesBadge(displayedStep, challenge);
}

function createModalChallengesBadge(currentStep, challenge) {
    const badge = document.getElementById("modal-challenges-badge");
    const icon = document.getElementById("modal-challenges-badge-icon");
    icon.innerText = challenge.get("icon");
    const totalSteps = challenge.get("steps").length;
    const badgeFilling = document.getElementById("modal-badge-filling");
    badgeFilling.style.height = 120*(currentStep/totalSteps) + "px";
    badgeFilling.style.bottom = 120*(currentStep/totalSteps) + "px";
    if (currentStep/totalSteps == 1) {
        badgeFilling.style.borderRadius = "10px 10px 10px 10px";
    }
    else {
        badgeFilling.style.borderRadius = "0 0 10px 10px";
    }
    badge.appendChild(badgeFilling);
}