document.addEventListener("DOMContentLoaded", function () {

    const ctx = document.getElementById("dataChart").getContext("2d");
    const dataChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: [],
            datasets: [{
                label: 'Temperature',
                data: [],
                borderColor: 'rgba(75, 192, 192, 1)',
                borderWidth: 1,
                fill: false
            }]
        },
        options: {
            scales: {
                xAxes: [{
                    scaleLabel: {
                        display: true,
                        labelString: 'Time'
                    }
                }],
                yAxes: [{
                    ticks: {
                        beginAtZero: true
                    },
                    scaleLabel: {
                        display: true,
                        labelString: 'Temperature (°C)'
                    }
                }]
            }
        }
    });

    // Create an XMLHttpRequest object
    const xhttp = new XMLHttpRequest();

    // Define a callback function
    xhttp.onload = function (ev) {
        const url = xhttp.responseURL;
        const values = JSON.parse(this.response).reverse();
        const [firstValue, ...restValues] = values;
        const labels = [];
        const data = [];
        for (const value of restValues) {
            const aperture = document.createElement("li");
            aperture.innerHTML = value["aperture"];
            const temperature = document.createElement("li");
            temperature.innerHTML = value["temperature"]
            const time = document.createElement("li");
            time.innerHTML = value["time"]

            labels.push(new Date(value["time"]).toLocaleTimeString());
            data.push(value["temperature"]);
        }
        // Update chart
        dataChart.data.labels = labels;
        dataChart.data.datasets[0].data = data;
        dataChart.update();

        // Update statistics
        document.getElementById("aperture").innerText = "Aperture: " + firstValue["aperture"];
        document.getElementById("temperature").innerText = "Temperature: " + firstValue["temperature"];
        document.getElementById("minTemperature").innerText = "Min temperature: " + firstValue["minTemperature"];
        document.getElementById("maxTemperature").innerText = "Max temperature: " + firstValue["maxTemperature"];
        document.getElementById("averageTemperature").innerText = "Average temperature: " + firstValue["averageTemperature"].toFixed(2);

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
