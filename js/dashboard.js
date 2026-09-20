import { api, clearSession, requireAuth, showToast, statusBadge } from "./api.js";

async function refreshUser() {
  try {
    const user = await api("/auth/me");
    return user;
  } catch {
    clearSession();
    window.location.href = "/login";
    return null;
  }
}

function renderTopbar(user) {
  document.getElementById("user-name").textContent = user.name;
  document.getElementById("user-role").textContent = `· ${user.role.charAt(0).toUpperCase() + user.role.slice(1)}`;
  document.getElementById("user-avatar").textContent = user.name.charAt(0).toUpperCase();

  const logoutBtn = document.getElementById("logout-btn");
  logoutBtn.addEventListener("click", () => {
    clearSession();
    window.location.href = "/";
  });
}

function bindRiderForm() {
  const form = document.getElementById("book-form");
  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    const data = Object.fromEntries(new FormData(form).entries());
    if (!data.distance_km) delete data.distance_km;
    if (!data.pickup || !data.dropoff || !data.pickup.trim() || !data.dropoff.trim()) {
      showToast("Pickup and dropoff are required", true);
      return;
    }
    try {
      const booking = await api("/bookings", { method: "POST", body: JSON.stringify(data) });
      form.reset();
      showToast(`Booking #${booking.id} placed. Fare: ₹${booking.fare.toFixed(0)}`);
      loadRiderBookings();
    } catch (err) {
      showToast(err.message, true);
    }
  });
}

async function loadRiderBookings() {
  const box = document.getElementById("rider-bookings");
  try {
    const bookings = await api("/bookings/my");
    if (!bookings.length) {
      box.innerHTML = '<div class="empty">No bookings yet. Book your first ride above.</div>';
      return;
    }
    box.innerHTML = `
      <table class="table">
        <thead><tr><th>#</th><th>Route</th><th>Fare</th><th>Status</th><th></th></tr></thead>
        <tbody>
          ${bookings
            .map(
              (b) => `
            <tr>
              <td>${b.id}</td>
              <td><strong>${escapeHtml(b.pickup)}</strong> → ${escapeHtml(b.dropoff)}<br>
                  <small>Driver: ${b.driver_name ? escapeHtml(b.driver_name) : "Not assigned yet"}</small></td>
              <td>₹${b.fare.toFixed(0)}</td>
              <td>${statusBadge(b.status)}</td>
              <td>${
                b.status === "pending" || b.status === "accepted"
                  ? `<button class="btn btn-sm btn-danger" data-cancel="${b.id}">Cancel</button>`
                  : ""
              }</td>
            </tr>`
            )
            .join("")}
        </tbody>
      </table>`;
    box.querySelectorAll("[data-cancel]").forEach((btn) => {
      btn.addEventListener("click", () => updateBooking(btn.dataset.cancel, "cancelled", loadRiderBookings));
    });
  } catch (err) {
    box.innerHTML = `<div class="empty">Could not load bookings: ${escapeHtml(err.message)}</div>`;
  }
}

function renderDriverPanels() {
  document.getElementById("pending-title").textContent = "Open bookings (first-come, first-served)";
  document.getElementById("my-title").textContent = "My rides";

  loadPendingBookings();
  loadDriverBookings();
}

async function loadPendingBookings() {
  const box = document.getElementById("pending-bookings");
  try {
    const bookings = await api("/bookings/pending");
    if (!bookings.length) {
      box.innerHTML = '<div class="empty">No open bookings right now. Check back soon.</div>';
      return;
    }
    box.innerHTML = bookings
      .map(
        (b) => `
        <div class="card" style="margin-bottom:14px">
          <div class="row spread">
            <div><strong>#${b.id}</strong> · ${escapeHtml(b.pickup)} → ${escapeHtml(b.dropoff)}</div>
            ${statusBadge(b.status)}
          </div>
          <div class="row spread" style="margin-top:6px">
            <span class="muted small">Rider: ${escapeHtml(b.user_name)} · Fare: ₹${b.fare.toFixed(0)}</span>
            <button class="btn btn-sm btn-success" data-accept="${b.id}">Accept</button>
          </div>
        </div>`
      )
      .join("");
    box.querySelectorAll("[data-accept]").forEach((btn) => {
      btn.addEventListener("click", () => updateBooking(btn.dataset.accept, "accepted", loadPendingBookings));
    });
  } catch (err) {
    box.innerHTML = `<div class="empty">${escapeHtml(err.message)}</div>`;
  }
}

async function loadDriverBookings() {
  const box = document.getElementById("my-bookings");
  try {
    const bookings = await api("/bookings/my");
    if (!bookings.length) {
      box.innerHTML = '<div class="empty">You have no accepted rides yet.</div>';
      return;
    }
    box.innerHTML = bookings
      .map(
        (b) => `
        <div class="card" style="margin-bottom:14px">
          <div class="row spread">
            <div><strong>#${b.id}</strong> · ${escapeHtml(b.pickup)} → ${escapeHtml(b.dropoff)}</div>
            ${statusBadge(b.status)}
          </div>
          <div class="row spread" style="margin-top:6px">
            <span>Rider: ${escapeHtml(b.user_name)} · Fare: ₹${b.fare.toFixed(0)}</span>
            ${
              b.status === "accepted"
                ? `<button class="btn btn-sm btn-amber" data-complete="${b.id}">Complete ride</button>`
                : ""
            }
          </div>
        </div>`
      )
      .join("");
    box.querySelectorAll("[data-complete]").forEach((btn) => {
      btn.addEventListener("click", () =>
        updateBooking(btn.dataset.complete, "completed", loadDriverBookings)
      );
    });
  } catch (err) {
    box.innerHTML = `<div class="empty">${escapeHtml(err.message)}</div>`;
  }
}

async function updateBooking(id, status, reload) {
  try {
    const booking = await api(`/bookings/${id}`, {
      method: "PATCH",
      body: JSON.stringify({ status }),
    });
    showToast(`Booking #${booking.id} is now ${booking.status}`);
    loadPendingBookings();
    loadDriverBookings();
    if (typeof reload === "function") reload();
  } catch (err) {
    showToast(err.message, true);
  }
}

function escapeHtml(value) {
  return String(value ?? "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

const loaded = { rider: false, driver: false };

function switchView(view) {
  document.querySelectorAll(".tab-btn").forEach((btn) => {
    btn.classList.toggle("active", btn.dataset.view === view);
  });
  document.getElementById("rider-panel").hidden = view !== "rider";
  document.getElementById("driver-panel").hidden = view !== "driver";

  if (view === "rider") {
    document.getElementById(
      "role-strip"
    ).innerHTML = `Rider view — Khudol amadi book toudou chatni. (Book a ride and track it.)`;
    if (!loaded.rider) {
      loaded.rider = true;
      bindRiderForm();
    }
    loadRiderBookings();
  } else {
    document.getElementById(
      "role-strip"
    ).innerHTML = `Driver view — Pending hirings thang and accept toujou. (Pick up riders near you.)`;
    if (!loaded.driver) {
      loaded.driver = true;
      renderDriverPanels();
    } else {
      loadPendingBookings();
      loadDriverBookings();
    }
  }
}

function setupTabs(initialView) {
  document.querySelectorAll(".tab-btn").forEach((btn) => {
    btn.addEventListener("click", () => switchView(btn.dataset.view));
  });
  switchView(initialView);
}

if (!requireAuth()) {
  throw new Error("redirecting");
}

const user = await refreshUser();
if (user) {
  renderTopbar(user);
  setupTabs(user.role === "driver" ? "driver" : "rider");
}