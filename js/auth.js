import { api, setSession, showToast } from "./api.js";

function bindLogin(formId) {
  const form = document.getElementById(formId);
  if (!form) return;
  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    const btn = form.querySelector("button[type=submit]");
    btn.disabled = true;
    const data = Object.fromEntries(new FormData(form).entries());
    try {
      const res = await api("/auth/login", { method: "POST", body: JSON.stringify(data) });
      setSession(res.token, res.user);
      window.location.href = "/dashboard";
    } catch (err) {
      showToast(err.message, true);
      btn.disabled = false;
    }
  });
}

function bindRegister(formId) {
  const form = document.getElementById(formId);
  if (!form) return;
  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    const btn = form.querySelector("button[type=submit]");
    btn.disabled = true;
    const formData = new FormData(form);
    const data = {
      name: formData.get("name"),
      phone: formData.get("phone"),
      password: formData.get("password"),
      role: formData.get("role") || "rider",
    };
    try {
      const res = await api("/auth/register", { method: "POST", body: JSON.stringify(data) });
      setSession(res.token, res.user);
      showToast(`Welcome, ${res.user.name}!`);
      setTimeout(() => (window.location.href = "/dashboard"), 600);
    } catch (err) {
      showToast(err.message, true);
      btn.disabled = false;
    }
  });
}

bindLogin("login-form");
bindRegister("register-form");