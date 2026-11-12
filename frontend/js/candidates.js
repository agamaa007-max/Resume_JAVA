/**
 * BlindHire - Candidates Screening List Controller
 */

document.addEventListener("DOMContentLoaded", async () => {
    if (!api.requireAuth()) return;

    await loadFilterOptions();
    await fetchCandidates();
    setupFilters();
});

let currentCandidates = [];

async function loadFilterOptions() {
    const jobSelect = document.getElementById("filter-job-select");
    if (!jobSelect) return;

    try {
        const res = await api.get("/jobs");
        const jobs = res.data || [];

        const urlParams = new URLSearchParams(window.location.search);
        const preselectedJob = urlParams.get("jobId");

        jobSelect.innerHTML = `<option value="">All Job Postings</option>` +
            jobs.map(j => `<option value="${j.id}" ${preselectedJob == j.id ? 'selected' : ''}>${escapeHtml(j.title)}</option>`).join("");
    } catch (err) {
        console.error("Failed to load jobs for filter:", err);
    }
}

async function fetchCandidates() {
    const tableBody = document.getElementById("candidates-tbody");
    if (!tableBody) return;

    const jobSelect = document.getElementById("filter-job-select");
    const minScoreInput = document.getElementById("filter-min-score");
    const skillInput = document.getElementById("filter-skill-input");
    const searchInput = document.getElementById("filter-search-input");

    const params = new URLSearchParams();
    if (jobSelect && jobSelect.value) params.append("jobId", jobSelect.value);
    if (minScoreInput && minScoreInput.value) params.append("minScore", minScoreInput.value);
    if (skillInput && skillInput.value.trim()) params.append("skill", skillInput.value.trim());
    if (searchInput && searchInput.value.trim()) params.append("search", searchInput.value.trim());

    try {
        tableBody.innerHTML = `<tr><td colspan="6" style="text-align: center; padding: 32px; color: var(--text-muted);">Loading candidates from database...</td></tr>`;

        const res = await api.get(`/candidates?${params.toString()}`);
        currentCandidates = res.data || [];
        renderCandidatesTable(currentCandidates);
    } catch (err) {
        tableBody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: var(--danger); padding: 24px;">Failed to load candidates: ${err.message}</td></tr>`;
    }
}

function renderCandidatesTable(candidates) {
    const tableBody = document.getElementById("candidates-tbody");
    const countEl = document.getElementById("candidate-count-label");
    if (!tableBody) return;

    if (countEl) countEl.textContent = `Showing ${candidates.length} candidate(s)`;

    if (candidates.length === 0) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="6" style="text-align: center; padding: 48px 24px; color: var(--text-muted);">
                    <div style="font-size: 1.1rem; font-weight: 600; color: var(--navy-dark); margin-bottom: 6px;">No candidates match the criteria</div>
                    <p style="font-size: 0.85rem;">Try clearing the filter or upload new resumes to screen.</p>
                </td>
            </tr>
        `;
        return;
    }

    const selectedJobId = document.getElementById("filter-job-select")?.value || "";

    tableBody.innerHTML = candidates.map(c => {
        const score = c.score || 0;
        const scoreClass = score >= 80 ? "score-high" : (score >= 60 ? "score-medium" : "score-low");
        const skills = c.skills || [];

        return `
            <tr>
                <td>
                    <span style="font-weight: 700; color: var(--primary); font-family: monospace; font-size: 0.95rem;">
                        ${escapeHtml(c.candidateCode)}
                    </span>
                    <div style="font-size: 0.75rem; color: var(--text-muted);">${escapeHtml(c.jobTitle || 'Evaluated')}</div>
                </td>
                <td style="max-width: 280px;">
                    <div>
                        ${skills.slice(0, 4).map(s => `<span class="skill-chip">${escapeHtml(s)}</span>`).join('')}
                        ${skills.length > 4 ? `<span style="font-size: 0.75rem; color: var(--text-muted);">+${skills.length - 4} more</span>` : ''}
                    </div>
                </td>
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
                    <a href="/candidate-details.html?id=${c.id}${selectedJobId ? '&jobId=' + selectedJobId : ''}" class="btn btn-sm btn-primary">
                        Review Details
                    </a>
                </td>
            </tr>
        `;
    }).join('');
}

function setupFilters() {
    const jobSelect = document.getElementById("filter-job-select");
    const minScoreInput = document.getElementById("filter-min-score");
    const minScoreLabel = document.getElementById("min-score-val-label");
    const skillInput = document.getElementById("filter-skill-input");
    const searchInput = document.getElementById("filter-search-input");
    const resetBtn = document.getElementById("btn-reset-filters");

    if (minScoreInput && minScoreLabel) {
        minScoreInput.addEventListener("input", (e) => {
            minScoreLabel.textContent = e.target.value + "%";
            fetchCandidates();
        });
    }

    if (jobSelect) jobSelect.addEventListener("change", fetchCandidates);
    if (skillInput) skillInput.addEventListener("input", debounce(fetchCandidates, 300));
    if (searchInput) searchInput.addEventListener("input", debounce(fetchCandidates, 300));

    if (resetBtn) {
        resetBtn.addEventListener("click", () => {
            if (jobSelect) jobSelect.value = "";
            if (minScoreInput) {
                minScoreInput.value = 0;
                if (minScoreLabel) minScoreLabel.textContent = "0%";
            }
            if (skillInput) skillInput.value = "";
            if (searchInput) searchInput.value = "";
            fetchCandidates();
        });
    }
}

function debounce(func, delay) {
    let timer;
    return (...args) => {
        clearTimeout(timer);
        timer = setTimeout(() => func(...args), delay);
    };
}

function escapeHtml(str) {
    if (!str) return "";
    return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}
