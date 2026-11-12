/**
 * BlindHire - Authentication Controller (Login & Registration)
 */

document.addEventListener("DOMContentLoaded", () => {
    // If already logged in, redirect away from login/register to dashboard
    if (api.getToken()) {
        const path = window.location.pathname;
        if (path.includes("login.html") || path.includes("register.html")) {
            window.location.href = "/dashboard.html";
            return;
        }
    }

    // --- Login Form Handler ---
    const loginForm = document.getElementById("login-form");
    if (loginForm) {
        loginForm.addEventListener("submit", async (e) => {
            e.preventDefault();
            const email = document.getElementById("login-email").value.trim();
            const password = document.getElementById("login-password").value;
            const submitBtn = loginForm.querySelector("button[type='submit']");

            if (!email || !password) {
                showToast("Please enter both email and password.", "error");
                return;
            }

            try {
                submitBtn.disabled = true;
                submitBtn.textContent = "Signing In...";

                const res = await api.post("/auth/login", { email, password });
                showToast("Login successful! Redirecting...", "success");
                api.saveAuth(res.data.token, res.data);

                setTimeout(() => {
                    window.location.href = "/dashboard.html";
                }, 600);
            } catch (err) {
                showToast(err.message, "error");
            } finally {
                submitBtn.disabled = false;
                submitBtn.textContent = "Sign In";
            }
        });
    }

    // --- Registration Form Handler ---
    const registerForm = document.getElementById("register-form");
    if (registerForm) {
        registerForm.addEventListener("submit", async (e) => {
            e.preventDefault();
            const fullName = document.getElementById("reg-name").value.trim();
            const email = document.getElementById("reg-email").value.trim();
            const password = document.getElementById("reg-password").value;
            const confirmPassword = document.getElementById("reg-confirm-password").value;
            const submitBtn = registerForm.querySelector("button[type='submit']");

            if (!fullName || !email || !password) {
                showToast("Please complete all required fields.", "error");
                return;
            }

            if (password !== confirmPassword) {
                showToast("Passwords do not match.", "error");
                return;
            }

            if (password.length < 6) {
                showToast("Password must be at least 6 characters.", "error");
                return;
            }

            try {
                submitBtn.disabled = true;
                submitBtn.textContent = "Creating Account...";

                const res = await api.post("/auth/register", { fullName, email, password });
                showToast("Account created successfully! Logging you in...", "success");
                api.saveAuth(res.data.token, res.data);

                setTimeout(() => {
                    window.location.href = "/dashboard.html";
                }, 800);
            } catch (err) {
                showToast(err.message, "error");
            } finally {
                submitBtn.disabled = false;
                submitBtn.textContent = "Create Account";
            }
        });
    }
});
