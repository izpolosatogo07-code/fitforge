// ============================================================
//  FitForge – Boxtimer
// ============================================================

const CIRC = 553; // 2 * PI * 88

const state = {
  rounds: 3,
  work:   60,
  rest:   30,
  cur:    0,
  sec:    0,
  phase:  'idle',   // 'idle' | 'work' | 'rest' | 'paused'
  timer:  null,
  prevPhase: 'work' // Für Pause-Fortsetzen
};

// --- DOM-Elemente ---
const valRounds  = document.getElementById('val-rounds');
const valWork    = document.getElementById('val-work');
const valRest    = document.getElementById('val-rest');
const timerTime  = document.getElementById('timer-time');
const timerPhase = document.getElementById('timer-phase');
const startBtn   = document.getElementById('start-btn');
const curRound   = document.getElementById('cur-round');
const totRounds  = document.getElementById('tot-rounds');
const roundDots  = document.getElementById('round-dots');
const ringProg   = document.getElementById('ring-prog');

// --- Einstellungen anpassen ---
function timerAdj(key, delta) {
  if (state.phase !== 'idle') return;

  const limits = { rounds: [1, 20], work: [15, 300], rest: [5, 120] };
  const [min, max] = limits[key];
  state[key] = Math.min(max, Math.max(min, state[key] + delta));

  if (key === 'rounds') { valRounds.textContent = state.rounds; totRounds.textContent = state.rounds; }
  if (key === 'work')   { valWork.textContent = state.work; updateDisplay(state.work); }
  if (key === 'rest')   { valRest.textContent = state.rest; }

  renderDots();
}

// --- Timer steuern ---
function timerToggle() {
  if (state.phase === 'idle')   { timerStart(); }
  else if (state.phase === 'paused') { timerResume(); }
  else                           { timerPause(); }
}

function timerStart() {
  state.cur   = 0;
  state.sec   = state.work;
  state.phase = 'work';
  curRound.textContent = 1;
  timerPhase.textContent = 'ARBEIT';
  startBtn.textContent   = 'Pause';
  renderDots();
  tick();
}

function timerPause() {
  clearInterval(state.timer);
  state.prevPhase = state.phase;
  state.phase     = 'paused';
  startBtn.textContent = 'Weiter';
}

function timerResume() {
  state.phase = state.prevPhase;
  startBtn.textContent = 'Pause';
  tick();
}

function timerReset() {
  clearInterval(state.timer);
  state.phase = 'idle';
  state.cur   = 0;
  state.sec   = 0;
  curRound.textContent   = '0';
  timerPhase.textContent = 'BEREIT';
  startBtn.textContent   = 'Start';
  updateDisplay(state.work);
  setRing(1, false);
  renderDots();
}

// --- Tick ---
function tick() {
  state.timer = setInterval(() => {
    state.sec--;
    updateDisplay(state.sec);

    const total = state.phase === 'work' ? state.work : state.rest;
    setRing(state.sec / total, state.phase === 'rest');

    if (state.sec <= 0) {
      if (state.phase === 'work') {
        // Runde beendet?
        if (state.cur + 1 >= state.rounds) {
          clearInterval(state.timer);
          state.phase = 'idle';
          timerPhase.textContent = 'FERTIG! 🎉';
          startBtn.textContent   = 'Start';
          updateDisplay(0);
          setRing(0, false);
          renderDots();
          playBeep(880, 0.4);
          return;
        }
        // → Pause starten
        state.phase = 'rest';
        state.sec   = state.rest;
        timerPhase.textContent = 'PAUSE';
        playBeep(660, 0.2);

      } else {
        // → Nächste Runde
        state.cur++;
        state.phase = 'work';
        state.sec   = state.work;
        curRound.textContent   = state.cur + 1;
        timerPhase.textContent = 'ARBEIT';
        renderDots();
        playBeep(440, 0.2);
      }
    }
  }, 1000);
}

// --- Anzeige aktualisieren ---
function updateDisplay(sec) {
  const m = Math.floor(sec / 60);
  const s = sec % 60;
  timerTime.textContent = m + ':' + String(s).padStart(2, '0');
}

function setRing(ratio, isRest) {
  ringProg.style.strokeDashoffset = CIRC * (1 - Math.max(0, ratio));
  if (isRest) {
    ringProg.classList.add('rest');
  } else {
    ringProg.classList.remove('rest');
  }
}

// --- Punkte rendern ---
function renderDots() {
  roundDots.innerHTML = '';
  for (let i = 0; i < state.rounds; i++) {
    const dot = document.createElement('div');
    let cls = 'round-dot';
    if (i < state.cur) cls += ' done';
    else if (i === state.cur && state.phase !== 'idle') cls += ' active';
    dot.className = cls;
    roundDots.appendChild(dot);
  }
}

// --- Ton (Web Audio API) ---
function playBeep(freq, duration) {
  try {
    const ctx  = new (window.AudioContext || window.webkitAudioContext)();
    const osc  = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.connect(gain);
    gain.connect(ctx.destination);
    osc.frequency.value = freq;
    osc.type = 'sine';
    gain.gain.setValueAtTime(0.3, ctx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + duration);
    osc.start(ctx.currentTime);
    osc.stop(ctx.currentTime + duration);
  } catch (e) { /* Kein Audio-Support */ }
}

// --- Initialisierung ---
updateDisplay(state.work);
renderDots();
totRounds.textContent = state.rounds;
