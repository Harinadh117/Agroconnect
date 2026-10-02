// AgroConnect - Single Page Application
let currentUser = null;

document.addEventListener('DOMContentLoaded', async () => {
    const res = await fetch('/api/auth/me');
    if (res.ok) { currentUser = await res.json(); showDashboard(); }
});

// ========== AUTH ==========
function showAuthTab(tab) {
    document.querySelectorAll('.auth-tab').forEach(t => t.classList.remove('active'));
    document.getElementById('loginForm').classList.toggle('hidden', tab !== 'login');
    document.getElementById('registerForm').classList.toggle('hidden', tab !== 'register');
    event.target.classList.add('active');
}

function toggleRoleFields() {
    const role = document.getElementById('regRole').value;
    document.getElementById('workerFields').classList.toggle('hidden', role !== 'WORKER');
    document.getElementById('kavuluFields').classList.toggle('hidden', role !== 'KAVULU');
}

async function login() {
    const phone = document.getElementById('loginPhone').value;
    const password = document.getElementById('loginPassword').value;
    const res = await fetch('/api/auth/login', {
        method: 'POST', headers: {'Content-Type':'application/json'},
        body: JSON.stringify({phone, password})
    });
    const data = await res.json();
    if (res.ok) { currentUser = data; showDashboard(); }
    else { showMsg('loginMsg', data.error, 'error'); }
}

async function register() {
    const body = {
        name: document.getElementById('regName').value,
        phone: document.getElementById('regPhone').value,
        password: document.getElementById('regPassword').value,
        role: document.getElementById('regRole').value,
        village: document.getElementById('regVillage').value,
        district: document.getElementById('regDistrict').value,
        state: document.getElementById('regState').value
    };
    if (!body.name || !body.phone || !body.password || !body.role) {
        showMsg('regMsg', 'Please fill all required fields', 'error'); return;
    }
    if (body.role === 'WORKER') {
        body.workType = document.getElementById('regWorkType').value;
        body.experience = document.getElementById('regExperience').value;
        body.availableDays = document.getElementById('regAvailDays').value;
    }
    if (body.role === 'KAVULU') {
        body.preferredCrop = document.getElementById('regCrop').value;
        body.experience = document.getElementById('regKavuluExp').value;
    }
    const res = await fetch('/api/auth/register', {
        method: 'POST', headers: {'Content-Type':'application/json'},
        body: JSON.stringify(body)
    });
    const data = await res.json();
    if (res.ok) { currentUser = data; showDashboard(); }
    else { showMsg('regMsg', data.error, 'error'); }
}

async function logout() {
    await fetch('/api/auth/logout', {method:'POST'});
    currentUser = null;
    document.getElementById('authSection').classList.remove('hidden');
    document.getElementById('dashboardSection').classList.add('hidden');
}

// ========== DASHBOARD ==========
function showDashboard() {
    document.getElementById('authSection').classList.add('hidden');
    document.getElementById('dashboardSection').classList.remove('hidden');
    document.getElementById('userName').textContent = currentUser.name;
    document.getElementById('userRole').textContent = currentUser.role;

    const role = currentUser.role;
    if (role === 'FARMER') renderFarmerDashboard();
    else if (role === 'WORKER') renderWorkerDashboard();
    else if (role === 'KAVULU') renderKavuluDashboard();
    else if (role === 'ADMIN') renderAdminDashboard();
}

function setTabs(tabList, defaultTab) {
    const tabsEl = document.getElementById('tabs');
    tabsEl.innerHTML = tabList.map(t =>
        `<button class="tab${t.id===defaultTab?' active':''}" onclick="switchTab('${t.id}')">${t.label}</button>`
    ).join('');
}

function switchTab(tabId) {
    document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
    event.target.classList.add('active');
    document.querySelectorAll('.tab-content').forEach(tc => tc.classList.remove('active'));
    const el = document.getElementById(tabId);
    if (el) el.classList.add('active');
}

// ========== FARMER DASHBOARD ==========
function renderFarmerDashboard() {
    setTabs([
        {id:'postJob', label:'Post Job'},
        {id:'myJobs', label:'My Jobs'},
        {id:'postLand', label:'Post Land'},
        {id:'myLand', label:'My Land'},
        {id:'workers', label:'Find Workers'}
    ], 'postJob');

    document.getElementById('content').innerHTML = `
        <div id="postJob" class="tab-content active">
            <div class="form-section">
                <h3>Post a Job Listing</h3>
                <div class="form-row">
                    <input id="jWorkType" placeholder="Work type (e.g., Harvesting)">
                    <input id="jDuration" type="number" placeholder="Duration (days)">
                    <input id="jWorkers" type="number" placeholder="Workers needed" value="1">
                </div>
                <div class="form-row">
                    <input id="jVillage" placeholder="Village" value="${currentUser.village||''}">
                    <input id="jDistrict" placeholder="District" value="${currentUser.district||''}">
                    <input id="jState" placeholder="State" value="${currentUser.state||''}">
                </div>
                <div class="form-row">
                    <input id="jExp" placeholder="Experience required (optional)">
                    <button class="btn btn-primary" onclick="postJob()">Post Job</button>
                </div>
            </div>
        </div>
        <div id="myJobs" class="tab-content"><div id="myJobsList"></div></div>
        <div id="postLand" class="tab-content">
            <div class="form-section">
                <h3>Post a Land Listing</h3>
                <div class="form-row">
                    <input id="lArea" type="number" step="0.01" placeholder="Area (acres)">
                    <input id="lCrop" placeholder="Crop type (optional)">
                    <input id="lDuration" placeholder="Duration (e.g., 6 months)">
                </div>
                <div class="form-row">
                    <input id="lVillage" placeholder="Village" value="${currentUser.village||''}">
                    <input id="lDistrict" placeholder="District" value="${currentUser.district||''}">
                    <input id="lState" placeholder="State" value="${currentUser.state||''}">
                </div>
                <div class="form-row">
                    <button class="btn btn-primary" onclick="postLand()">Post Land</button>
                </div>
            </div>
        </div>
        <div id="myLand" class="tab-content"><div id="myLandList"></div></div>
        <div id="workers" class="tab-content">
            <div class="filter-bar">
                <input id="wFilterDistrict" placeholder="Filter by district">
                <button class="btn btn-secondary" onclick="loadWorkers()">Search</button>
            </div>
            <div id="workersList" class="card-list"></div>
        </div>
    `;
    loadMyJobs();
}

async function postJob() {
    const res = await fetch('/api/farm/jobs', {
        method: 'POST', headers: {'Content-Type':'application/json'},
        body: JSON.stringify({
            workType: document.getElementById('jWorkType').value,
            durationDays: document.getElementById('jDuration').value,
            workersRequired: document.getElementById('jWorkers').value,
            village: document.getElementById('jVillage').value,
            district: document.getElementById('jDistrict').value,
            state: document.getElementById('jState').value,
            experienceRequired: document.getElementById('jExp').value
        })
    });
    const data = await res.json();
    showGlobalMsg(res.ok ? 'Job posted!' : data.error, res.ok ? 'success' : 'error');
}

async function loadMyJobs() {
    const res = await fetch('/api/farm/my-jobs');
    const jobs = await res.json();
    document.getElementById('myJobsList').innerHTML = jobs.length === 0
        ? '<p>No jobs posted yet.</p>'
        : '<div class="card-list">' + jobs.map(j => `
            <div class="card">
                <h4>${esc(j.workType)}</h4>
                <p>Duration: ${j.durationDays} days | Workers: ${j.workersRequired}</p>
                <p>Location: ${esc(j.village||'')}, ${esc(j.district||'')}</p>
                ${j.experienceRequired ? `<p>Exp: ${esc(j.experienceRequired)}</p>` : ''}
                ${j.applications && j.applications.length > 0 ? `
                    <div class="sub-list"><h5>Applications (${j.applications.length})</h5>
                    ${j.applications.map(a => `
                        <div class="sub-item">
                            <span>${esc(a.workerName)} ${a.experience?'('+esc(a.experience)+')':''}</span>
                            <span>
                                ${a.status === 'PENDING' ? `
                                    <button class="btn btn-success btn-sm" onclick="selectWorker(${a.applicationId})">Select</button>
                                    <button class="btn btn-danger btn-sm" onclick="rejectWorker(${a.applicationId})">Reject</button>
                                ` : `<span class="badge badge-${a.status.toLowerCase()}">${a.status}</span>`}
                                ${a.phone ? ` <strong>${a.phone}</strong>` : ''}
                            </span>
                        </div>
                    `).join('')}</div>
                ` : ''}
                ${j.directHires && j.directHires.length > 0 ? `
                    <div class="sub-list"><h5>Direct Hires (${j.directHires.length})</h5>
                    ${j.directHires.map(a => `
                        <div class="sub-item">
                            <span>${esc(a.workerName)} ${a.experience?'('+esc(a.experience)+')':''}</span>
                            <span>
                                <span class="badge badge-${a.status.toLowerCase()}">${a.status}</span>
                                ${a.phone ? ` <strong>${a.phone}</strong>` : ''}
                            </span>
                        </div>
                    `).join('')}</div>
                ` : ''}
                ${(!j.applications || j.applications.length === 0) && (!j.directHires || j.directHires.length === 0)
                    ? '<p style="color:#888;margin-top:8px;">No applicants yet</p>' : ''}
            </div>
        `).join('') + '</div>';
}

async function selectWorker(appId) {
    const res = await fetch(`/api/farm/applications/${appId}/select`, {method:'PUT'});
    const data = await res.json();
    showGlobalMsg(res.ok ? 'Worker selected!' : data.error, res.ok ? 'success' : 'error');
    loadMyJobs();
}

async function rejectWorker(appId) {
    const res = await fetch(`/api/farm/applications/${appId}/reject`, {method:'PUT'});
    showGlobalMsg(res.ok ? 'Worker rejected' : 'Error', res.ok ? 'success' : 'error');
    loadMyJobs();
}

async function postLand() {
    const res = await fetch('/api/farm/land', {
        method: 'POST', headers: {'Content-Type':'application/json'},
        body: JSON.stringify({
            area: document.getElementById('lArea').value,
            cropType: document.getElementById('lCrop').value,
            duration: document.getElementById('lDuration').value,
            village: document.getElementById('lVillage').value,
            district: document.getElementById('lDistrict').value,
            state: document.getElementById('lState').value
        })
    });
    const data = await res.json();
    showGlobalMsg(res.ok ? 'Land posted!' : data.error, res.ok ? 'success' : 'error');
}

async function loadMyLand() {
    const res = await fetch('/api/farm/my-land');
    const lands = await res.json();
    document.getElementById('myLandList').innerHTML = lands.length === 0
        ? '<p>No land listings yet.</p>'
        : '<div class="card-list">' + lands.map(l => `
            <div class="card">
                <h4>${esc(l.village||'')}, ${esc(l.district||'')} - ${l.area} acres</h4>
                <p>Crop: ${esc(l.cropType||'Any')} | Duration: ${esc(l.duration||'N/A')}</p>
                ${l.requests && l.requests.length > 0 ? `
                    <div class="sub-list"><h5>Requests (${l.requests.length})</h5>
                    ${l.requests.map(r => `
                        <div class="sub-item">
                            <span>${esc(r.kavuluName)} ${r.experience?'('+esc(r.experience)+')':''}</span>
                            <span>
                                ${r.status === 'PENDING' ? `
                                    <button class="btn btn-success btn-sm" onclick="approveLandReq(${r.requestId})">Approve</button>
                                    <button class="btn btn-danger btn-sm" onclick="rejectLandReq(${r.requestId})">Reject</button>
                                ` : `<span class="badge badge-${r.status.toLowerCase()}">${r.status}</span>`}
                                ${r.phone ? ` <strong>${r.phone}</strong>` : ''}
                            </span>
                        </div>
                    `).join('')}</div>
                ` : '<p style="color:#888;margin-top:8px;">No requests yet</p>'}
            </div>
        `).join('') + '</div>';
}

async function approveLandReq(reqId) {
    const res = await fetch(`/api/farm/land-requests/${reqId}/approve`, {method:'PUT'});
    showGlobalMsg(res.ok ? 'Request approved!' : 'Error', res.ok ? 'success' : 'error');
    loadMyLand();
}

async function rejectLandReq(reqId) {
    const res = await fetch(`/api/farm/land-requests/${reqId}/reject`, {method:'PUT'});
    showGlobalMsg(res.ok ? 'Request rejected' : 'Error', res.ok ? 'success' : 'error');
    loadMyLand();
}

async function loadWorkers() {
    const district = document.getElementById('wFilterDistrict').value;
    const res = await fetch('/api/farm/workers' + (district ? '?district=' + district : ''));
    const workers = await res.json();
    document.getElementById('workersList').innerHTML = workers.length === 0
        ? '<p>No available workers found.</p>'
        : workers.map(w => `
            <div class="card">
                <h4>${esc(w.name)}</h4>
                <p>Work: ${esc(w.workType||'General')} | Exp: ${esc(w.experience||'N/A')}</p>
                <p>Location: ${esc(w.village||'')}, ${esc(w.district||'')}</p>
                <p>Available: ${w.availableDays} days</p>
                <div class="actions">
                    <button class="btn btn-primary btn-sm" onclick="openHireModal(${w.id}, '${esc(w.name)}')">Hire</button>
                </div>
            </div>
        `).join('');
}

function openHireModal(workerId, workerName) {
    const duration = prompt(`How many days to hire ${workerName} for?`, '1');
    if (duration && !isNaN(duration) && duration > 0) {
        hireWorker(workerId, parseInt(duration));
    }
}

async function hireWorker(workerId, durationDays) {
    const res = await fetch(`/api/farm/hire-worker/${workerId}`, {
        method: 'POST',
        headers: {'Content-Type':'application/json'},
        body: JSON.stringify({durationDays: durationDays})
    });
    const data = await res.json();
    showGlobalMsg(res.ok ? 'Worker hired successfully!' : data.error, res.ok ? 'success' : 'error');
    if (res.ok) loadWorkers();
}

// ========== WORKER DASHBOARD ==========
function renderWorkerDashboard() {
    setTabs([
        {id:'browseJobs', label:'Browse Jobs'},
        {id:'myApps', label:'My Applications'},
        {id:'availability', label:'Availability'}
    ], 'browseJobs');

    document.getElementById('content').innerHTML = `
        <div id="browseJobs" class="tab-content active">
            <div class="filter-bar">
                <input id="jobFilterDistrict" placeholder="Filter by district">
                <button class="btn btn-secondary" onclick="loadJobs()">Search</button>
            </div>
            <div id="jobsList" class="card-list"></div>
        </div>
        <div id="myApps" class="tab-content"><div id="myAppsList" class="card-list"></div></div>
        <div id="availability" class="tab-content">
            <div class="form-section">
                <h3>Update Availability</h3>
                <p>Current: <strong>${currentUser.availableDays || 0}</strong> days</p>
                <div class="form-row" style="margin-top:10px;">
                    <input id="newAvailDays" type="number" placeholder="Available days" value="${currentUser.availableDays||0}">
                    <button class="btn btn-primary" onclick="updateAvail()">Update</button>
                </div>
            </div>
        </div>
    `;
    loadJobs();
    loadMyApplications();
}

async function loadJobs() {
    const district = document.getElementById('jobFilterDistrict').value;
    const res = await fetch('/api/farm/jobs' + (district ? '?district=' + district : ''));
    const jobs = await res.json();
    document.getElementById('jobsList').innerHTML = jobs.length === 0
        ? '<p>No jobs available.</p>'
        : jobs.map(j => `
            <div class="card">
                <h4>${esc(j.workType)}</h4>
                <p>Duration: ${j.durationDays} days | Workers: ${j.workersRequired}</p>
                <p>Location: ${esc(j.village||'')}, ${esc(j.district||'')}</p>
                ${j.experienceRequired ? `<p>Exp required: ${esc(j.experienceRequired)}</p>` : ''}
                <p>Posted by: ${esc(j.farmerName)}</p>
                <div class="actions">
                    <button class="btn btn-primary btn-sm" onclick="applyJob(${j.jobId})">Apply</button>
                </div>
            </div>
        `).join('');
}

async function applyJob(jobId) {
    const res = await fetch(`/api/farm/jobs/${jobId}/apply`, {method:'POST'});
    const data = await res.json();
    showGlobalMsg(res.ok ? 'Applied!' : data.error, res.ok ? 'success' : 'error');
}

async function loadMyApplications() {
    const res = await fetch('/api/farm/my-applications');
    const applications = await res.json();
    document.getElementById('myAppsList').innerHTML = applications.length === 0
        ? '<p>No applications yet.</p>'
        : applications.map(a => `
            <div class="card">
                <h4>${esc(a.workType)}</h4>
                <p>Duration: ${a.durationDays} days | Type: ${a.type === 'APPLIED' ? 'Job Application' : 'Direct Hire'}</p>
                <p>Location: ${esc(a.village||'')}, ${esc(a.district||'')}</p>
                <p>Farmer: ${esc(a.farmerName)}</p>
                ${a.experienceRequired ? `<p>Exp required: ${esc(a.experienceRequired)}</p>` : ''}
                <p>
                    Status: <span class="badge badge-${a.status.toLowerCase()}">${a.status}</span>
                </p>
                ${a.farmerPhone ? `<p>Farmer Phone: <strong>${a.farmerPhone}</strong></p>` : ''}
            </div>
        `).join('');
}

async function updateAvail() {
    const days = document.getElementById('newAvailDays').value;
    const res = await fetch('/api/farm/worker/availability', {
        method: 'PUT', headers: {'Content-Type':'application/json'},
        body: JSON.stringify({availableDays: days})
    });
    showGlobalMsg(res.ok ? 'Availability updated!' : 'Error', res.ok ? 'success' : 'error');
    if (res.ok) currentUser.availableDays = parseInt(days);
}

// ========== KAVULU DASHBOARD ==========
function renderKavuluDashboard() {
    setTabs([
        {id:'browseLand', label:'Browse Land'},
        {id:'myRequests', label:'My Requests'}
    ], 'browseLand');

    document.getElementById('content').innerHTML = `
        <div id="browseLand" class="tab-content active">
            <div class="filter-bar">
                <input id="landFilterDistrict" placeholder="Filter by district">
                <button class="btn btn-secondary" onclick="loadLand()">Search</button>
            </div>
            <div id="landList" class="card-list"></div>
        </div>
        <div id="myRequests" class="tab-content"><p style="color:#888;">View your land requests under Browse Land.</p></div>
    `;
    loadLand();
}

async function loadLand() {
    const district = document.getElementById('landFilterDistrict').value;
    const res = await fetch('/api/farm/land' + (district ? '?district=' + district : ''));
    const lands = await res.json();
    document.getElementById('landList').innerHTML = lands.length === 0
        ? '<p>No land listings available.</p>'
        : lands.map(l => `
            <div class="card">
                <h4>${esc(l.village||'')}, ${esc(l.district||'')} - ${l.area} acres</h4>
                <p>Crop: ${esc(l.cropType||'Any')} | Duration: ${esc(l.duration||'N/A')}</p>
                <p>Posted by: ${esc(l.farmerName)}</p>
                <div class="actions">
                    <button class="btn btn-primary btn-sm" onclick="requestLand(${l.landId})">Request</button>
                </div>
            </div>
        `).join('');
}

async function requestLand(landId) {
    const res = await fetch(`/api/farm/land/${landId}/request`, {method:'POST'});
    const data = await res.json();
    showGlobalMsg(res.ok ? 'Request sent!' : data.error, res.ok ? 'success' : 'error');
}

// ========== ADMIN DASHBOARD ==========
function renderAdminDashboard() {
    setTabs([
        {id:'analytics', label:'Analytics'},
        {id:'users', label:'Users'},
        {id:'adminJobs', label:'Jobs'},
        {id:'adminLand', label:'Land'}
    ], 'analytics');

    document.getElementById('content').innerHTML = `
        <div id="analytics" class="tab-content active"><div id="statsGrid" class="stats-grid"></div></div>
        <div id="users" class="tab-content">
            <div class="filter-bar">
                <select id="roleFilter" onchange="loadAdminUsers()">
                    <option value="">All Roles</option>
                    <option value="FARMER">Farmer</option>
                    <option value="WORKER">Worker</option>
                    <option value="KAVULU">Kavulu</option>
                </select>
            </div>
            <table class="data-table"><thead><tr>
                <th>ID</th><th>Name</th><th>Phone</th><th>Role</th><th>District</th><th>Status</th><th>Actions</th>
            </tr></thead><tbody id="usersBody"></tbody></table>
        </div>
        <div id="adminJobs" class="tab-content"><div id="adminJobsList" class="card-list"></div></div>
        <div id="adminLand" class="tab-content"><div id="adminLandList" class="card-list"></div></div>
    `;
    loadAnalytics();
}

async function loadAnalytics() {
    const res = await fetch('/api/admin/analytics');
    const s = await res.json();
    document.getElementById('statsGrid').innerHTML = [
        {n: s.totalUsers, l: 'Total Users'},
        {n: s.totalFarmers, l: 'Farmers'},
        {n: s.totalWorkers, l: 'Workers'},
        {n: s.totalKavulu, l: 'Kavulu'},
        {n: s.activeJobs, l: 'Active Jobs'},
        {n: s.activeLand, l: 'Land Listings'}
    ].map(x => `<div class="stat-card"><div class="number">${x.n}</div><div class="label">${x.l}</div></div>`).join('');
}

async function loadAdminUsers() {
    const role = document.getElementById('roleFilter').value;
    const res = await fetch('/api/admin/users' + (role ? '?role=' + role : ''));
    const users = await res.json();
    document.getElementById('usersBody').innerHTML = users.map(u => `
        <tr>
            <td>${u.id}</td><td>${esc(u.name)}</td><td>${u.phone}</td><td>${u.role}</td>
            <td>${esc(u.district||'-')}</td>
            <td>${u.suspended ? '<span class="badge badge-suspended">Suspended</span>' : 'Active'}</td>
            <td>
                ${u.role !== 'ADMIN' ? `
                    <button class="btn btn-warning btn-sm" onclick="suspendUser(${u.id})">${u.suspended?'Unsuspend':'Suspend'}</button>
                    <button class="btn btn-danger btn-sm" onclick="deleteUser(${u.id})">Delete</button>
                ` : ''}
            </td>
        </tr>
    `).join('');
}

async function suspendUser(id) {
    await fetch(`/api/admin/users/${id}/suspend`, {method:'PUT'});
    loadAdminUsers();
}

async function deleteUser(id) {
    if (!confirm('Delete this user?')) return;
    await fetch(`/api/admin/users/${id}`, {method:'DELETE'});
    loadAdminUsers(); loadAnalytics();
}

async function loadAdminJobs() {
    const res = await fetch('/api/admin/jobs');
    const jobs = await res.json();
    document.getElementById('adminJobsList').innerHTML = jobs.length === 0 ? '<p>No jobs.</p>'
        : jobs.map(j => `
            <div class="card">
                <h4>${esc(j.workType)}</h4>
                <p>${j.durationDays} days | ${esc(j.village||'')}, ${esc(j.district||'')}</p>
                <p>By: ${esc(j.farmerName)}</p>
                <div class="actions"><button class="btn btn-danger btn-sm" onclick="removeJob(${j.jobId})">Remove</button></div>
            </div>
        `).join('');
}

async function removeJob(id) {
    await fetch(`/api/admin/jobs/${id}`, {method:'DELETE'});
    loadAdminJobs(); loadAnalytics();
}

async function loadAdminLand() {
    const res = await fetch('/api/admin/land');
    const lands = await res.json();
    document.getElementById('adminLandList').innerHTML = lands.length === 0 ? '<p>No land listings.</p>'
        : lands.map(l => `
            <div class="card">
                <h4>${esc(l.village||'')}, ${esc(l.district||'')} - ${l.area} acres</h4>
                <p>Crop: ${esc(l.cropType||'Any')} | By: ${esc(l.farmerName)}</p>
                <div class="actions"><button class="btn btn-danger btn-sm" onclick="removeLand(${l.landId})">Remove</button></div>
            </div>
        `).join('');
}

async function removeLand(id) {
    await fetch(`/api/admin/land/${id}`, {method:'DELETE'});
    loadAdminLand(); loadAnalytics();
}

// ========== TAB DATA LOADING ==========
const origSwitchTab = switchTab;
switchTab = function(tabId) {
    origSwitchTab(tabId);
    if (tabId === 'myJobs') loadMyJobs();
    if (tabId === 'myLand') loadMyLand();
    if (tabId === 'workers') loadWorkers();
    if (tabId === 'browseJobs') loadJobs();
    if (tabId === 'myApps') loadMyApplications();
    if (tabId === 'availability') {}
    if (tabId === 'browseLand') loadLand();
    if (tabId === 'analytics') loadAnalytics();
    if (tabId === 'users') loadAdminUsers();
    if (tabId === 'adminJobs') loadAdminJobs();
    if (tabId === 'adminLand') loadAdminLand();
};

// ========== UTILITIES ==========
function showMsg(id, msg, type) {
    const el = document.getElementById(id);
    el.textContent = msg;
    el.className = 'message message-' + type;
    el.classList.remove('hidden');
    setTimeout(() => el.classList.add('hidden'), 4000);
}

function showGlobalMsg(msg, type) {
    showMsg('globalMsg', msg, type);
}

function esc(str) {
    if (!str) return '';
    const d = document.createElement('div');
    d.textContent = str;
    return d.innerHTML;
}

// ========== PROFILE EDIT ==========
function openProfileEditModal() {
    loadProfileData();
    document.getElementById('profileModal').classList.remove('hidden');
}

function closeProfileModal() {
    document.getElementById('profileModal').classList.add('hidden');
}

async function loadProfileData() {
    const res = await fetch('/api/farm/profile/current');
    if (res.ok) {
        const user = await res.json();
        document.getElementById('profileName').value = user.name || '';
        document.getElementById('profileVillage').value = user.village || '';
        document.getElementById('profileDistrict').value = user.district || '';
        document.getElementById('profileState').value = user.state || '';

        // Show/hide role-specific fields
        const workerFieldsDiv = document.getElementById('profileWorkerFields');
        const farmerFieldsDiv = document.getElementById('profileFarmerFields');

        if (user.role === 'WORKER') {
            workerFieldsDiv.classList.remove('hidden');
            farmerFieldsDiv.classList.add('hidden');
            document.getElementById('profileWorkType').value = user.workType || '';
            document.getElementById('profileExperience').value = user.experience || '';
            document.getElementById('profileAvailDays').value = user.availableDays || '';
            document.getElementById('profileAvailFrom').value = user.availableFrom || '';
        } else if (user.role === 'FARMER' || user.role === 'KAVULU') {
            workerFieldsDiv.classList.add('hidden');
            farmerFieldsDiv.classList.remove('hidden');
            document.getElementById('profileCrop').value = user.preferredCrop || '';
        } else {
            workerFieldsDiv.classList.add('hidden');
            farmerFieldsDiv.classList.add('hidden');
        }
    } else {
        showMsg('profileMsg', 'Error loading profile', 'error');
    }
}

async function updateUserProfile(e) {
    e.preventDefault();

    const body = {
        name: document.getElementById('profileName').value,
        village: document.getElementById('profileVillage').value,
        district: document.getElementById('profileDistrict').value,
        state: document.getElementById('profileState').value
    };

    // Add role-specific fields
    if (currentUser.role === 'WORKER') {
        body.workType = document.getElementById('profileWorkType').value;
        body.experience = document.getElementById('profileExperience').value;
        body.availableDays = document.getElementById('profileAvailDays').value;
        body.availableFrom = document.getElementById('profileAvailFrom').value;
    } else if (currentUser.role === 'FARMER' || currentUser.role === 'KAVULU') {
        body.preferredCrop = document.getElementById('profileCrop').value;
    }

    const res = await fetch('/api/farm/profile', {
        method: 'PUT', headers: {'Content-Type':'application/json'},
        body: JSON.stringify(body)
    });

    if (res.ok) {
        const updated = await res.json();
        currentUser = updated;
        showMsg('profileMsg', 'Profile updated successfully!', 'success');
        setTimeout(() => closeProfileModal(), 1500);
    } else {
        const data = await res.json();
        showMsg('profileMsg', data.error || 'Error updating profile', 'error');
    }
}

// Add event listener for profile form
document.addEventListener('DOMContentLoaded', () => {
    const profileForm = document.getElementById('profileForm');
    if (profileForm) {
        profileForm.addEventListener('submit', updateUserProfile);
    }
});
