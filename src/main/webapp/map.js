google.charts.load('current', {packages: ['corechart']});

const zipCodeRegex = /(^\d{5}$)|(^\d{5}-\d{4}$)/;
var geocoder;
var lastGeocode = null
var map;
var lastInfoWindow = null;
var bounds;
var state = "01";
var town = "";

function initMap() {
    var mapOptions = {
    center: {lat: 42, lng: -93},
    zoom: 4,
    gestureHandling: 'greedy',
    streetViewControl: false,
    maxZoom: 13,
    minZoom: 4,
    style: [
        {
          "featureType": "all",
          "stylers": [
            { "color": "#C0C0C0" }
          ]
        },{
          "featureType": "road.arterial",
          "elementType": "geometry",
          "stylers": [
            { "color": "#CCFFFF" }
          ]
        },{
          "featureType": "poi",
          "stylers": [
            { "visibility": "off" }
          ]
        }
      ]
    };

    geocoder = new google.maps.Geocoder();
    map = new google.maps.Map(document.getElementById('map'), mapOptions);


    map.controls[google.maps.ControlPosition.LEFT_TOP].push(document.getElementById("content_form"));
    map.controls[google.maps.ControlPosition.RIGHT_BOTTOM].push(document.getElementById("water"));
}

function loadAreaDataFromForm(){
    event.preventDefault();
    bounds = new google.maps.LatLngBounds();
    var areaType = document.getElementById("area").value;
    var areaCode = document.getElementById("zip_code").value;
    if(!zipCodeRegex.test(areaCode)){
        console.log(areaCode+" is an invalid postal code");
        return;
    }
    zoomToArea(areaType, areaCode);
}

function zoomToArea(areaType, areaCode){
    var geocodeStatus;
    geocoder.geocode({address:areaCode}, function(results, status) {
        console.log("geocode of zip "+areaCode+" is "+status);
        if(status === "OK"){
            lastGeocode = results[0];
            map.setCenter(lastGeocode.geometry.location);
            map.fitBounds(lastGeocode.geometry.viewport);
            bounds.union(lastGeocode.geometry.viewport);
            
            setGeographicState();
            addSuperfundMarkers(areaType, areaCode);
            addWaterSystem(areaType, areaCode);

        } else {

            map.setZoom(4);
            map.setCenter({lat: 42, lng: -93});
            console.log("Reset map center and zoom");
        }
    });
}

function setGeographicState(){
    if(lastGeocode.postcode_localities != null) town = lastGeocode.postcode_localities[0];
    else town = lastGeocode.address_components[lastGeocode.address_components.length-4].short_name;
    for(i in lastGeocode.address_components){
        if(lastGeocode.address_components[i].short_name.length === 2){
            state = lastGeocode.address_components[i].short_name;
            break;
        }
    }
    document.getElementById("view_state_button").innerText = "View State of "+state;
}

function viewState(){
    if(lastGeocode === null) return;
    addSuperfundMarkers("state", state);
    console.log("Zoom to state of "+state);
    
}

function addSuperfundMarkers(areaType, areaCode){
    fetch('/superfund?area='+areaType+"&zip_code="+areaCode).then(response => response.json()).then((sites) => {
        sites.forEach((site) => {

        bounds.extend({lat: site.lattitude, lng: site.longitude});

        var marker = new google.maps.Marker(
                    {position: {lat: site.lattitude, lng: site.longitude}, 
                    title: site.name,
                    icon: iconUrl(site.score),
                    map: map});
                var contentStr = "<h3>"+site.name+"</h3>" +
                    "<h4>"+site.city+", "+site.state+"</h4>";
        var infoWindow = new google.maps.InfoWindow({
                    content: contentStr
                });
            marker.addListener('click', () => {
                    if(lastInfoWindow != null) {lastInfoWindow.close();}
                    infoWindow.open(map, marker);
                    lastInfoWindow = infoWindow;
                });
                map.addListener('click', () => {
                    infoWindow.close();
                });
        });
        console.log("Placed Superfund Markers");
    }).then((sites) => {
        map.fitBounds(bounds);
    });
}

const low_danger = 25;
const medium_danger = 40;
const high_danger = 55;

function iconUrl(score){
	var url = "https://maps.google.com/mapfiles/ms/icons/";
	if(score < low_danger) url+= "green";
	else if(score < medium_danger) url += "yellow";
	else if (score < high_danger) url +="orange";
    else url +="red";
	url += "-dot.png";
	return url;
}

function addWaterSystem(areaType, zipCode){
    fetch("/water?town="+town+"&state="+state).then(response => response.json()).then((systems) => {
        const waterElement = document.getElementById("water");
        var waterPollutionHTML = "";
        systems.forEach((system) => {
            console.log(system);
            console.log(system.contaminants);
            waterPollutionHTML += 
                "<h2>"+system.name+"</h2>"+
                "<h4>Serves "+system.populationServed+"</h4>";
            system.contaminants.forEach((contaminant) => {
                waterPollutionHTML += "<p>"+contaminant.contaminantName+"</p>";
            });
        });
        waterElement.innerHTML = waterPollutionHTML;
    });
}