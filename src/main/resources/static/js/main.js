// Plan ein-/ausklappen
function togglePlan(index) {
  const list = document.getElementById('plan-' + index);
  const btn  = document.getElementById('btn-' + index);
  if (!list) return;
  if (list.style.display === 'none' || !list.style.display) {
    list.style.display = 'block';
    btn.textContent = 'Schließen ▲';
  } else {
    list.style.display = 'none';
    btn.textContent = 'Anzeigen ▼';
  }
}

// Neuesten Plan automatisch öffnen
document.addEventListener('DOMContentLoaded', function () {
  const firstPlan = document.getElementById('plan-0');
  const firstBtn  = document.getElementById('btn-0');
  if (firstPlan && firstBtn) {
    firstPlan.style.display = 'block';
    firstBtn.textContent = 'Schließen ▲';
  }

  // PWA Service Worker registrieren
  if ('serviceWorker' in navigator) {
    navigator.serviceWorker.register('/sw.js').catch(() => {});
  }

  // PWA Install Banner
  let deferredPrompt;
  window.addEventListener('beforeinstallprompt', (e) => {
    e.preventDefault();
    deferredPrompt = e;
    showInstallBanner(deferredPrompt);
  });
});

function showInstallBanner(prompt) {
  if (document.getElementById('pwa-banner')) return;
  const banner = document.createElement('div');
  banner.id = 'pwa-banner';
  banner.className = 'pwa-banner';
  banner.innerHTML = `
    <span>📱 FitForge als App installieren</span>
    <button onclick="installPWA()">Installieren</button>
    <button class="close" onclick="document.getElementById('pwa-banner').remove()">✕</button>
  `;
  document.body.appendChild(banner);
  window._pwaPrompt = prompt;
}

function installPWA() {
  if (window._pwaPrompt) {
    window._pwaPrompt.prompt();
    window._pwaPrompt.userChoice.then(() => {
      document.getElementById('pwa-banner')?.remove();
    });
  }
}
