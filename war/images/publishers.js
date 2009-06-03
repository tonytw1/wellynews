// global flag
var isIE = false;

// global request and XML document objects
var req;

// retrieve XML document (reusable generic function);
// parameter is URL string (relative or complete) to
// an .xml file whose Content-Type is a valid XML
// type, such as text/xml; XML source must be from
// same domain as HTML file
function loadXMLDoc(url) {
    // branch for native XMLHttpRequest object
    if (window.XMLHttpRequest) {
        req = new XMLHttpRequest();
        req.onreadystatechange = processReqChange;
        req.open("GET", url, true);
        req.send(null);
    // branch for IE/Windows ActiveX version
    } else if (window.ActiveXObject) {
        isIE = true;
        req = new ActiveXObject("Microsoft.XMLHTTP");
        if (req) {
            req.onreadystatechange = processReqChange;
            req.open("GET", url, true);
            req.send();
        }
    }
}

// handle onreadystatechange event of req object
function processReqChange() {
    // only if req shows "loaded"
    if (req.readyState == 4) {
        // only if "OK"
        if (req.status == 200) {
            //clearTopicList();
            //buildTopicList();
            buildPublisherList();
         } else {
            alert("There was a problem retrieving the XML data:\n" +
                req.statusText);
         }
    }
}

// invoked by "Category" select element change;
// loads chosen XML document, clears Topics select
// element, loads new items into Topics select element
function loadDoc(evt) {
    // equalize W3C/IE event models to get event object
    evt = (evt) ? evt : ((window.event) ? window.event : null);
    if (evt) {
        // equalize W3C/IE models to get event target reference
        var elem = (evt.target) ? evt.target : ((evt.srcElement) ? evt.srcElement : null);
        if (elem) {
            try {
                if (elem.selectedIndex > 0) {
                    loadXMLDoc("publishers.xml");
                }       
            }
            catch(e) {
                var msg = (typeof e == "string") ? e : ((e.message) ? e.message : "Unknown Error");
                alert("Unable to get XML data:\n" + msg);
                return;
            }
        }
    }
}

// retrieve text of an XML document element, including
// elements using namespaces
function getElementTextNS(prefix, local, parentElem, index) {
    var result = "";
    if (prefix && isIE) {
        // IE/Windows way of handling namespaces
        result = parentElem.getElementsByTagName(prefix + ":" + local)[index];
    } else {
        // the namespace versions of this method 
        // (getElementsByTagNameNS()) operate
        // differently in Safari and Mozilla, but both
        // return value with just local name, provided 
        // there aren't conflicts with non-namespace element
        // names
        result = parentElem.getElementsByTagName(local)[index];
    }
    if (result) {
        // get text, accounting for possible
        // whitespace (carriage return) text nodes 
        if (result.childNodes.length > 1) {
            return result.childNodes[1].nodeValue;
        } else {
            return result.firstChild.nodeValue;                 
        }
    } else {
        return "n/a";
    }
}

// empty Topics select list content
function clearPublisherList() {
    var select = document.getElementById("publisher");
    while (select.length > 0) {
        select.remove(0);
    }
}




// add item to select element the less
// elegant, but compatible way.
function appendToSelect(select, value, content) {
    var opt;
    opt = document.createElement("option");
    opt.value = value;
    opt.appendChild(content);
    select.appendChild(opt);
}


function buildPublisherList() {
      var select = document.getElementById("publisher");
      var publishers = req.responseXML.getElementsByTagName("publisher");
      clearPublisherList();
      for (var i=0; i < publishers.length; i++) {
        	appendToSelect(select,
        	    getElementTextNS("", "id", publishers[i], 0),
        	    document.createTextNode(getElementTextNS("", "name", publishers[i], 0))
        	    );
      }
}


// Based on the value of the URL form field, use an XMLHttpRequest to populate to
// obtain a listing of possible publishers.
function refinePublishers() {
	url = "ajax/publishers";
	urlValue = document.getElementById("url").value;
	if (urlValue != "") {
		url = url + "?url=" + urlValue;
		loadXMLDoc(url);
	}
}