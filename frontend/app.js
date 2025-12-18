// ===== API Configuration =====
const API = {
    users: 'http://localhost:8081/api/users',
    courts: 'http://localhost:8082/api/courts',
    bookings: 'http://localhost:8083/api/bookings',
    notifications: 'http://localhost:8084/api/notifications'
};

let autoRefreshInterval = null;

// ===== Initialize App =====
document.addEventListener('DOMContentLoaded', () => {
    initTabs();
    checkServiceStatus();
    loadAllData();
    setupForms();
    
    // Check status every 30 seconds
    setInterval(checkServiceStatus, 30000);
});

// ===== Tab Navigation =====
function initTabs() {
    const tabs = document.querySelectorAll('.tab');
    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            // Remove active class from all tabs
            tabs.forEach(t => t.classList.remove('active'));
            document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
            
            // Add active class to clicked tab
            tab.classList.add('active');
            const tabId = tab.dataset.tab;
            document.getElementById(tabId).classList.add('active');
            
            // Load data for the tab
            if (tabId === 'users') loadUsers();
            if (tabId === 'courts') loadCourts();
            if (tabId === 'bookings') {
                loadBookings();
                populateBookingDropdowns();
            }
            if (tabId === 'notifications') loadNotifications();
            if (tabId === 'dashboard') loadStats();
        });
    });
}

// ===== Service Status Check =====
async function checkServiceStatus() {
    const services = [
        { id: 'status-user', url: API.users },
        { id: 'status-court', url: API.courts },
        { id: 'status-booking', url: API.bookings },
        { id: 'status-notification', url: API.notifications },
        { id: 'status-rabbitmq', url: 'http://localhost:15672' }
    ];
    
    for (const service of services) {
        const element = document.getElementById(service.id);
        try {
            await fetch(service.url, { mode: 'no-cors' });
            element.classList.add('online');
            element.classList.remove('offline');
        } catch (error) {
            element.classList.add('offline');
            element.classList.remove('online');
        }
    }
}

// ===== Load All Data =====
function loadAllData() {
    loadUsers();
    loadCourts();
    loadBookings();
    loadNotifications();
    loadStats();
}

// ===== Load Statistics =====
async function loadStats() {
    try {
        const [users, courts, bookings, notifications] = await Promise.all([
            fetch(API.users).then(r => r.json()).catch(() => []),
            fetch(API.courts).then(r => r.json()).catch(() => []),
            fetch(API.bookings).then(r => r.json()).catch(() => []),
            fetch(API.notifications).then(r => r.json()).catch(() => [])
        ]);
        
        document.getElementById('stat-users').textContent = users.length;
        document.getElementById('stat-courts').textContent = courts.length;
        document.getElementById('stat-bookings').textContent = bookings.length;
        document.getElementById('stat-notifications').textContent = notifications.length;
        document.getElementById('notif-badge').textContent = notifications.length;
    } catch (error) {
        console.error('Error loading stats:', error);
    }
}

// ===== Users =====
async function loadUsers() {
    const container = document.getElementById('users-list');
    container.innerHTML = '<p class="loading">Loading users...</p>';
    
    try {
        const response = await fetch(API.users);
        const users = await response.json();
        
        if (users.length === 0) {
            container.innerHTML = `
                <div class="empty-state">
                    <span class="emoji">👤</span>
                    <p>No users yet. Create one above!</p>
                </div>
            `;
            return;
        }
        
        container.innerHTML = users.map(user => `
            <div class="data-item">
                <div class="data-item-info">
                    <span class="data-item-title">${user.name || user.fullName || 'Unknown'}</span>
                    <span class="data-item-subtitle">📧 ${user.email}</span>
                </div>
                <div class="data-item-meta">
                    <span class="data-item-id">ID: ${user.id}</span>
                </div>
            </div>
        `).join('');
    } catch (error) {
        container.innerHTML = '<p class="empty-state">❌ Failed to load users. Is the service running?</p>';
    }
}

// ===== Courts =====
async function loadCourts() {
    const container = document.getElementById('courts-list');
    container.innerHTML = '<p class="loading">Loading courts...</p>';
    
    try {
        const response = await fetch(API.courts);
        const courts = await response.json();
        
        if (courts.length === 0) {
            container.innerHTML = `
                <div class="empty-state">
                    <span class="emoji">🏟️</span>
                    <p>No courts yet. Create one above!</p>
                </div>
            `;
            return;
        }
        
        container.innerHTML = courts.map(court => `
            <div class="data-item">
                <div class="data-item-info">
                    <span class="data-item-title">${court.name || court.courtName || 'Unknown Court'}</span>
                    <span class="data-item-subtitle">📍 ${court.location || court.clubName || 'Unknown Location'} ${court.indoor ? '• 🏠 Indoor' : '• ☀️ Outdoor'}</span>
                </div>
                <div class="data-item-meta">
                    <span class="data-item-id">ID: ${court.id}</span>
                </div>
            </div>
        `).join('');
    } catch (error) {
        container.innerHTML = '<p class="empty-state">❌ Failed to load courts. Is the service running?</p>';
    }
}

// ===== Bookings =====
async function loadBookings() {
    const container = document.getElementById('bookings-list');
    container.innerHTML = '<p class="loading">Loading bookings...</p>';
    
    try {
        const response = await fetch(API.bookings);
        const bookings = await response.json();
        
        if (bookings.length === 0) {
            container.innerHTML = `
                <div class="empty-state">
                    <span class="emoji">📅</span>
                    <p>No bookings yet. Create one above to trigger RabbitMQ!</p>
                </div>
            `;
            return;
        }
        
        container.innerHTML = bookings.map(booking => `
            <div class="data-item">
                <div class="data-item-info">
                    <span class="data-item-title">Booking #${booking.id}</span>
                    <span class="data-item-subtitle">👤 User ID: ${booking.userId} • 🏟️ Court ID: ${booking.courtId}</span>
                    <span class="data-item-subtitle">🕐 ${formatDate(booking.time)}</span>
                </div>
                <div class="data-item-meta">
                    <span class="data-item-id">ID: ${booking.id}</span>
                </div>
            </div>
        `).join('');
    } catch (error) {
        container.innerHTML = '<p class="empty-state">❌ Failed to load bookings. Is the service running?</p>';
    }
}

// ===== Notifications =====
async function loadNotifications() {
    const container = document.getElementById('notifications-list');
    container.innerHTML = '<p class="loading">Loading notifications...</p>';
    
    try {
        const response = await fetch(API.notifications);
        const notifications = await response.json();
        
        document.getElementById('notif-badge').textContent = notifications.length;
        
        if (notifications.length === 0) {
            container.innerHTML = `
                <div class="empty-state">
                    <span class="emoji">🔔</span>
                    <p>No notifications yet. Create a booking to see async RabbitMQ magic!</p>
                </div>
            `;
            return;
        }
        
        container.innerHTML = notifications.map(notif => `
            <div class="data-item notification-item">
                <div class="data-item-info">
                    <span class="data-item-title">🔔 ${notif.notificationType || 'Notification'}</span>
                    <span class="data-item-subtitle">${notif.message}</span>
                    <span class="data-item-subtitle">👤 User ID: ${notif.userId} • 📅 Booking ID: ${notif.bookingId}</span>
                    <span class="data-item-subtitle">⏰ ${formatDate(notif.createdAt)}</span>
                </div>
                <div class="data-item-meta">
                    <span class="data-item-id" style="background: #10b981;">✓ ${notif.status}</span>
                </div>
            </div>
        `).join('');
    } catch (error) {
        container.innerHTML = '<p class="empty-state">❌ Failed to load notifications. Is the service running?</p>';
    }
}

// ===== Populate Booking Dropdowns =====
async function populateBookingDropdowns() {
    try {
        const [users, courts] = await Promise.all([
            fetch(API.users).then(r => r.json()).catch(() => []),
            fetch(API.courts).then(r => r.json()).catch(() => [])
        ]);
        
        const userSelect = document.getElementById('booking-user');
        userSelect.innerHTML = '<option value="">-- Select User --</option>' + 
            users.map(u => `<option value="${u.id}">${u.name || u.fullName || 'User'} (ID: ${u.id})</option>`).join('');
        
        const courtSelect = document.getElementById('booking-court');
        courtSelect.innerHTML = '<option value="">-- Select Court --</option>' + 
            courts.map(c => `<option value="${c.id}">${c.name || c.courtName || 'Court'} (ID: ${c.id})</option>`).join('');
    } catch (error) {
        console.error('Error populating dropdowns:', error);
    }
}

// ===== Form Handlers =====
function setupForms() {
    // User Form
    document.getElementById('user-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const name = document.getElementById('user-name').value;
        const email = document.getElementById('user-email').value;
        
        try {
            const response = await fetch(API.users, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name, email })
            });
            
            if (response.ok) {
                showToast('User created successfully!', 'success');
                document.getElementById('user-form').reset();
                loadUsers();
                loadStats();
            } else {
                showToast('Failed to create user', 'error');
            }
        } catch (error) {
            showToast('Error: Service unavailable', 'error');
        }
    });
    
    // Court Form
    document.getElementById('court-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const name = document.getElementById('court-name').value;
        const location = document.getElementById('court-location').value;
        const indoor = document.getElementById('court-indoor').checked;
        
        try {
            const response = await fetch(API.courts, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name, location, indoor })
            });
            
            if (response.ok) {
                showToast('Court created successfully!', 'success');
                document.getElementById('court-form').reset();
                loadCourts();
                loadStats();
            } else {
                showToast('Failed to create court', 'error');
            }
        } catch (error) {
            showToast('Error: Service unavailable', 'error');
        }
    });
    
    // Booking Form
    document.getElementById('booking-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const userId = document.getElementById('booking-user').value;
        const courtId = document.getElementById('booking-court').value;
        
        if (!userId || !courtId) {
            showToast('Please select both user and court', 'error');
            return;
        }
        
        try {
            const response = await fetch(`${API.bookings}?userId=${userId}&courtId=${courtId}`, {
                method: 'POST'
            });
            
            if (response.ok) {
                showToast('🎾 Booking created! Check Notifications tab for async update!', 'success');
                loadBookings();
                loadStats();
                
                // Auto-refresh notifications after 2 seconds to show async result
                setTimeout(() => {
                    loadNotifications();
                    loadStats();
                    showToast('🔔 Notification created via RabbitMQ!', 'info');
                }, 2000);
            } else {
                const text = await response.text();
                showToast(`Failed: ${text}`, 'error');
            }
        } catch (error) {
            showToast('Error: Service unavailable', 'error');
        }
    });
}

// ===== Full Demo =====
async function runFullDemo() {
    const resultDiv = document.getElementById('demo-result');
    resultDiv.className = 'demo-result show';
    resultDiv.innerHTML = '⏳ Running demo...';
    
    try {
        // Step 1: Create user
        resultDiv.innerHTML = '⏳ Step 1/4: Creating user...';
        const userRes = await fetch(API.users, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name: 'Demo User', email: `demo${Date.now()}@padelpal.com` })
        });
        const user = await userRes.json();
        
        // Step 2: Create court
        resultDiv.innerHTML = '⏳ Step 2/4: Creating court...';
        const courtRes = await fetch(API.courts, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name: 'Demo Court', location: 'Demo Building', indoor: true })
        });
        const court = await courtRes.json();
        
        // Step 3: Create booking
        resultDiv.innerHTML = '⏳ Step 3/4: Creating booking (publishing to RabbitMQ)...';
        const bookingRes = await fetch(`${API.bookings}?userId=${user.id}&courtId=${court.id}`, {
            method: 'POST'
        });
        const booking = await bookingRes.json();
        
        // Step 4: Wait and check notification
        resultDiv.innerHTML = '⏳ Step 4/4: Waiting for async notification from RabbitMQ...';
        await new Promise(resolve => setTimeout(resolve, 2500));
        
        const notifRes = await fetch(API.notifications);
        const notifications = await notifRes.json();
        
        loadStats();
        
        resultDiv.className = 'demo-result show success';
        resultDiv.innerHTML = `
            ✅ <strong>Demo Complete!</strong><br><br>
            👤 Created User #${user.id}<br>
            🏟️ Created Court #${court.id}<br>
            📅 Created Booking #${booking.id} → Published to RabbitMQ<br>
            🔔 Notifications: ${notifications.length} total (created asynchronously via RabbitMQ)<br><br>
            <em>The notification was created by the Notification Service consuming the RabbitMQ message!</em>
        `;
        
        showToast('🎉 Full demo completed!', 'success');
        loadAllData();
        
    } catch (error) {
        resultDiv.className = 'demo-result show error';
        resultDiv.innerHTML = `❌ Demo failed: ${error.message}. Make sure all services are running.`;
    }
}

// ===== Auto Refresh =====
function startAutoRefresh() {
    if (autoRefreshInterval) return;
    autoRefreshInterval = setInterval(() => {
        loadNotifications();
        loadStats();
    }, 2000);
    showToast('Auto-refresh started (every 2 seconds)', 'info');
}

function stopAutoRefresh() {
    if (autoRefreshInterval) {
        clearInterval(autoRefreshInterval);
        autoRefreshInterval = null;
        showToast('Auto-refresh stopped', 'info');
    }
}

// ===== Toast Notifications =====
function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `
        <span>${type === 'success' ? '✅' : type === 'error' ? '❌' : 'ℹ️'}</span>
        <span>${message}</span>
    `;
    container.appendChild(toast);
    
    setTimeout(() => {
        toast.style.animation = 'slideIn 0.3s ease reverse';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

// ===== Utilities =====
function formatDate(dateString) {
    if (!dateString) return 'Unknown';
    const date = new Date(dateString);
    return date.toLocaleString();
}
