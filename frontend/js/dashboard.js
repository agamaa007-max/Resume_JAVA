/**
 * BlindHire - Dashboard Controller
 * Fetches and renders live database metrics, recent jobs, and screened candidates.
 */

document.addEventListener("DOMContentLoaded", async () => {
    if (!api.requireAuth()) return;

    await loadDashboardStats();
    await loadRecentJobs();
    await loadRecentCandidates();
});

async function loadDashboardStats() {
    try {
        const res = await api.get("/screening/dashboard");
        const stats = res.data || {};

        document.getElementById("stat-total-jobs").textContent = stats.totalJobs || 0;
        document.getElementById("stat-total-candidates").textContent = stats.totalCandidates || 0;
        document.getElementById("stat-total-resumes").textContent = stats.totalResumes || 0;
        document.getElementById("stat-avg-score").textContent = (stats.averageScore ? stats.averageScore : 0) + "%";
    } catch (err) {
        console.error("Failed to load dashboard stats:", err);
        showToast("Could not load dashboard statistics from database.", "error");
    }
}

async function loadRecentJobs() {
    const tableBody = document.getElementById("recent-jobs-tbody");
    if (!tableBody) return;

    try {
        const res = await api.get("/jobs");
        const jobs = res.data || [];

        if (jobs.length === 0) {
            tableBody.innerHTML = `<tr><td colspan="4" style="text-align: center; color: var(--text-muted); padding: 24px;">No job postings created yet.</td></tr>`;
            return;
        }

        tableBody.innerHTML = jobs.slice(0, 5).map(job => `
            <tr>
                <td><strong>${escapeHtml(job.title)}</strong></td>
                <td><span class="badge ${job.status === 'ACTIVE' ? 'badge-success' : 'badge-gray'}">${job.status}</span></td>
                <td>${job.minimumExperience} yrs</td>
                <td>
                    <a href="/candidates.html?jobId=${job.id}" class="btn btn-sm btn-outline">View Applicants</a>
                </td>
            </tr>
        `).join("");
    } catch (err) {
        tableBody.innerHTML = `<tr><td colspan="4" style="text-align: center; color: var(--danger);">Failed to load jobs.</td></tr>`;
    }
}

async function loadRecentCandidates() {
    const tableBody = document.getElementById("recent-candidates-tbody");
    if (!tableBody) return;

    try {
        const res = await api.get("/candidates");
        const candidates = res.data || [];

        if (candidates.length === 0) {
            tableBody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: var(--text-muted); padding: 24px;">No candidates screened yet. Upload a resume to begin.</td></tr>`;
            return;
        }

        tableBody.innerHTML = candidates.slice(0, 5).map(c => {
            const score = c.score || 0;
            const scoreClass = score >= 80 ? "score-high" : (score >= 60 ? "score-medium" : "score-low");
            return `
                <tr>
                    <td><strong>${escapeHtml(c.candidateCode)}</strong></td>
                    <td>${escapeHtml(c.education || "-")}</td>
                    <td>${escapeHtml(c.experience || "-")}</td>
                    <td>
                        <div class="score-badge">
                            <span class="${scoreClass}">${score}%</span>
                            <div class="score-bar-bg">
                                <div class="score-bar-fill ${scoreClass}" style="width: ${Math.min(100, score)}%;"></div>
                            </div>
                        </div>
                    </td>
                    <td>
                        <a href="/candidate-details.html?id=${c.id}" class="btn btn-sm btn-secondary">Review</a>
                    </td>
                </tr>
            `;
        }).join("");
    } catch (err) {
        tableBody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: var(--danger);">Failed to load candidates.</td></tr>`;
    }
}

function escapeHtml(str) {
    if (!str) return "";
    return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}
