/**
 * BlindHire - Resume Upload & Text Extraction Controller
 */

document.addEventListener("DOMContentLoaded", async () => {
    if (!api.requireAuth()) return;

    await populateJobDropdown();
    setupUploadForm();
});

async function populateJobDropdown() {
    const select = document.getElementById("upload-job-select");
    if (!select) return;

    try {
        const res = await api.get("/jobs");
        const jobs = res.data || [];

        // Check if pre-selected in URL
        const urlParams = new URLSearchParams(window.location.search);
        const preselectedJobId = urlParams.get("jobId");

        select.innerHTML = `<option value="">-- Choose Job Opening --</option>` +
            jobs.map(j => `
                <option value="${j.id}" ${preselectedJobId == j.id ? 'selected' : ''}>
                    ${escapeHtml(j.title)} (Min ${j.minimumExperience} yrs exp)
                </option>
            `).join("");
    } catch (err) {
        showToast("Could not load jobs list.", "error");
    }
}

function setupUploadForm() {
    const form = document.getElementById("resume-upload-form");
    const fileInput = document.getElementById("resume-file-input");
    const fileDropzone = document.getElementById("file-dropzone");
    const fileSelectedLabel = document.getElementById("file-selected-label");

    if (!form || !fileInput) return;

    // Dropzone click & drag-and-drop
    if (fileDropzone) {
        fileDropzone.addEventListener("click", () => fileInput.click());
        fileDropzone.addEventListener("dragover", (e) => {
            e.preventDefault();
            fileDropzone.style.borderColor = "var(--primary)";
        });
        fileDropzone.addEventListener("dragleave", () => {
            fileDropzone.style.borderColor = "var(--border-color)";
        });
        fileDropzone.addEventListener("drop", (e) => {
            e.preventDefault();
            fileDropzone.style.borderColor = "var(--border-color)";
            if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
                fileInput.files = e.dataTransfer.files;
                updateFileLabel();
            }
        });
    }

    fileInput.addEventListener("change", updateFileLabel);

    function updateFileLabel() {
        if (fileInput.files && fileInput.files.length > 0) {
            const f = fileInput.files[0];
            fileSelectedLabel.textContent = `Selected: ${f.name} (${Math.round(f.size / 1024)} KB)`;
            fileSelectedLabel.style.display = "block";
        }
    }

    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        const jobId = document.getElementById("upload-job-select").value;
        if (!jobId) {
            showToast("Please select a target job opening.", "error");
            return;
        }

        if (!fileInput.files || fileInput.files.length === 0) {
            showToast("Please choose a PDF or DOCX resume to upload.", "error");
            return;
        }

        const file = fileInput.files[0];
        const validExtensions = [".pdf", ".docx", ".txt"];
        const ext = file.name.substring(file.name.lastIndexOf(".")).toLowerCase();
        if (!validExtensions.includes(ext)) {
            showToast("Only PDF and DOCX formats are supported.", "error");
            return;
        }

        if (file.size > 10 * 1024 * 1024) { // 10MB limit
            showToast("File size exceeds 10MB limit.", "error");
            return;
        }

        const submitBtn = form.querySelector("button[type='submit']");
        const progressBox = document.getElementById("upload-progress-box");
        const resultBox = document.getElementById("screening-result-box");

        try {
            submitBtn.disabled = true;
            if (progressBox) progressBox.style.display = "block";
            if (resultBox) resultBox.style.display = "none";

            // Convert to base64
            const base64String = await readFileAsBase64(file);

            const payload = {
                jobId: parseInt(jobId),
                fileName: file.name,
                fileBase64: base64String
            };

            const res = await api.post("/resumes/upload", payload);
            showToast("Resume parsed and screened successfully!", "success");

            if (resultBox) {
                renderScreeningResult(res.data);
                resultBox.style.display = "block";
                resultBox.scrollIntoView({ behavior: "smooth" });
            }

            form.reset();
            fileSelectedLabel.style.display = "none";
        } catch (err) {
            showToast(err.message, "error");
        } finally {
            submitBtn.disabled = false;
            if (progressBox) progressBox.style.display = "none";
        }
    });
}

function readFileAsBase64(file) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => resolve(reader.result);
        reader.onerror = (error) => reject(error);
        reader.readAsDataURL(file);
    });
}

function renderScreeningResult(data) {
    const resultBox = document.getElementById("screening-result-box");
    if (!resultBox || !data) return;

    const score = data.finalScore || 0;
    const scoreColor = score >= 80 ? "var(--success)" : (score >= 60 ? "var(--warning)" : "var(--danger)");

    resultBox.innerHTML = `
        <div class="card" style="border: 2px solid #3b82f6;">
            <div class="card-header" style="background: #eff6ff;">
                <div>
                    <h3 class="card-title">Screening Complete: ${escapeHtml(data.candidateCode)}</h3>
                    <p class="card-subtitle">Screened against job: <strong>${escapeHtml(data.jobTitle)}</strong></p>
                </div>
                <div style="text-align: right;">
                    <div style="font-size: 2rem; font-weight: 800; color: ${scoreColor}; line-height: 1;">${score}%</div>
                    <span style="font-size: 0.75rem; text-transform: uppercase; color: var(--text-muted); font-weight: 700;">Overall Match</span>
                </div>
            </div>
            <div class="card-body">
                <div class="score-breakdown-card" style="margin-bottom: 20px;">
                    <div class="breakdown-item">
                        <div class="breakdown-header"><span>Skills (50%)</span></div>
                        <div class="breakdown-value">${data.skillScore}%</div>
                    </div>
                    <div class="breakdown-item">
                        <div class="breakdown-header"><span>Experience (25%)</span></div>
                        <div class="breakdown-value">${data.experienceScore}%</div>
                    </div>
                    <div class="breakdown-item">
                        <div class="breakdown-header"><span>Education (15%)</span></div>
                        <div class="breakdown-value">${data.educationScore}%</div>
                    </div>
                    <div class="breakdown-item">
                        <div class="breakdown-header"><span>Projects (10%)</span></div>
                        <div class="breakdown-value">${data.projectScore}%</div>
                    </div>
                </div>

                <div style="margin-bottom: 16px;">
                    <strong style="font-size: 0.85rem; color: var(--navy-dark);">Matched Skills:</strong>
                    <div style="margin-top: 6px;">
                        ${(data.matchedSkills && data.matchedSkills.length > 0) ? data.matchedSkills.map(s => `<span class="skill-chip matched">✓ ${escapeHtml(s)}</span>`).join('') : '<span style="color:var(--text-muted);">No direct required skills matched</span>'}
                    </div>
                </div>

                <div style="margin-bottom: 20px;">
                    <strong style="font-size: 0.85rem; color: var(--navy-dark);">Missing Skills:</strong>
                    <div style="margin-top: 6px;">
                        ${(data.missingSkills && data.missingSkills.length > 0) ? data.missingSkills.map(s => `<span class="skill-chip missing">✕ ${escapeHtml(s)}</span>`).join('') : '<span style="color:var(--success);">All required skills present!</span>'}
                    </div>
                </div>

                <div class="explanation-box">
                    <strong>Score Explanation Breakdown:</strong>
                    ${escapeHtml(data.explanation || '')}
                </div>
            </div>
            <div class="card-footer">
                <a href="/candidates.html?jobId=${data.jobId}" class="btn btn-secondary">View All Applicants</a>
                <a href="/candidate-details.html?id=${data.candidateId}&jobId=${data.jobId}" class="btn btn-primary">Open Candidate Details</a>
            </div>
        </div>
    `;
}

function escapeHtml(str) {
    if (!str) return "";
    return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}
