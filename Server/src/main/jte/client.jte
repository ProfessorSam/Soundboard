<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Party Soundboard</title>
    <style>
        body {
            margin: 0;
            font-family: Arial, sans-serif;
            background-color: #f3f4f6;
            display: flex;
            flex-direction: column;
            height: 100vh;
        }
        header {
            padding: 1rem;
            display: flex;
            justify-content: space-between;
            align-items: center;
            background: white;
            box-shadow: 0 2px 5px rgba(0,0,0,0.1);
            z-index: 1;
        }
        #console {
            flex: 1;
            overflow-y: auto;
            background: white;
            margin: 1rem;
            padding: 1rem;
            border-radius: 1rem;
            box-shadow: 0 0 10px rgba(0,0,0,0.05);
            font-size: 0.9rem;
        }
        #console div {
            margin-bottom: 0.5rem;
            color: #374151;
        }
        #volume-container {
            display: flex;
            align-items: center;
        }
        #volume-container label {
            margin-right: 0.5rem;
            font-size: 0.9rem;
        }
        input[type=range] {
            width: 150px;
        }
        #countdown-container {
            padding: 1rem;
            background: white;
            box-shadow: 0 -2px 5px rgba(0,0,0,0.05);
            display: flex;
            justify-content: center;
            align-items: center;
            gap: 0.5rem;
        }
        #countdown-container input[type=number] {
            width: 80px;
            padding: 0.3rem;
        }
        #countdown-container button {
            padding: 0.4rem 0.8rem;
            background-color: #2563eb;
            color: white;
            border: none;
            border-radius: 0.5rem;
            cursor: pointer;
        }
    </style>
</head>
<body>
<header>
    <h1>Party Soundboard</h1>
    <div id="volume-container">
        <label for="volume">Volume</label>
        <input type="range" id="volume" min="0" max="1" step="0.01" value="0.5" />
    </div>
</header>
<main id="console"></main>
<div id="countdown-container">
    <label for="countdown">Countdown (s):</label>
    <input type="number" id="countdown" min="0" />
    <button onclick="setCountdown()">Set Countdown</button>
</div>

<script>
    @raw
    const eventConsole = document.getElementById('console');
    const volumeSlider = document.getElementById('volume');
    const audioCache = {};
    let currentVolume = parseFloat(volumeSlider.value);

    volumeSlider.addEventListener('input', (e) => {
        currentVolume = parseFloat(e.target.value);
    });

    const playSound = async (id) => {
        if (!audioCache[id]) {
            const res = await fetch(`/api/sound/${id}`);
            const blob = await res.blob();
            const url = URL.createObjectURL(blob);
            audioCache[id] = new Audio(url);
        }
        const audio = audioCache[id].cloneNode();
        audio.volume = currentVolume;
        audio.play();
    };

    const addEvent = ({ id, tag, emitter }) => {
        const time = new Date().toLocaleTimeString();
        const entry = document.createElement('div');
        entry.textContent = `[${time}] ${emitter} played ${tag} (ID: ${id})`;
        eventConsole.appendChild(entry);
        eventConsole.scrollTop = eventConsole.scrollHeight;
    };

    const eventSource = new EventSource('/partyclientsse');
    eventSource.addEventListener('play', function(event) {
        console.log("Playing sound...");
        const data = JSON.parse(event.data);
        addEvent(data);
        playSound(data.id);
    });
    eventSource.onerror = function(error) {
        console.error('SSE error:', error);
    };
    eventSource.onopen = function() {
        console.log('SSE connection opened');
    };

    function setCountdown() {
        const value = parseInt(document.getElementById('countdown').value);
        if (!isNaN(value)) {
            fetch('/api/setcountdown', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ countdown: value })
            }).then(res => {
                if (res.ok) {
                    alert('Countdown set!');
                } else {
                    alert('Failed to set countdown.');
                }
            }).catch(err => {
                console.error('Error setting countdown:', err);
                alert('Error setting countdown.');
            });
        }
    }
    @endraw
</script>
</body>
</html>
