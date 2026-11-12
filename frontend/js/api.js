/**
 * BlindHire - Centralized API & Authentication Client
 * Handles HTTP requests, session tokens, error routing, and toast notifications.
 */

const API_BASE = "/api";

const api = {
    // --- Token & User State Management ---
    getToken() {
        return localStorage.getItem("blindhire_token");
    },

    getUser() {
        const u = localStorage.getItem("blindhire_user");
        try {
            return u ? JSON.parse(u) : null;
        } catch (e) {
            return null;
        }
    },

    saveAuth(token, user) {
        localStorage.setItem("blindhire_token", token);
        localStorage.setItem("blindhire_user", JSON.stringify(user));
    },

    clearAuth() {
        localStorage.removeItem("blindhire_token");
        localStorage.removeItem("blindhire_user");
    },

    requireAuth() {
        const token = this.getToken();
        if (!token) {
            window.location.href = "/login.html";
            return false;
        }
        return true;
    },

    async logout() {
        try {
            await this.post("/auth/logout", {});
        } catch (ignored) {}
        this.clearAuth();
        window.location.href = "/login.html";
    },

    // --- Core Request Dispatcher ---
    async request(endpoint, options = {}) {
        const url = endpoint.startsWith("http") ? endpoint : `${API_BASE}${endpoint}`;
        const headers = {
            "Content-Type": "application/json",
            ...(options.headers || {})
        };

        const token = this.getToken();
        if (token) {
            headers["Authorization"] = `Bearer ${token}`;
        }

        try {
            const response = await fetch(url, {
                ...options,
                headers
            });

            // Handle unauthorized redirect
            if (response.status === 401 && !endpoint.includes("/auth/login")) {
                this.clearAuth();
                window.location.href = "/login.html";
                throw new Error("Session expired. Please log in again.");
            }

            const data = await response.json().catch(() => null);

            if (!response.ok) {
                const message = (data && data.message) ? data.message : `Server returned HTTP ${response.status}`;
                throw new Error(message);
            }

            return data;
        } catch (err) {
            console.error(`[API ERROR] ${endpoint}:`, err);
            throw err;
        }
    },

    get(endpoint) {
        return this.request(endpoint, { method: "GET" });
    },

    post(endpoint, body) {
        return this.request(endpoint, {
            method: "POST",
            body: JSON.stringify(body)
        });
    },

    put(endpoint, body) {
        return this.request(endpoint, {
            method: "PUT",
            body: JSON.stringify(body)
        });
    },

    delete(endpoint) {
        return this.request(endpoint, { method: "DELETE" });
    }
};

// --- Toast Notification Manager ---
function showToast(message, type = "info") {
    let container = document.getElementById("toast-container");
    if (!container) {
        container = document.createElement("div");
        container.id = "toast-container";
        document.body.appendChild(container);
    }

    const toast = document.createElement("div");
    toast.className = `toast toast-${type}`;
    toast.textContent = message;

    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = "0";
        toast.style.transform = "translateY(-10px)";
        toast.style.transition = "all 0.3s ease";
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

// --- Layout Initializer ---
document.addEventListener("DOMContentLoaded", () => {
    // Populate active user info in sidebar if present
    const user = api.getUser();
    const userNameEl = document.getElementById("header-user-name");
    const sidebarUserEl = document.getElementById("sidebar-user-name");
    const avatarEl = document.getElementById("sidebar-user-avatar");

    if (user) {
        if (userNameEl) userNameEl.textContent = user.fullName || user.email;
        if (sidebarUserEl) sidebarUserEl.textContent = user.fullName || user.email;
        if (avatarEl && user.fullName) {
            const initials = user.fullName.split(" ").map(n => n[0]).join("").toUpperCase().substring(0, 2);
            avatarEl.textContent = initials;
        }
    }

    // Set active link in sidebar navigation
    const currentPath = window.location.pathname;
    const navLinks = document.querySelectorAll(".nav-item");
    navLinks.forEach(link => {
        const href = link.getAttribute("href");
        if (href && (currentPath.endsWith(href) || (href === "/dashboard.html" && currentPath === "/"))) {
            link.classList.add("active");
        }
    });
});
