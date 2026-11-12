/**
 * BlindHire - Job Management Controller
 */

document.addEventListener("DOMContentLoaded", () => {
    if (!api.requireAuth()) return;

    if (document.getElementById("jobs-grid-container")) {
        loadJobs();
        setupSearch();
    }

    if (document.getElementById("create-job-form")) {
        setupCreateJobForm();
    }
});

let allJobs = [];

async function loadJobs() {
    const container = document.getElementById("jobs-grid-container");
    if (!container) return;

    try {
        const res = await api.get("/jobs");
        allJobs = res.data || [];
        renderJobs(allJobs);
    } catch (err) {
        container.innerHTML = `<div class="card card-body" style="text-align: center; color: var(--danger);">Failed to load jobs: ${err.message}</div>`;
    }
}

function renderJobs(jobs) {
    const container = document.getElementById("jobs-grid-container");
    if (!container) return;

    if (jobs.length === 0) {
        container.innerHTML = `
            <div class="card" style="grid-column: 1 / -1; text-align: center; padding: 48px 24px;">
                <h3 style="color: var(--navy-dark); margin-bottom: 8px;">No Job Postings Found</h3>
                <p style="color: var(--text-muted); margin-bottom: 20px;">Create your first vacancy to start receiving and screening resumes anonymously.</p>
                <div>
                    <a href="/create-job.html" class="btn btn-primary">+ Create New Job</a>
                </div>
            </div>
        `;
        return;
    }

    container.innerHTML = jobs.map(job => {
        const skills = job.skills || [];
        const reqSkills = skills.filter(s => s.skillType === 'REQUIRED');
        const prefSkills = skills.filter(s => s.skillType === 'PREFERRED');

        return `
            <div class="job-card">
                <div>
                    <div class="job-header">
                        <h3 class="job-title">${escapeHtml(job.title)}</h3>
                        <span class="badge ${job.status === 'ACTIVE' ? 'badge-success' : 'badge-gray'}">${job.status}</span>
                    </div>

                    <div class="job-meta-row">
                        <span>Min Exp: <strong>${job.minimumExperience} yrs</strong></span>
                        <span>•</span>
                        <span>Edu: <strong>${escapeHtml(job.minimumEducation || 'Any')}</strong></span>
                    </div>

                    <p class="job-desc">${escapeHtml(job.description)}</p>
                </div>

                <div class="job-skills-section">
                    <div style="margin-bottom: 8px;">
                        <span style="font-size: 0.75rem; text-transform: uppercase; font-weight: 700; color: var(--text-secondary);">Required Skills:</span>
                        <div style="margin-top: 4px;">
                            ${reqSkills.length > 0 ? reqSkills.map(s => `<span class="skill-chip">${escapeHtml(s.skillName)}</span>`).join('') : '<span style="font-size: 0.8rem; color: var(--text-muted);">None specified</span>'}
                        </div>
                    </div>
                    ${prefSkills.length > 0 ? `
                        <div style="margin-bottom: 12px;">
                            <span style="font-size: 0.75rem; text-transform: uppercase; font-weight: 700; color: var(--text-muted);">Preferred:</span>
                            <div style="margin-top: 4px;">
                                ${prefSkills.map(s => `<span class="skill-chip" style="background:#f1f5f9;">${escapeHtml(s.skillName)}</span>`).join('')}
                            </div>
                        </div>
                    ` : ''}

                    <div style="display: flex; gap: 10px; margin-top: 14px;">
                        <a href="/candidates.html?jobId=${job.id}" class="btn btn-sm btn-primary" style="flex: 1;">Screen Applicants</a>
                        <a href="/upload-resume.html?jobId=${job.id}" class="btn btn-sm btn-outline">+ Resume</a>
                        <button onclick="deleteJob(${job.id})" class="btn btn-sm btn-secondary" title="Delete Job" style="color: var(--danger);">✕</button>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

function setupSearch() {
    const searchInput = document.getElementById("job-search-input");
    if (!searchInput) return;

    searchInput.addEventListener("input", (e) => {
        const query = e.target.value.toLowerCase().trim();
        const filtered = allJobs.filter(j => 
            j.title.toLowerCase().includes(query) ||
            j.description.toLowerCase().includes(query)
        );
        renderJobs(filtered);
    });
}

async function deleteJob(id) {
    if (!confirm("Are you sure you want to delete this job posting? Associated screening records will be removed.")) {
        return;
    }

    try {
        await api.delete(`/jobs/${id}`);
        showToast("Job posting deleted.", "success");
        loadJobs();
    } catch (err) {
        showToast("Failed to delete job: " + err.message, "error");
    }
}

function setupCreateJobForm() {
    const form = document.getElementById("create-job-form");
    if (!form) return;

    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        const title = document.getElementById("job-title").value.trim();
        const description = document.getElementById("job-description").value.trim();
        const minimumEducation = document.getElementById("job-education").value.trim();
        const minimumExperience = parseInt(document.getElementById("job-experience").value) || 0;
        const requiredSkillsStr = document.getElementById("job-required-skills").value.trim();
        const preferredSkillsStr = document.getElementById("job-preferred-skills").value.trim();

        if (!title || !description) {
            showToast("Job title and description are required.", "error");
            return;
        }

        const requiredSkills = requiredSkillsStr ? requiredSkillsStr.split(",").map(s => s.trim()).filter(Boolean) : [];
        const preferredSkills = preferredSkillsStr ? preferredSkillsStr.split(",").map(s => s.trim()).filter(Boolean) : [];

        const payload = {
            title,
            description,
            minimumEducation,
            minimumExperience,
            requiredSkills,
            preferredSkills,
            status: "ACTIVE"
        };

        const submitBtn = form.querySelector("button[type='submit']");
        try {
            submitBtn.disabled = true;
            submitBtn.textContent = "Publishing Job...";

            await api.post("/jobs", payload);
            showToast("Job vacancy published successfully!", "success");

            setTimeout(() => {
                window.location.href = "/jobs.html";
            }, 800);
        } catch (err) {
            showToast(err.message, "error");
        } finally {
            submitBtn.disabled = false;
            submitBtn.textContent = "Create Job Posting";
        }
    });
}

function escapeHtml(str) {
    if (!str) return "";
    return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}
