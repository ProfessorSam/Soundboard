@param de.professorsam.soundboard.SoundBoardContext context

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Soundboard</title>
    <style>
        :root {
            --bg-light: #f5f5f5;
            --bg-dark: #121212;
            --text-light: #333;
            --text-dark: #eee;
            --pulse-active-start: rgba(0, 200, 255, 0.4);
            --pulse-active-end: rgba(0, 255, 153, 0.4);
            --pulse-cooldown: rgba(255, 0, 0, 0.3);
            --btn-dark: #333;
            --btn-light: #ddd;
        }

        body {
            margin: 0;
            font-family: system-ui, sans-serif;
            background-color: var(--bg-light);
            color: var(--text-light);
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 1rem;
            transition: background-color 0.3s, color 0.3s;
        }

        body.dark {
            background-color: var(--bg-dark);
            color: var(--text-dark);
        }

        .soundboard {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            grid-template-rows: repeat(6, 1fr);
            gap: 1rem;
            width: 100%;
            max-width: 500px;
        }

        .sound {
            background-color: #ffffff;
            border: 1px solid #ddd;
            border-radius: 12px;
            padding: 1rem;
            text-align: center;
            font-size: 1.2rem;
            cursor: pointer;
            position: relative;
            overflow: hidden;
            transition: background-color 0.3s ease, transform 0.1s ease, box-shadow 0.5s ease;
            box-shadow:
                    0 0 6px 2px rgba(0, 255, 200, 0.1),
                    0 0 12px 4px rgba(0, 200, 255, 0.05),
                    0 0 18px 6px rgba(0, 255, 136, 0.05);
        }

        .sound:active {
            transform: scale(0.97) rotate(-1deg);
            background-color: #f0f0f0;
            animation: shake 0.3s ease;
            box-shadow: 0 0 25px rgba(0, 255, 170, 0.8);
        }

        .cooling .sound {
            background-color: #e0e0e0 !important;
            cursor: not-allowed;
            pointer-events: none;
            animation: none !important;
            box-shadow: 0 0 15px rgba(255, 0, 0, 0.6);
        }

        @keyframes shake {
            0% { transform: scale(0.97) rotate(-1deg); }
            25% { transform: scale(0.98) rotate(1deg); }
            50% { transform: scale(0.97) rotate(-1deg); }
            75% { transform: scale(0.98) rotate(1deg); }
            100% { transform: scale(0.97) rotate(0deg); }
        }

        @keyframes pulse-active {
            0% { box-shadow: 0 0 6px 2px var(--pulse-active-start); }
            50% { box-shadow: 0 0 12px 6px var(--pulse-active-end); }
            100% { box-shadow: 0 0 6px 2px var(--pulse-active-start); }
        }

        .cooldown-overlay {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0,0,0,0.4);
            color: white;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.1rem;
            pointer-events: none;
            opacity: 0;
            transition: opacity 0.3s ease;
        }

        .cooling .cooldown-overlay {
            opacity: 1;
        }

        .cooldown-timer {
            margin-top: 1rem;
            font-weight: bold;
            font-size: 1.1rem;
            color: #d32f2f;
            transition: opacity 0.3s ease;
        }

        .toggle-dark {
            margin-top: 2rem;
            padding: 0.5rem 1.5rem;
            border-radius: 999px;
            border: none;
            font-size: 1rem;
            background-color: var(--btn-dark);
            color: white;
            cursor: pointer;
            transition: background-color 0.3s;
        }

        body.dark .toggle-dark {
            background-color: var(--btn-light);
            color: black;
        }

        body.dark .sound {
            background-color: #2a2a2a;
            color: #eee;
        }
    </style>
</head>
<body>
<div id="board" class="soundboard">
    @for (int i = 1; i <= 12; i++)
        <button class="sound" data-id="${i}">
            ${de.professorsam.soundboard.SoundBoard.getInstance().idToTag(i)}
            <div class="cooldown-overlay">Wait…</div>
        </button>
    @endfor
</div>
<div id="cooldown-timer" class="cooldown-timer" style="display: none;">Cooldown: <span id="cooldown-seconds">0</span>s</div>

<button class="toggle-dark" onclick="toggleDarkMode()">Dark Mode</button>

<script>
    let cooldown = ${context.cooldown()};
    const board = document.getElementById('board');
    const buttons = board.querySelectorAll('.sound');
    const timerContainer = document.getElementById('cooldown-timer');
    const cooldownSeconds = document.getElementById('cooldown-seconds');
    let cooldownInterval;

    function setCooldown(seconds) {
        let remaining = seconds;
        cooldownSeconds.textContent = remaining;
        timerContainer.style.display = 'block';
        board.classList.add('cooling');

        cooldownInterval = setInterval(() => {
            remaining--;
            cooldownSeconds.textContent = remaining;
            if (remaining <= 0) {
                clearInterval(cooldownInterval);
                board.classList.remove('cooling');
                timerContainer.style.display = 'none';
                startPulsing();
            }
        }, 1000);
    }

    function startPulsing() {
        buttons.forEach((button, index) => {
            const delay = index * 500;
            setTimeout(() => {
                button.style.animation = 'pulse-active 5s infinite ease-in-out';
            }, delay);
        });
    }

    function stopPulsing() {
        buttons.forEach(button => {
            button.style.animation = 'none';
        });
    }

    buttons.forEach(button => {
        button.addEventListener('click', () => {
            stopPulsing();
            const id = button.getAttribute('data-id');
            fetch("/api/sound/" + id, {
                method: 'POST'
            }).then(() => {
                board.classList.add('cooling');
                window.location.reload(true);
            }).catch(err => console.error('Error:', err));

            if (navigator.vibrate) {
                navigator.vibrate(100);
            }
        });
    });

    if (cooldown > 0) {
        setCooldown(cooldown);
    } else {
        startPulsing();
    }

    function toggleDarkMode() {
        const isDark = document.body.classList.toggle('dark');
        localStorage.setItem('theme', isDark ? 'dark' : 'light');
    }

    window.addEventListener('DOMContentLoaded', () => {
        const savedTheme = localStorage.getItem('theme');
        if (savedTheme === 'dark') {
            document.body.classList.add('dark');
        }
    });
</script>
</body>
</html>
