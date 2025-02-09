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
                    type: 'time',
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

    const resetButton = document.getElementById("resetAlarm");
    const manualModeButton = document.getElementById("manualMode");
    const automaticModeButton = document.getElementById("automaticMode");
    const overrideSection = document.getElementById("override");

    function updateButtonStates(state, mode) {
        resetButton.disabled = state !== "ALARM";
        manualModeButton.hidden = mode !== "AUTOMATIC";
        automaticModeButton.hidden = mode !== "MANUAL";
        overrideSection.hidden = mode !== "MANUAL";
    }


    // Send a request for values
    function getValues() {
        const xhttp = new XMLHttpRequest();
        xhttp.open("GET", "/api/data", true);
        xhttp.onload = function (ev) {
            const values = JSON.parse(this.response).reverse();
            const [stats, ...records] = values;
            const labels = [];
            const data = [];
            for (const value of records) {
                labels.push(new Date(value["time"]));
                data.push(value["temperature"]);
            }
            // Update chart
            dataChart.data.labels = labels;
            dataChart.data.datasets[0].data = data;
            dataChart.update();

            // Update statistics
            document.getElementById("state").innerText = "State: " + stats["state"];
            document.getElementById("mode").innerText = "Mode: " + stats["mode"];
            document.getElementById("aperture").innerText = "Aperture: " + stats["aperture"];
            document.getElementById("temperature").innerText = "Temperature: " + stats["temperature"];
            document.getElementById("minTemperature").innerText = "Min temperature: " + stats["minTemperature"];
            document.getElementById("maxTemperature").innerText = "Max temperature: " + stats["maxTemperature"];
            document.getElementById("averageTemperature").innerText = "Average temperature: " + stats["averageTemperature"].toFixed(2);

            updateButtonStates(stats["state"], stats["mode"]);
        }
        xhttp.send();
    }

    // Schedule the main update
    getValues();
    setInterval(getValues, 2000);

    document.getElementById("resetAlarm").addEventListener("click", function () {
        const xhttp = new XMLHttpRequest();
        xhttp.open("POST", "/api/commands", true);
        xhttp.setRequestHeader("Content-Type", "application/json; charset=UTF-8");
        xhttp.onload = function () {
            if (xhttp.status === 200) {
                console.log("Alarm reset request sent successfully");
            } else {
                console.error("Failed to send alarm reset request");
            }
        };
        xhttp.send(JSON.stringify({reset: true}));
    });

    document.getElementById("manualMode").addEventListener("click", function () {
        const xhttp = new XMLHttpRequest();
        xhttp.open("POST", "/api/commands", true);
        xhttp.setRequestHeader("Content-Type", "application/json; charset=UTF-8");
        xhttp.onload = function () {
            if (xhttp.status === 200) {
                console.log("Manual mode signal sent successfully");
            } else {
                console.error("Failed to send manual mode signal");
            }
        };
        xhttp.send(JSON.stringify({mode: "manual"}));
    });

    document.getElementById("automaticMode").addEventListener("click", function () {
        const xhttp = new XMLHttpRequest();
        xhttp.open("POST", "/api/commands", true);
        xhttp.setRequestHeader("Content-Type", "application/json; charset=UTF-8");
        xhttp.onload = function () {
            if (xhttp.status === 200) {
                console.log("Automatic mode signal sent successfully");
            } else {
                console.error("Failed to send automatic mode signal");
            }
        };
        xhttp.send(JSON.stringify({mode: "automatic"}));
    });

    document.getElementById("overrideButton").addEventListener("click", function () {
        const xhttp = new XMLHttpRequest();
        xhttp.open("POST", "/api/commands", true);
        xhttp.setRequestHeader("Content-Type", "application/json; charset=UTF-8");
        xhttp.onload = function () {
            if (xhttp.status === 200) {
                console.log("Override value sent successfully");
            } else {
                console.error("Failed to send override value");
            }
        };
        const value = document.getElementById("manualOverride").value;
        xhttp.send(JSON.stringify({aperture: value}));
    });
});
