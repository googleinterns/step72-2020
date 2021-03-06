const CLIENT_ID = '605480199600-e4uo1livbvl58cup3qtd1miqas7vspcu.apps.googleusercontent.com';
const API_KEY = 'AIzaSyAUR8-gJeYJOCSDJTP6qgN7FsIDG3u-vgU';
const SCOPES  = "https://www.googleapis.com/auth/calendar.app.created https://www.googleapis.com/auth/calendar.readonly";
var DISCOVERY_DOCS = ["https://www.googleapis.com/discovery/v1/apis/calendar/v3/rest"];

const CHALLENGE_TYPE = {
    RECYCLE: "RECYCLE",
    WASTE: "WASTE",
    GARDENING: "GARDENING",
    FOOD: "FOOD",
};

let user;
let events;
let challenges = [];
let challengeMap = new Map();
let badgeMap = new Map();
const defaultNumChallenges = 3;

const projectTitle = "EnviroGEN";
let calendarId = null; 

let eventCategoryIcons = new Map();
eventCategoryIcons.set("food_beverage", "🥑🍋🍏");
eventCategoryIcons.set("nature", "🌲🌱🌳");
eventCategoryIcons.set("water", "🌊🐳​🌊​");
eventCategoryIcons.set("waste_cleanup", "🗑♻️🥤");
eventCategoryIcons.set("other", "🥑🌲🐢");

const badgeHeight = 120;
const NUM_BOOKMARKS_TEXT = 1;
let lastBoldedItem;

async function loadChallenges() {
    await getServerChallenges();

    setChallengeBox(user.current_challenge_id);

    setChallengesNavBar();

    window.onclick = function(event) {
        const challengesModal = document.getElementById("challenges-modal");
        const createEventModal = document.getElementById("create-event-modal");
        const badgesModal = document.getElementById("badges-modal");
        
        switch(event.target){
          case (challengesModal):
            closeChallengesModal();
            break;
          case (createEventModal):
            closeCreateEventModal();
            break;
          case (badgesModal):
            closeBadgesModal();
            break;
          default:
            break;  
        }
    }
}

async function loadBadges() {
  await getServerBadges(); 
  setEarnedBadges();
}

async function getServerBadges() {
  let id_token = getIdToken();
  const getRequest = new Request(`/badges?&id_token=${id_token}`, {method: 'GET'});
  let badgeJson = await fetch(getRequest).then(response => response.json());
  
  for(badge of badgeJson) {
    badgeMap.set(badge.id, badge);
    //badgeMap.get(badge.id)["url"] = masterBadgeMap.get(badge.id).url;
  }
}

async function loadEvents() {
    const timezone = document.getElementById("user-timezone");
    timezone.value = new Date().getTimezoneOffset();

    const idToken = document.getElementById("id-token");
    idToken.value = getIdToken();

    events = await fetch("/events").then(response => response.json());
    const feed = document.getElementById("events-feed");
    feed.innerHTML = "";
    for (event of events) {
        feed.appendChild(postEvent(event));
    }
}


function postEvent(event) {
    const eventEl = document.createElement('div');
    eventEl.className = "event-post";
    eventEl.appendChild(addEventUserText(event));
    eventEl.appendChild(addEventBookmark(event));
    eventEl.appendChild(addEventAddToCalendarButton(event));
    eventEl.appendChild(addEventMiddleSection(event));
    eventEl.appendChild(addEventInfo(event));
    return eventEl;
}

function addEventAddToCalendarButton(event) {
    const addToCalDiv = document.createElement('div');
    addToCalDiv.style.height = 10;
    addToCalDiv.className = "add-to-calendar-div";
    let signedIn = gapi.auth2.getAuthInstance().isSignedIn.get();
    if (signedIn && user.added_to_calendar_events.includes(event.extendedProperties.event_id)) checkAddToCalendarButton(addToCalDiv);
    else {
        addToCalDiv.innerText = "+";
        addToCalDiv.onclick = () => { clickAddToCalendar(addToCalDiv, event); };
        addToCalDiv.onmouseover = () => { addToCalDiv.appendChild(createAddToCalendarPopup()); };
        addToCalDiv.onmouseout = () => { addToCalDiv.removeChild(addToCalDiv.childNodes[1]); };
    }
    return addToCalDiv;
}

async function clickAddToCalendar(addToCalDiv, event) {
    checkAddToCalendarButton(addToCalDiv);

    let idToken = getIdToken();
    const putRequest = new Request(`/user?add_to_cal=${event.extendedProperties.event_id}&id_token=${idToken}`, {method: 'PUT'});
    user = await fetch(putRequest).then(response => response.json());

    updateCalendar(event);
}

function checkAddToCalendarButton(addToCalDiv) {
    const checkmark = document.createElement('img');
    checkmark.className = "added-to-calendar-checkmark";
    checkmark.src = "/resources/greencheckmark.png";
    addToCalDiv.innerHTML = "";
    addToCalDiv.appendChild(checkmark);
    addToCalDiv.style.marginRight = "8px";
    addToCalDiv.style.cursor = "auto";

    addToCalDiv.onclick = () => {};
    addToCalDiv.onmouseover = () => {};
    addToCalDiv.onmouseout = () => {};
}

function createAddToCalendarPopup() {
    const popup = document.createElement('div');
    popup.className = "add-to-calendar-popup";
    
    popup.innerHTML += `<p class="add-to-calendar-popup-text">Add to Google Calendar</p>
    <div class="popup-triangle"></div>`;
    
    return popup;
}

function addEventBookmark(event) {
    const bookmarkDiv = document.createElement('div');
    bookmarkDiv.style.height = 0;
    const bookmark = document.createElement('img');
    bookmark.className = "event-bookmark";
    bookmarkDiv.appendChild(bookmark);
    bookmarkDiv.appendChild(addEventNumBookmarks(event));
    bookmarkDiv.onclick = () => {};

    if (gapi.auth2.getAuthInstance().isSignedIn.get() && user.bookmarked_events.includes(event.extendedProperties.event_id)) {
        bookmark.src="/resources/filled-bookmark.png"; 
        bookmarkDiv.childNodes[NUM_BOOKMARKS_TEXT].style.color = "#fafafa";
        bookmarkDiv.onclick = async () => {
            unclickBookmark(bookmark, bookmarkDiv, event);
        }
    } else {
        bookmark.src = "/resources/bookmark.png";
        if (gapi.auth2.getAuthInstance().isSignedIn.get()) {
            bookmarkDiv.onclick = async () => { 
                clickBookmark(bookmark, bookmarkDiv, event);
            };
        }   
    }
    return bookmarkDiv;
}

async function clickBookmark(bookmark, bookmarkDiv, event) {
    bookmark.src="/resources/filled-bookmark.png"; 
    bookmarkDiv.childNodes[NUM_BOOKMARKS_TEXT].style.color = "#fafafa";
    event.extendedProperties.bookmarks+=1;
    bookmarkDiv.childNodes[NUM_BOOKMARKS_TEXT].innerText = event.extendedProperties.bookmarks;

    let idToken = getIdToken();
    const putRequest = new Request(`/user?book=${event.extendedProperties.event_id}&add=true&id_token=${idToken}`, {method: 'PUT'});
    user = await fetch(putRequest).then(response => response.json());

    bookmarkDiv.onclick = async () => {
        if (gapi.auth2.getAuthInstance().isSignedIn.get() && user.bookmarked_events.includes(event.extendedProperties.event_id)) {
            await unclickBookmark(bookmark, bookmarkDiv, event);
        }
    };
}

async function unclickBookmark(bookmark, bookmarkDiv, event) {
    bookmark.src = "/resources/bookmark.png";
    bookmarkDiv.childNodes[NUM_BOOKMARKS_TEXT].style.color = "#004643";
    event.extendedProperties.bookmarks-=1;
    bookmarkDiv.childNodes[NUM_BOOKMARKS_TEXT].innerText = event.extendedProperties.bookmarks;

    let idToken = getIdToken();
    const putRequest = new Request(`/user?book=${event.extendedProperties.event_id}&add=false&id_token=${idToken}`, {method: 'PUT'});
    user = await fetch(putRequest).then(response => response.json());

    bookmarkDiv.onclick = async () => {
        if (gapi.auth2.getAuthInstance().isSignedIn.get() && !user.bookmarked_events.includes(event.extendedProperties.event_id)) {
            await clickBookmark(bookmark, bookmarkDiv, event);
        }
    };
}

function addEventNumBookmarks(event) {
    const bookmarkNum = document.createElement('p');
    bookmarkNum.className = "event-bookmark-num";
    const bookmarks = event.extendedProperties.bookmarks;
    if (bookmarks > 99) bookmarkNum.innerText = "99+";
    else bookmarkNum.innerText = bookmarks;
    return bookmarkNum;
}

function addEventUserText(event) {
    const eventUser = document.createElement('p');
    eventUser.className = "event-info";
    eventUser.innerText = event.extendedProperties.creator + " posted an event:";

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
    }
    else {
        icon.innerText = challengeMap.get(challengeId).icon;

        const currentStep = user.challenge_statuses[challengeId];
        const totalSteps = challengeMap.get(challengeId).steps.length;
        
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

function setChallengesNavBar() {
    const navBar = document.getElementById("challenges-nav-bar");
    navBar.innerHTML = "<p id='challenges-nav-bar-header'>Challenges</p>";

    //challenge [key, value]
    for (challenge of challengeMap) {
        navBar.appendChild(createChallengeNavBarItem(challenge[1]));

        const itemBackground = document.createElement('div');
        itemBackground.id = "challenges-nav-bar-item-background-" + challenge[0];
        itemBackground.className = "challenges-nav-bar-item-background";
        let percentDone = 0;
        if (challenge[1].steps.length != 0) percentDone = user.challenge_statuses[challenge[0]] / challenge[1].steps.length;
        itemBackground.style.width = percentDone*100+"%";
        navBar.appendChild(itemBackground);
    }
}

function createChallengeNavBarItem(challenge) {
    const item = document.createElement('div');
    item.className = "challenges-nav-bar-item";
    item.id = "challenges-nav-bar-item-" + challenge.id;

    const title = document.createElement('p');
    title.className = "challenges-nav-bar-item-title";
    title.innerText = challenge.challenge_type;
    item.appendChild(title);

    const icon = document.createElement('p');
    icon.className = "challenges-nav-bar-item-icon";
    icon.innerText = challenge.icon;
    item.appendChild(icon);

    item.addEventListener("click", () => {
        boldCurrentChallengeTitle(item);
        const step = user.challenge_statuses[challenge.id]+1;
        if (step < challenge.steps.length+1) {
            showChallengeInfo(challenge, step);
        }
        else {
            showChallengeCompletePage(challenge, false);
        }    
    });
    return item;
}

async function getServerChallenges(){
  let id_token = getIdToken();
  const response = await fetch(`/challenges?id_token=${id_token}`);
  const challengeJson = await response.json();
  
  for(var i = 0; i < challengeJson.length; i++) {
    let chalIndex = challengeJson[i].id;
    challengeMap.set(chalIndex,challengeJson[i]);

    switch (challengeMap.get(chalIndex).challenge_type){
      case(CHALLENGE_TYPE.RECYCLE):
        challengeMap.get(chalIndex)["icon"] = "♻️";
        //console.log(challengeMap.get(chalIndex).icon);
        break;
      case(CHALLENGE_TYPE.GARDENING):
        challengeMap.get(chalIndex)["icon"] = "🌱";
        break;
      case(CHALLENGE_TYPE.WASTE):
        challengeMap.get(chalIndex)["icon"] = "🗑";
        break;
      case(CHALLENGE_TYPE.FOOD):
        challengeMap.get(chalIndex)["icon"] = "🥑";
        break;
      default:
        challengeMap.get(chalIndex)["icon"] = "⚠";
        break;
    }
  }
}

function boldCurrentChallengeTitle(chosenItem) {
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
    let stepsText = displayedStep + "/" + challenge.steps.length;
    header.innerText = challenge.icon + " " + challenge.name + " " + stepsText;

    const stepText = document.getElementById("challenges-main-panel-step");
    stepText.innerText = challenge.steps[displayedStep-1].left;

    setPrevButton(displayedStep, challenge);
    setNextButton(displayedStep, challenge);
    
    const description = document.getElementById("challenges-main-panel-description");
    description.innerText = challenge.steps[displayedStep-1].right;

    createModalChallengesBadge(displayedStep, challenge);

    const setChallengeDiv = document.getElementById("challenges-modal-set-challenge-div");
    if (displayedStep == user.challenge_statuses[challenge.id]+1) {
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

    setPrevButton(challenge.steps.length+1, challenge);
}

async function showNewChallengeCompletePage(challenge) {
    const text = document.getElementById("challenge-complete-text");
    const otherText = document.getElementById("challenge-others-text")
    let newChallengeId = findNextUncompletedChallenge(challenge.id);
    if (newChallengeId == -1) {
        text.innerHTML = "All challenges complete!";
    }
    else {
        text.innerHTML = `${challenge.challenge_type} challenge complete!<br>Next up is the <b>${challengeMap.get(newChallengeId).name}</b> challenge <br>Share with another user?<br>`;
        createInputElement(text);
        await sendCompletedChallenges(challenge.id, newChallengeId);
        await loadChallenges();
        await updateUserBadges(challenge.challenge_type);
        await loadBadges();
    }
}

function createInputElement(container) {
  var input = document.createElement('input');
  input.type = "text";
  container.appendChild(input);
}

async function sendCompletedChallenges(complete_chal_id ,current_chal_id) {
    id_token = getIdToken();
    put_request = new Request(`/challenges?id_token=${id_token}&completed-chal=${complete_chal_id}&current-chal=${current_chal_id}`, {method: "PUT"});
    user = await fetch(put_request).then(response => response.json());
}

async function updateUserBadges(challenge_type){
  id_token = getIdToken();
  put_request = new Request(`/badges?id_token=${id_token}&challenge-type=${challenge_type}`, {method: "PUT"});
  let new_badge_indicator = await fetch(put_request).then(response => response.json());
  setNewBadgeIndicator(new_badge_indicator);
}

function setNewBadgeIndicator(new_badge_indicator){
  const icon = document.getElementsByClassName("badge-indicator-icon");
  new_badge_indicator? icon[0].innerHTML = `<p>🔵</p>` : icon[0].innerHTML = `<p>🏆</p>`;
}

function setEarnedBadges(){
  const badgeGallery = document.getElementById("gallery-content");
  badgeGallery.innerHTML = "<h2 id='badges-header'> Badges </h2>";
  for(badge of badgeMap){
    badgeGallery.appendChild(createBadgeItem(badge[1]));
  }
}

function createBadgeItem(badge) {
  const item = document.createElement('div');
  item.className = "badge-item";
  item.id = "badge-item-" + badge.id;

  const badgeImage = document.createElement('img');
  badgeImage.setAttribute("src", badgeMap.get(badge.id).url);
  item.append(badgeImage);

  const badgeDesc = document.createElement('p');
  badgeDesc.className = "badge-item-description";
  badgeDesc.innerText = badgeMap.get(badge.id).description;
  item.append(badgeDesc);

  return item;
}

function findNextUncompletedChallenge(prevChallengeId) {
    for(chalId of challengeMap.keys()) {
      if(chalId != prevChallengeId) {
        let status = user.challenge_statuses[chalId];
        if (status < challengeMap.get(chalId).steps.length) {
          return chalId;
        }
      }
    }
    return -1;
}

function showOldChallengeCompletePage(challenge) {
    const text = document.getElementById("challenge-complete-text");
    text.innerHTML = `${challenge.name} challenge complete!`;
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
    
    if (displayedStep == user.challenge_statuses[challenge.id]+1) {
        if (user.current_challenge_id != challenge.id) {
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
        let currentStatus = user.challenge_statuses[challenge.id];
        if (currentStatus+1 == displayedStep) {
            let idToken = getIdToken();
            const putRequest = new Request(`/user?id_token=${idToken}&chal=${challenge.id}&stat=${currentStatus+1}`, {method: 'PUT'});
            user = await fetch(putRequest).then(response => response.json());

            const navBarItemBackground = document.getElementById("challenges-nav-bar-item-background-" + user.current_challenge_id);
            let percentDone = user.challenge_statuses[challenge.id] / challenge.steps.length;
            navBarItemBackground.style.width = percentDone*100+"%";

            setChallengeBox(challenge.id);
        }
        if (displayedStep < challenge.steps.length) {
            showChallengeInfo(challenge, displayedStep+1);
        }
        else {
            const newCompletion = user.challenge_statuses[challenge.id] == challenge.steps.length;
            showChallengeCompletePage(challenge, newCompletion);
        }    
    }; 
} 

function setCheckbox(challenge) {
    if (challenge.id == user.current_challenge_id) {
        checkCheckbox(challenge);
    }
    else {
        resetCheckbox();
    }

    const checkbox = document.getElementById("challenges-modal-set-challenge-checkbox");
    checkbox.onclick =  async () => {
        checkCheckbox(challenge);
        await updateUserCurrentChallenge(challenge.id);
        setNextButton(user.challenge_statuses[challenge.id]+1, challenge);
    };
}

function createModalChallengesBadge(currentStep, challenge) {
    const badge = document.getElementById("modal-challenges-badge");
    const icon = document.getElementById("modal-challenges-badge-icon");
    icon.innerText = challenge.icon;
    const totalSteps = challenge.steps.length;
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
        showChallengeCompletePage(challengeMap.get(user.current_challenge_id), false);
    }

    else {
        const challenge = challengeMap.get(user.current_challenge_id);
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
    let idToken = getIdToken();
    const putRequest = new Request(`/user?id_token=${idToken}&chal=${id}`, {method: 'PUT'});
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

function openBadgesModal(){
    const modal = document.getElementById("badges-modal");
    modal.style.display = "flex";
    setNewBadgeIndicator(false);
}

function showBadges(){
    
  
}

function closeBadgesModal(){
  const modal = document.getElementById("badges-modal");
  modal.style.display = "none";
}

function updateCalendar(event) {
    gapi.client.calendar.calendarList.list().then(function(response) {
          let calendars = response.result.items;
          for (calendar of calendars) {
              if (calendar.summary == projectTitle) {
                  calendarId = calendar.id;
              }
          }

        if (calendarId == null) {
            let calendarRequest = gapi.client.calendar.calendars.insert({
                'summary': projectTitle
            });

            calendarRequest.execute(function(response) {
                calendarId = response.id;
            addEventToCalendar(event);
            });
        }
        else {
            addEventToCalendar(event);
        };    
    });
}

function addEventToCalendar(event) {
    let start = moment(event.start.dateTime.value).format('YYYY-MM-DD[T]HH:mm:ssZZ');
    event.start.dateTime = start;
    let end = moment(event.end.dateTime.value).format('YYYY-MM-DD[T]HH:mm:ssZZ');
    event.end.dateTime = end;

    let request = gapi.client.calendar.events.insert({
            'calendarId': calendarId,
            'resource': event
        });
        request.execute();
}

async function getUserInfo() {
    let idToken = getIdToken();
    let response = await fetch(`/user?id_token=${idToken}`);
    if (response.status == 404) {
        const postRequest = new Request(`/user?id_token=${idToken}`, {method: "POST"});
        await fetch(postRequest);
        response = await fetch(`/user?id_token=${idToken}`)
    }
    user = await response.json();
}

/**
*  On load, called to load the auth2 library and API client library.
*/
function handleClientLoad() {
    gapi.load('client:auth2', initClient);
}

/**
*  Initializes the API client library and sets up sign-in state
*  listeners.
*/
function initClient() {
    gapi.client.init({
     apiKey: API_KEY,
     clientId: CLIENT_ID,
     discoveryDocs: DISCOVERY_DOCS,
      scope: SCOPES
    }).then(function () {
    // Listen for sign-in state changes.
    gapi.auth2.getAuthInstance().isSignedIn.listen(updateSigninStatus);

    // Handle the initial sign-in state.
    updateSigninStatus(gapi.auth2.getAuthInstance().isSignedIn.get());
    const authorizeButton = document.getElementById('authorize-button');
    const signoutButton = document.getElementById('signout-button');
    authorizeButton.onclick = handleAuthClick;
    signoutButton.onclick = handleSignoutClick;
    
}, function(error) {
        console.log(JSON.stringify(error, null, 2));
    });
}

/**
*  Called when the signed in status changes, to update the UI
*  appropriately. After a sign-in, the API is called.
*/
async function updateSigninStatus(isSignedIn) {
    const authorizeButton = document.getElementById('authorize-button');
    const signoutButton = document.getElementById('signout-button');
    const feedRightSide = document.getElementById("feed-right-side");
    const showBookmarkedOption = document.getElementById("show-bookmarked-div");
    if (isSignedIn) {
        authorizeButton.style.display = 'none';
        signoutButton.style.display = 'block';
        feedRightSide.style.display = "block";  
        showBookmarkedOption.style.display = "flex";
        await getUserInfo();
        await loadEvents();
        await loadChallenges();
        await loadBadges();
        showAddToCalendarButtons();
    } else {
        authorizeButton.style.display = 'block';
        signoutButton.style.display = 'none';
        feedRightSide.style.display = "none";
        showBookmarkedOption.style.display = "none";
        await loadEvents();
        hideAddToCalendarButtons();
    }
}

/**
*  Sign in the user upon button click.
*/
function handleAuthClick(event) {
    gapi.auth2.getAuthInstance().signIn();
}

/**
*  Sign out the user upon button click.
*/
function handleSignoutClick(event) {
    gapi.auth2.getAuthInstance().signOut();
}

function showAddToCalendarButtons() {
    const buttons = document.getElementsByClassName("add-to-calendar-div");
    for (btn of buttons) btn.style.display = "block";
}

function hideAddToCalendarButtons() {
    const buttons = document.getElementsByClassName("add-to-calendar-div");
    for (btn of buttons) btn.style.display = "none";
}

function getIdToken() {
    return gapi.auth2.getAuthInstance().currentUser.get().getAuthResponse().id_token;
}

async function sortEventsByDistance(location) {
    const getDistanceMatrix = (service, data) => new Promise((resolve, reject) => {
        service.getDistanceMatrix(data, (response, status) => {
            if (status == "OK") {
                resolve(response);
            } else {
                reject(response);
            }
        })
    });

    const service = new google.maps.DistanceMatrixService;
    for (event of events) {
        const result = await getDistanceMatrix(service, {
            origins: [location],
            destinations: [event.location],
            travelMode: 'DRIVING',
            unitSystem: google.maps.UnitSystem.METRIC,
            avoidHighways: false,
            avoidTolls: false
        });
        event.distance = result.rows[0].elements[0].distance.value;
    }
    

    function compareByDistance(a, b) {
        if (a.distance == null && b.distance == null || a.distance == b.distance) return 0;
        else if (a.distance == null || a.distance < b.distance) return -1;
        else return 1;
    }

    events.sort(compareByDistance);
}

async function getLocalEvents(location) {
    await sortEventsByDistance(location);

    if (showBookmarked) showOnlyBookmarkedEvents();
    else showAllEvents();
}

let showBookmarked = false;

function toggleShowBookmarked() {
    showBookmarked = !showBookmarked;
    showBookmarked ? showOnlyBookmarkedEvents() : showAllEvents();   
}

function showOnlyBookmarkedEvents() {
    const checkmark = document.getElementById("show-bookmarked-checkmark");
    checkmark.style.display = "block";
    const feed = document.getElementById("events-feed");
    feed.innerHTML = "";
    for (event of events) {
        if (user.bookmarked_events.includes(event.extendedProperties.event_id)) feed.appendChild(postEvent(event));
    }
    showAddToCalendarButtons();
}

function showAllEvents() {
    const checkmark = document.getElementById("show-bookmarked-checkmark");
    checkmark.style.display = "none";
    const feed = document.getElementById("events-feed");
    feed.innerHTML = "";
    for (event of events) {
        feed.appendChild(postEvent(event));
    }
    if (gapi.auth2.getAuthInstance().isSignedIn.get()) showAddToCalendarButtons();
}