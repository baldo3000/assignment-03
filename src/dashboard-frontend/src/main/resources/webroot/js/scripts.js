document.addEventListener("DOMContentLoaded", function () {
    const ctx = document.getElementById("dataChart").getContext("2d");
    const dataChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: [],
            datasets: [{
                label: 'Temperature',
                data: [],
                borderColor: 'rgba(75, 192, 192, 1)', // Default color (will be overridden per segment)
                borderWidth: 1,
                segment: {
                    borderColor: function (ctx) {
                        if (!ctx.p1) return 'rgba(75, 192, 192, 1)'; // Default color

                        const nextValue = ctx.p1.raw; // The point this segment leads to
                        if (nextValue >= 25) {
                            return 'rgba(255, 0, 0, 1)';
                        } else if (nextValue >= 20) {
                            return 'rgba(255, 127, 0, 1)';
                        } else {
                            return 'rgba(75, 192, 192, 1)';
                        }
                    }
                },
                pointBackgroundColor: function (context) {
                    const value = context.raw;
                    if (value >= 25) return 'rgba(255, 0, 0, 1)';
                    else if (value >= 20) return 'rgba(255, 127, 0, 1)';
                    else return 'rgba(75, 192, 192, 1)';
                },
                pointBorderColor: function (context) {
                    const value = context.raw;
                    if (value >= 25) return 'rgba(255, 0, 0, 1)';
                    else if (value >= 20) return 'rgba(255, 127, 0, 1)';
                    else return 'rgba(75, 192, 192, 1)';
                },
                fill: false
            }]
        },
        options: {
            animation: {
                x: {duration: 1000},
                y: {duration: 0}      // Disable animation along the y-axis
            },
            scales: {
                x: {
                    title: {
                        display: true,
                        text: 'Time'
                    }
                },
                y: {
                    beginAtZero: true,
                    title: {
                        display: true,
                        text: 'Temperature (°C)'
                    }
                }
            }
        }
    });

    // Create an XMLHttpRequest object
    const xhttp = new XMLHttpRequest();

    // Define a callback function
    xhttp.onload = function (ev) {
        const values = JSON.parse(this.response).reverse();
        const [stats, ...records] = values;
        const labels = [];
        const data = [];
        for (const value of records) {
            labels.push(new Date(value["time"]).toLocaleTimeString());
            data.push(value["temperature"]);
        }
        // Update chart
        dataChart.data.labels = labels;
        dataChart.data.datasets[0].data = data;
        dataChart.update();

        // Update statistics
        document.getElementById("state").innerText = "State: " + stats["state"];
        document.getElementById("aperture").innerText = "Aperture: " + stats["aperture"];
        document.getElementById("temperature").innerText = "Temperature: " + stats["temperature"];
        document.getElementById("minTemperature").innerText = "Min temperature: " + stats["minTemperature"];
        document.getElementById("maxTemperature").innerText = "Max temperature: " + stats["maxTemperature"];
        document.getElementById("averageTemperature").innerText = "Average temperature: " + stats["averageTemperature"].toFixed(2);

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
