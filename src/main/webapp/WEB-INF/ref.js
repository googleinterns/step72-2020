//start here to implement (note for me)
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



function openChallengesModal() {
    const modal = document.getElementById("challenges-modal");
    modal.style.display = "flex";

    const navBarItems = document.getElementsByClassName("challenges-nav-bar-item");
    if (lastBoldedItem != null) lastBoldedItem.style.fontWeight = "normal";

    if (user.current_challenge_id == -1) {
        //showChallengeCompletePage(challengeMap, false);
    }

    else {
        const challenge = challengeMap.get(user.current_challenge_id);
        showChallengeInfo(challenge, user.challenge_statuses[user.current_challenge_id]+1);
        
        const navBarItem = document.getElementById("challenges-nav-bar-item-"+user.current_challenge_id);
        navBarItem.style.fontWeight = "bold";
        lastBoldedItem = navBarItem;
    }
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
    stepText.innerText = challenge.steps[displayedStep-1].key;

    setPrevButton(displayedStep, challenge);
    setNextButton(displayedStep, challenge);
    
    const description = document.getElementById("challenges-main-panel-description");
    description.innerText = challenge.steps[displayedStep-1].value;

    const resources = document.getElementById("challenges-main-panel-resources");
    //resources.innerText = challenge.get("resources")[displayedStep-1];

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


async function sendCompletedChallenges(complete_chal_id ,current_chal_id) {
    id_token = getIdToken();
    put_request = new Request(`/challenges?id_token=${id_token}&completed-chal=${complete_chal_id}&current-chal=${current_chal_id}`, {method: "PUT"});
    user = await fetch(put_request).then(response => response.json());
}


     /*   <img src = "resources/gard_medal_1.png"></img>
        <img src = "resources/gard_medal_3.png"></img>
        <img src = "resources/recy_medal_1.png"></img>
        <img src = "resources/recy_medal_3.png"></img>
        <img src = "resources/wast_medal_1.png"></img>   */