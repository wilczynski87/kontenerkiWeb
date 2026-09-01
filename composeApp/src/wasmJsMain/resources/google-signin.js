(function () {
  const scriptId = "magazynki-google-gsi-script";
  let initPromise = null;

  function readClientId() {
    const meta = document.querySelector("meta[name='google-oauth-client-id']");
    const fromMeta = meta?.getAttribute("content")?.trim();
    if (fromMeta) return fromMeta;
    return window.__GOOGLE_OAUTH_CLIENT_ID__?.trim?.() || "";
  }

  function ensureScriptLoaded() {
    if (window.google?.accounts?.id) {
      return Promise.resolve();
    }
    if (initPromise) return initPromise;

    initPromise = new Promise((resolve, reject) => {
      if (document.getElementById(scriptId)) {
        const wait = setInterval(() => {
          if (window.google?.accounts?.id) {
            clearInterval(wait);
            resolve();
          }
        }, 50);
        setTimeout(() => {
          clearInterval(wait);
          if (window.google?.accounts?.id) resolve();
          else reject(new Error("Google Identity Services failed to load"));
        }, 10000);
        return;
      }

      const script = document.createElement("script");
      script.id = scriptId;
      script.src = "https://accounts.google.com/gsi/client";
      script.async = true;
      script.defer = true;
      script.onload = () => resolve();
      script.onerror = () => reject(new Error("Failed to load Google Identity Services"));
      document.head.appendChild(script);
    });

    return initPromise;
  }

  window.magazynkiGoogleSignIn = {
    isAvailable() {
      return !!readClientId();
    },
    requestIdToken(clientId) {
      const effectiveClientId = (clientId || readClientId() || "").trim();
      if (!effectiveClientId) {
        return Promise.reject(new Error("Google OAuth client ID is not configured"));
      }

      return ensureScriptLoaded().then(() => new Promise((resolve, reject) => {
        let settled = false;
        const finish = (fn, value) => {
          if (settled) return;
          settled = true;
          fn(value);
        };

        window.google.accounts.id.initialize({
          client_id: effectiveClientId,
          callback: (response) => {
            const token = response?.credential?.trim?.() || "";
            if (!token) {
              finish(reject, new Error("Empty Google ID token"));
            } else {
              finish(resolve, token);
            }
          },
          auto_select: false,
          cancel_on_tap_outside: true,
        });

        window.google.accounts.id.prompt((notification) => {
          if (notification.isNotDisplayed() || notification.isSkippedMoment()) {
            finish(reject, new Error("Google Sign-In prompt unavailable"));
          }
        });

        setTimeout(() => finish(reject, new Error("Google Sign-In timed out")), 120000);
      }));
    },
  };
})();
