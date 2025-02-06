document.addEventListener("DOMContentLoaded", function () {

    // Create an XMLHttpRequest object
    const xhttp = new XMLHttpRequest();

    // Define a callback function
    xhttp.onload = function () {
        // Here you can use the Data
        const ul = document.getElementById("datalist");
        ul.innerHTML = "";
        const values = JSON.parse(this.response);
        for (const value of values) {
            const element = document.createElement("li");
            const elementList = document.createElement("ul");

            const aperture = document.createElement("li");
            aperture.innerHTML = value["aperture"];
            const temperature = document.createElement("li");
            temperature.innerHTML = value["temperature"]
            const time = document.createElement("li");
            time.innerHTML = value["time"]

            elementList.appendChild(aperture);
            elementList.appendChild(temperature);
            elementList.appendChild(time);

            element.appendChild(elementList);

            ul.appendChild(element);
        }
    }

    // Send a request for values
    function getValues() {
        xhttp.open("GET", "/api/data", true);
        xhttp.send();
    }

    // Schedule the main update
    getValues();
    setInterval(getValues, 2000);
});
