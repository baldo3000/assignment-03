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
                        beginAtZero: true,
                        max: 40
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
    xhttp.onload = function () {
        const values = JSON.parse(this.response).reverse();
        const labels = [];
        const data = [];
        for (const value of values) {
            const aperture = document.createElement("li");
            aperture.innerHTML = value["aperture"];
            const temperature = document.createElement("li");
            temperature.innerHTML = value["temperature"]
            const time = document.createElement("li");
            time.innerHTML = value["time"]

            labels.push(new Date(value["time"]).toLocaleTimeString());
            data.push(value["temperature"]);
        }
        dataChart.data.labels = labels;
        dataChart.data.datasets[0].data = data;
        dataChart.update();
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
