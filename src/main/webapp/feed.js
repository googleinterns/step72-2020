// will move to backend eventually, will make challenge & step classes
function createMockChallenges() {
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
    let mockRecyclingSteps = ["Use a reusable bag for groceries", "Reduce","Recycle"];
    let mockRecyclingStepDescriptions = ["(More information about why this helps the environment)",
    "Description for step 2", "Description for step 3"];
    let mockRecyclingStepResources = ["(External resources about recycling related environmental issues)",
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
    
    return mockChallenges;
}


let user;
let challenges;

let eventCategoryIcons = new Map();
eventCategoryIcons.set("food_beverage", "🥑🍋🍏");
eventCategoryIcons.set("nature", "🌲🌱🌳");
eventCategoryIcons.set("water", "🌊🐳​🌊​");
eventCategoryIcons.set("waste_cleanup", "🗑♻️🥤");
eventCategoryIcons.set("other", "🥑🌲🐢");

const badgeHeight = 120;
let lastBoldedItem;

async function loadPage() {
    await checkLoginStatus(); 

    challenges = createMockChallenges();
    
    const timezone = document.getElementById("user-timezone");
    timezone.value = new Date().getTimezoneOffset();
    const events = await fetch("/events").then(response => response.json());

    const feed = document.getElementById("events-feed");
    for (event of events) {
        feed.appendChild(postEvent(event));
    }

    setChallengeBox(user.current_challenge_id);

    setChallengesNavBar(challenges);

    window.onclick = function(event) {
        const challengesModal = document.getElementById("challenges-modal");
        const createEventModal = document.getElementById("create-event-modal");
        if (event.target == challengesModal) {
            closeChallengesModal();
        }
        else if (event.target == createEventModal) {
            closeCreateEventModal();
        }
    }
}

function postEvent(event) {
    const eventEl = document.createElement('div');
    eventEl.className = "event-post";
    eventEl.appendChild(addEventUserText(event));
    // eventEl.appendChild(addEventBookmark(event));
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
    bookmark.src = "/resources/bookmark.png";
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
    // eventUser.innerText = event.creator + " posted an event:";
    eventUser.innerText = "USER" + " posted an event:";

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
    eventIcon.innerText = eventCategoryIcons.get(event.extendedProperties.category);

    const eventTitle = document.createElement('p');
    eventTitle.className = "event-title";
    eventTitle.innerText = event.summary;

    const eventInnerCard = document.createElement('div');
    eventInnerCard.className = "event-inner-card";
    eventInnerCard.appendChild(eventIcon);
    eventInnerCard.appendChild(eventTitle);
    return eventInnerCard;
}

function addEventDescription(event) {
    const eventDescription = document.createElement("p");
    eventDescription.className = "event-description";
    eventDescription.innerText = "description:\n\n" + event.description;
    return eventDescription;
}

function addEventInfo(event) {
    const eventInfo = document.createElement("div");
    eventInfo.className = "event-info";

    const eventLocation = document.createElement("p");
    eventLocation.innerHTML =  "📍&nbsp&nbsp";
    eventLocation.innerText += event.location;

    const eventDate = document.createElement("p");
    eventDate.innerHTML =  "📅&nbsp&nbsp";

    const dateTimeOptions = { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' };
    const timeOptions = { hour: '2-digit', minute: '2-digit' };

    const dateTime = new Date(event.start.dateTime.value).toLocaleString([], dateTimeOptions);
    const endTime = new Date(event.end.dateTime.value).toLocaleTimeString([], timeOptions);
    eventDate.innerText += dateTime + " - " + endTime;

    eventInfo.appendChild(eventLocation);
    eventInfo.appendChild(eventDate);
    return eventInfo;
}

function setChallengeBox(challengeId) {
    const icon = document.getElementById("challenges-badge-icon");
    const stepsText = document.getElementById("challenge-steps");

    if (challengeId == -1) {
        icon.innerText = "✔️";
        stepsText.style.fontSize = "16px";
        stepsText.innerText = "Complete!";
        fillBadge(1, 1);
    }
    else {
        icon.innerText = challenges[challengeId].get("icon");
        
        const currentStep = user.challenge_statuses[challengeId];
        const totalSteps = challenges[challengeId].get("steps").length;
        
        stepsText.style.fontSize = "20px";
        stepsText.innerText = currentStep + "/" + totalSteps;
        
        fillBadge(currentStep, totalSteps);
    }
}

function fillBadge(currentStep, totalSteps) {
    const badgeFilling = document.getElementById("feed-badge-filling")
    badgeFilling.style.height = badgeHeight*(currentStep/totalSteps) + "px";
    let offset = 1.5;
    if (currentStep/totalSteps == 1) offset = -1.5;
    badgeFilling.style.bottom = badgeHeight*(currentStep/totalSteps) + offset + "px";

    if (currentStep/totalSteps == 1) {
        badgeFilling.style.borderRadius = "10px 10px 10px 10px";
    }
    else {
        badgeFilling.style.borderRadius = "0 0 10px 10px";
    }
}

function setChallengesNavBar(challenges) {
    const navBar = document.getElementById("challenges-nav-bar");
    for (challenge of challenges) {
        navBar.appendChild(createChallengeNavBarItem(challenge));

        const itemBackground = document.createElement('div');
        itemBackground.id = "challenges-nav-bar-item-background-" + challenge.get("id");
        itemBackground.className = "challenges-nav-bar-item-background";

        let percentDone = 0;
        if (challenge.get("steps").length != 0) percentDone = user.challenge_statuses[challenge.get("id")] / challenge.get("steps").length;
        itemBackground.style.width = percentDone*100+"%";
        navBar.appendChild(itemBackground);
    }
}

function createChallengeNavBarItem(challenge) {
    const item = document.createElement('div');
    item.className = "challenges-nav-bar-item";
    item.id = "challenges-nav-bar-item-" + challenge.get("id");

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
        const step = user.challenge_statuses[challenge.get("id")]+1;
        if (step < challenge.get("steps").length+1) {
            showChallengeInfo(challenge, step);
        }
        else {
            showChallengeCompletePage(challenge, false);
        }    
    });
    return item;
}

function boldCurrentChallengeTitle(chosenItem) {
    const items = document.getElementsByClassName("challenges-nav-bar-item");
    if (lastBoldedItem != null) lastBoldedItem.style.fontWeight = "normal";
    chosenItem.style.fontWeight = "bold";
    lastBoldedItem = chosenItem;
}


function showChallengeInfo(challenge, displayedStep) {
    
    const completeContent = document.getElementById("challenge-complete-content");
    completeContent.style.display = "none";

    const inProgressContent = document.getElementById("challenges-main-panel-upper-content");
    inProgressContent.style.display = "flex";

    const header = document.getElementById("challenges-main-panel-header");
    let stepsText = displayedStep + "/" + challenge.get("steps").length;
    header.innerText = challenge.get("icon") + " " + challenge.get("title") + " " + stepsText;

    const stepText = document.getElementById("challenges-main-panel-step");
    stepText.innerText = challenge.get("steps")[displayedStep-1];

    setPrevButton(displayedStep, challenge);
    setNextButton(displayedStep, challenge);
    
    const description = document.getElementById("challenges-main-panel-description");
    description.innerText = challenge.get("descriptions")[displayedStep-1];

    const resources = document.getElementById("challenges-main-panel-resources");
    resources.innerText = challenge.get("resources")[displayedStep-1];

    createModalChallengesBadge(displayedStep, challenge);

    const setChallengeDiv = document.getElementById("challenges-modal-set-challenge-div");
    if (displayedStep == user.challenge_statuses[challenge.get("id")]+1) {
        setChallengeDiv.style.display = "flex";
        setCheckbox(challenge);
    }
    else {
        setChallengeDiv.style.display = "none";
    }
}

function showChallengeCompletePage(challenge, newCompletion) {
    const nextButton = document.getElementById("challenges-modal-next-step-button");
    nextButton.style.display = "none";

    const completeContent = document.getElementById("challenge-complete-content");
    completeContent.style.display = "flex";

    const inProgressContent = document.getElementById("challenges-main-panel-upper-content");
    inProgressContent.style.display = "none";

    newCompletion ? showNewChallengeCompletePage(challenge) : showOldChallengeCompletePage(challenge);

    setPrevButton(challenge.get("steps").length+1, challenge);
}

async function showNewChallengeCompletePage(challenge) {
    const text = document.getElementById("challenge-complete-text");
    let newChallengeId = findNextUncompletedChallenge(challenge.get("id"));
    if (newChallengeId == -1) {
        text.innerHTML = "All challenges complete!";
    }
    else {
        text.innerHTML = `${challenge.get("title")} challenge complete!<br>Next up is the <b>${challenges[newChallengeId].get("title")}</b> challenge`;
    }

    const putRequest = new Request(`/user?chal=${newChallengeId}`, {method: 'PUT'});
    user = await fetch(putRequest).then(response => response.json());
}

function findNextUncompletedChallenge(prevChallengeId) {
    for (i = 1; i < challenges.length; i++) {
        let id = (prevChallengeId+i) % challenges.length;
        let status = user.challenge_statuses[id];
        if (status < challenges[id].get("steps").length) {
            return id;
        }
    }
    return -1;
}

function showOldChallengeCompletePage(challenge) {
    const text = document.getElementById("challenge-complete-text");
    text.innerHTML = `${challenge.get("title")} challenge complete!`;
}

function setPrevButton(displayedStep, challenge) {

    const prevButton = document.getElementById("challenges-modal-prev-step-button");
    const nextButton = document.getElementById("challenges-modal-next-step-button");
    if (displayedStep == 1) {
        prevButton.style.display = "none";
        nextButton.style.bottom = 0;
    }
    else {
        prevButton.style.display = "block";
        nextButton.style.bottom = "27px";
    }

    prevButton.onclick = ()=> {
        showChallengeInfo(challenge, displayedStep-1);
    };
}


function setNextButton(displayedStep, challenge) {
    const nextButton = document.getElementById("challenges-modal-next-step-button");
    
    if (displayedStep == user.challenge_statuses[challenge.get("id")]+1) {
        if (user.current_challenge_id != challenge.get("id")) {
            nextButton.style.display = "none";
        }
        else {
            nextButton.style.display = "block";
            nextButton.innerText = "step complete →";
        }
    }
    else {
        nextButton.style.display = "block";
        nextButton.innerText = "view next step →";
    }

    nextButton.onclick = async ()=> {
        let currentStatus = user.challenge_statuses[challenge.get("id")];
        if (currentStatus+1 == displayedStep) {
            const putRequest = new Request(`/user?chal=${challenge.get("id")}&stat=${currentStatus+1}`, {method: 'PUT'});
            user = await fetch(putRequest).then(response => response.json());

            const navBarItemBackground = document.getElementById("challenges-nav-bar-item-background-"+user.current_challenge_id);
            let percentDone = user.challenge_statuses[challenge.get("id")] / challenge.get("steps").length;
            navBarItemBackground.style.width = percentDone*100+"%";
            setChallengeBox(challenge.get("id"));
        }
        if (displayedStep < challenge.get("steps").length) {
            showChallengeInfo(challenge, displayedStep+1);
        }
        else {
            const newCompletion = user.challenge_statuses[challenge.get("id")] == challenge.get("steps").length;
            showChallengeCompletePage(challenge, newCompletion);
        }    
    }; 
} 

function setCheckbox(challenge) {
    if (challenge.get("id") == user.current_challenge_id) {
        checkCheckbox(challenge);
    }
    else {
        resetCheckbox();
    }

    const checkbox = document.getElementById("challenges-modal-set-challenge-checkbox");
    checkbox.onclick =  async () => {
        checkCheckbox(challenge);
        await updateUserCurrentChallenge(challenge.get("id"));
        setNextButton(user.challenge_statuses[challenge.get("id")]+1, challenge);
    };
}

function createModalChallengesBadge(currentStep, challenge) {
    const badge = document.getElementById("modal-challenges-badge");
    const icon = document.getElementById("modal-challenges-badge-icon");
    icon.innerText = challenge.get("icon");
    const totalSteps = challenge.get("steps").length;
    const badgeFilling = document.getElementById("modal-badge-filling");
    badgeFilling.style.height = 150*(currentStep/totalSteps) + "px";
    badgeFilling.style.bottom = 150*(currentStep/totalSteps) + "px";
    if (currentStep/totalSteps == 1) {
        badgeFilling.style.borderRadius = "10px 10px 10px 10px";
    }
    else {
        badgeFilling.style.borderRadius = "0 0 10px 10px";
    }
    badge.appendChild(badgeFilling);
}

function closeChallengesModal() {
    const modal = document.getElementById("challenges-modal");
    modal.style.display = "none";
    setChallengeBox(user.current_challenge_id);
}

function openChallengesModal() {
    const modal = document.getElementById("challenges-modal");
    modal.style.display = "flex";

    const navBarItems = document.getElementsByClassName("challenges-nav-bar-item");

    if (lastBoldedItem != null) lastBoldedItem.style.fontWeight = "normal";

    if (user.current_challenge_id == -1) {
        showChallengeCompletePage(challenges[0], false);
    }

    else {
        const challenge = challenges[user.current_challenge_id];
        showChallengeInfo(challenge, user.challenge_statuses[user.current_challenge_id]+1);
        
        const navBarItem = document.getElementById("challenges-nav-bar-item-"+user.current_challenge_id);
        navBarItem.style.fontWeight = "bold";
        lastBoldedItem = navBarItem;
    }
}

function checkCheckbox(challenge) {
    const checkbox = document.getElementById("challenges-modal-set-challenge-checkbox");
    checkbox.style.border = "none";
    const checkmark = document.getElementById("challenges-modal-checkmark");
    checkmark.style.display = "block";
}

async function updateUserCurrentChallenge(id) {
    const putRequest = new Request(`/user?chal=${id}`, {method: 'PUT'});
    user = await fetch(putRequest).then(response => response.json());

    setChallengeBox(id);
}

function resetCheckbox() {
    const checkbox = document.getElementById("challenges-modal-set-challenge-checkbox");
    checkbox.style.border = "solid 2.5px #004643";
    const checkmark = document.getElementById("challenges-modal-checkmark");
    checkmark.style.display = "none";
}

function openCreateEventModal() {
    const modal = document.getElementById("create-event-modal");
    modal.style.display = "block";
}

function closeCreateEventModal() {
    const modal = document.getElementById("create-event-modal");
    modal.style.display = "none";
}

async function checkLoginStatus() {
    const request = new Request('/login-status', {method: 'GET'});
    const loginStatus = await fetch(request).then(response => response.json());

    const loginBtn = document.getElementById("login-button");
    const feedRightSide = document.getElementById("feed-right-side");

    if (loginStatus["loggedIn"] == "false") {
        feedRightSide.style.display = "none";
        loginBtn.innerText = "Login";
    }
    else {
        feedRightSide.style.display = "block";
        loginBtn.innerText = "Logout";
        if (loginStatus["returningUser"] == "false") {
            openUserInfoModal();
        }
        else await getUserInfo();
    }

    loginBtn.onclick = () => {
        window.location = loginStatus["url"];
    };
}

function openUserInfoModal() {
    const modal = document.getElementById("user-info-modal");
    modal.style.display = "block";
}

async function getUserInfo() {
    const request = new Request('/user', {method: 'GET'});
    user = await fetch(request).then(response => response.json());
}


