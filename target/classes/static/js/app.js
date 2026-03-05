/**
 * Ocean View Resort - Online Room Reservation System
 * Main Application JavaScript
 */

// ============================================
// Global State
// ============================================
const API_BASE = '/api';
let authToken = localStorage.getItem('authToken');
let currentUser = JSON.parse(localStorage.getItem('currentUser') || 'null');

// ============================================
// Utility Functions
// ============================================

async function apiRequest(url, method = 'GET', body = null) {
    const headers = { 'Content-Type': 'application/json' };
    if (authToken) {
        headers['Authorization'] = `Bearer ${authToken}`;
    }

    const options = { method, headers };
    if (body) {
        options.body = JSON.stringify(body);
    }

    const response = await fetch(`${API_BASE}${url}`, options);
    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.error || 'An error occurred');
    }

    return data;
}

function showToast(message, type = 'info') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast alert-${type}`;
    toast.innerHTML = `<span>${type === 'success' ? '✓' : type === 'error' ? '✕' : 'ℹ'}</span> ${message}`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100px)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

function formatCurrency(amount) {
    return new Intl.NumberFormat('en-LK', {
        style: 'currency',
        currency: 'LKR',
        minimumFractionDigits: 2
    }).format(amount);
}

function formatDate(dateStr) {
    return new Date(dateStr).toLocaleDateString('en-GB', {
        day: '2-digit', month: 'short', year: 'numeric'
    });
}

function getStatusBadge(status) {
    const classes = {
        'CONFIRMED': 'badge-confirmed',
        'PENDING': 'badge-pending',
        'CANCELLED': 'badge-cancelled',
        'CHECKED_IN': 'badge-checked-in',
        'CHECKED_OUT': 'badge-checked-out'
    };
    return `<span class="badge ${classes[status] || ''}">${status.replace('_', ' ')}</span>`;
}

function checkAuth() {
    if (!authToken || !currentUser) {
        window.location.href = '/';
        return false;
    }
    return true;
}

function logout() {
    localStorage.removeItem('authToken');
    localStorage.removeItem('currentUser');
    authToken = null;
    currentUser = null;
    window.location.href = '/';
}

// ============================================
// Login Page
// ============================================

function initLoginPage() {
    const form = document.getElementById('loginForm');
    if (!form) return;

    // If already logged in, redirect
    if (authToken && currentUser) {
        window.location.href = '/pages/dashboard.html';
        return;
    }

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const username = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value;
        const errorDiv = document.getElementById('loginError');
        const btn = form.querySelector('button[type="submit"]');

        errorDiv.style.display = 'none';
        btn.disabled = true;
        btn.innerHTML = '<div class="spinner" style="width:20px;height:20px;margin:0;border-width:2px;"></div> Signing In...';

        try {
            const data = await apiRequest('/auth/login', 'POST', { username, password });
            authToken = data.token;
            currentUser = {
                username: data.username,
                fullName: data.fullName,
                role: data.role
            };
            localStorage.setItem('authToken', authToken);
            localStorage.setItem('currentUser', JSON.stringify(currentUser));
            showToast('Login successful! Welcome back.', 'success');
            setTimeout(() => window.location.href = '/pages/dashboard.html', 500);
        } catch (err) {
            errorDiv.textContent = err.message;
            errorDiv.style.display = 'flex';
            btn.disabled = false;
            btn.innerHTML = '🔐 Sign In';
        }
    });
}

// ============================================
// Dashboard Page
// ============================================

async function initDashboard() {
    if (!checkAuth()) return;
    setupSidebar('dashboard');

    try {
        const [occupancy, revenue, reservations] = await Promise.all([
            apiRequest('/reports/occupancy'),
            apiRequest('/reports/revenue'),
            apiRequest('/reservations')
        ]);

        // Update stat cards
        document.getElementById('totalRooms').textContent = occupancy.totalRooms || 0;
        document.getElementById('occupiedRooms').textContent = occupancy.occupiedRooms || 0;
        document.getElementById('totalReservations').textContent = occupancy.totalReservations || 0;
        document.getElementById('totalRevenue').textContent = formatCurrency(revenue.totalRevenue || 0);

        // Recent reservations table
        const tbody = document.getElementById('recentReservations');
        if (reservations.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" class="empty-state"><p>No reservations yet</p></td></tr>';
        } else {
            tbody.innerHTML = reservations.slice(0, 10).map(r => `
                <tr>
                    <td><strong>${r.reservationId}</strong></td>
                    <td>${r.guestName}</td>
                    <td>Room ${r.roomNumber}</td>
                    <td>${r.roomType}</td>
                    <td>${formatDate(r.checkInDate)}</td>
                    <td>${formatDate(r.checkOutDate)}</td>
                    <td>${getStatusBadge(r.status)}</td>
                </tr>
            `).join('');
        }
    } catch (err) {
        showToast('Failed to load dashboard data: ' + err.message, 'error');
    }
}

// ============================================
// Add Reservation Page
// ============================================

let selectedRoomId = null;

async function initAddReservation() {
    if (!checkAuth()) return;
    setupSidebar('add-reservation');

    const checkIn = document.getElementById('checkInDate');
    const checkOut = document.getElementById('checkOutDate');
    const roomTypeFilter = document.getElementById('roomTypeFilter');

    // Set min dates
    const today = new Date().toISOString().split('T')[0];
    checkIn.min = today;
    checkOut.min = today;

    // Load rooms on date change
    const loadRooms = async () => {
        if (!checkIn.value || !checkOut.value) return;

        if (checkOut.value <= checkIn.value) {
            showToast('Check-out must be after check-in date', 'error');
            return;
        }

        try {
            let url = `/rooms/available?checkIn=${checkIn.value}&checkOut=${checkOut.value}`;
            if (roomTypeFilter.value) url += `&roomType=${roomTypeFilter.value}`;

            const rooms = await apiRequest(url);
            renderRoomCards(rooms);
        } catch (err) {
            showToast('Failed to load rooms: ' + err.message, 'error');
        }
    };

    checkIn.addEventListener('change', () => {
        checkOut.min = checkIn.value;
        loadRooms();
    });
    checkOut.addEventListener('change', loadRooms);
    roomTypeFilter.addEventListener('change', loadRooms);

    // Form submission
    document.getElementById('reservationForm').addEventListener('submit', async (e) => {
        e.preventDefault();

        if (!selectedRoomId) {
            showToast('Please select a room first', 'error');
            return;
        }

        const formData = {
            guestName: document.getElementById('guestName').value.trim(),
            guestAddress: document.getElementById('guestAddress').value.trim(),
            contactNumber: document.getElementById('contactNumber').value.trim(),
            email: document.getElementById('email').value.trim(),
            roomId: selectedRoomId,
            checkInDate: checkIn.value,
            checkOutDate: checkOut.value
        };

        // Client-side validation
        if (!formData.guestName) {
            showToast('Guest name is required', 'error'); return;
        }
        if (!formData.contactNumber || !/^[0-9+\-\s]{7,15}$/.test(formData.contactNumber)) {
            showToast('Please enter a valid contact number (7-15 digits)', 'error'); return;
        }

        try {
            const result = await apiRequest('/reservations', 'POST', formData);
            showToast(`Reservation ${result.reservationId} created successfully!`, 'success');
            setTimeout(() => window.location.href = '/pages/view-reservation.html', 1500);
        } catch (err) {
            showToast(err.message, 'error');
        }
    });
}

function renderRoomCards(rooms) {
    const container = document.getElementById('roomsGrid');
    if (rooms.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <div class="icon">🏨</div>
                <h3>No rooms available</h3>
                <p>Try selecting different dates or room type.</p>
            </div>`;
        return;
    }

    container.innerHTML = rooms.map(room => `
        <div class="room-card ${selectedRoomId === room.roomId ? 'selected' : ''}"
             onclick="selectRoom(${room.roomId}, this)" data-room-id="${room.roomId}">
            <span class="room-type-badge ${room.roomType.toLowerCase()}">${room.roomType}</span>
            <h4>Room ${room.roomNumber}</h4>
            <p style="color: var(--text-secondary); font-size: 13px; margin-bottom: 8px;">
                🏞️ ${room.viewType} View
            </p>
            <div class="room-price">
                ${formatCurrency(room.pricePerNight)} <span>/ night</span>
            </div>
        </div>
    `).join('');
}

function selectRoom(roomId, element) {
    document.querySelectorAll('.room-card').forEach(c => c.classList.remove('selected'));
    element.classList.add('selected');
    selectedRoomId = roomId;
}

// ============================================
// View / Search Reservation Page
// ============================================

async function initViewReservation() {
    if (!checkAuth()) return;
    setupSidebar('view-reservation');

    // Load all reservations
    await loadReservations();

    // Search functionality
    document.getElementById('searchBtn').addEventListener('click', searchReservations);
    document.getElementById('searchInput').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') searchReservations();
    });
    document.getElementById('loadAllBtn').addEventListener('click', loadReservations);
}

async function loadReservations() {
    try {
        const reservations = await apiRequest('/reservations');
        renderReservationsTable(reservations);
    } catch (err) {
        showToast('Failed to load reservations', 'error');
    }
}

async function searchReservations() {
    const keyword = document.getElementById('searchInput').value.trim();
    if (!keyword) {
        loadReservations();
        return;
    }

    try {
        const results = await apiRequest(`/reservations/search?keyword=${encodeURIComponent(keyword)}`);
        renderReservationsTable(results);

        if (results.length === 0) {
            showToast('No reservations found matching your search', 'info');
        }
    } catch (err) {
        showToast('Search failed: ' + err.message, 'error');
    }
}

function renderReservationsTable(reservations) {
    const tbody = document.getElementById('reservationsTable');

    if (reservations.length === 0) {
        tbody.innerHTML = `
            <tr><td colspan="8">
                <div class="empty-state">
                    <div class="icon">📋</div>
                    <h3>No reservations found</h3>
                    <p>Add a new reservation to get started.</p>
                </div>
            </td></tr>`;
        return;
    }

    tbody.innerHTML = reservations.map(r => `
        <tr>
            <td><strong>${r.reservationId}</strong></td>
            <td>${r.guestName}</td>
            <td>${r.contactNumber || '-'}</td>
            <td>Room ${r.roomNumber} (${r.roomType})</td>
            <td>${formatDate(r.checkInDate)}</td>
            <td>${formatDate(r.checkOutDate)}</td>
            <td>${getStatusBadge(r.status)}</td>
            <td>
                <div style="display:flex;gap:8px;flex-wrap:wrap;">
                    <button class="btn btn-sm btn-primary" onclick="viewDetails('${r.reservationId}')">View</button>
                    ${r.status !== 'CANCELLED' && r.status !== 'CHECKED_OUT' ?
            `<button class="btn btn-sm btn-danger" onclick="cancelRes('${r.reservationId}')">Cancel</button>` : ''}
                </div>
            </td>
        </tr>
    `).join('');
}

async function viewDetails(resId) {
    try {
        const res = await apiRequest(`/reservations/${resId}`);
        const modal = document.getElementById('detailsModal');
        document.getElementById('modalContent').innerHTML = `
            <div style="display:grid; gap:12px;">
                <div class="bill-row"><span class="label">Reservation ID</span><span class="value">${res.reservationId}</span></div>
                <div class="bill-row"><span class="label">Guest Name</span><span class="value">${res.guestName}</span></div>
                <div class="bill-row"><span class="label">Address</span><span class="value">${res.guestAddress || 'N/A'}</span></div>
                <div class="bill-row"><span class="label">Contact</span><span class="value">${res.contactNumber}</span></div>
                <div class="bill-row"><span class="label">Email</span><span class="value">${res.email || 'N/A'}</span></div>
                <div class="bill-row"><span class="label">Room</span><span class="value">Room ${res.roomNumber} (${res.roomType})</span></div>
                <div class="bill-row"><span class="label">View</span><span class="value">${res.viewType}</span></div>
                <div class="bill-row"><span class="label">Check-In</span><span class="value">${formatDate(res.checkInDate)}</span></div>
                <div class="bill-row"><span class="label">Check-Out</span><span class="value">${formatDate(res.checkOutDate)}</span></div>
                <div class="bill-row"><span class="label">Nights</span><span class="value">${res.nights}</span></div>
                <div class="bill-row"><span class="label">Rate/Night</span><span class="value">${formatCurrency(res.pricePerNight)}</span></div>
                <div class="bill-row"><span class="label">Status</span><span class="value">${getStatusBadge(res.status)}</span></div>
                <div class="bill-row bill-total"><span class="label" style="font-weight:700">Total Amount</span><span class="value">${formatCurrency(res.totalAmount)}</span></div>
            </div>
        `;
        modal.classList.add('active');
    } catch (err) {
        showToast('Failed to load details: ' + err.message, 'error');
    }
}

function closeModal() {
    document.getElementById('detailsModal').classList.remove('active');
}

async function cancelRes(resId) {
    if (!confirm(`Are you sure you want to cancel reservation ${resId}?`)) return;

    try {
        await apiRequest(`/reservations/${resId}/cancel`, 'PUT');
        showToast(`Reservation ${resId} has been cancelled`, 'success');
        loadReservations();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// ============================================
// Billing Page
// ============================================

async function initBilling() {
    if (!checkAuth()) return;
    setupSidebar('billing');

    document.getElementById('billForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const resId = document.getElementById('billResId').value.trim();

        if (!resId) {
            showToast('Please enter a reservation ID', 'error');
            return;
        }

        try {
            const bill = await apiRequest(`/billing/${resId}`, 'POST');
            renderBill(bill);
        } catch (err) {
            showToast(err.message, 'error');
            document.getElementById('billResult').innerHTML = '';
        }
    });
}

function renderBill(bill) {
    const container = document.getElementById('billResult');
    container.innerHTML = `
        <div class="card bill-container" style="animation: fadeInUp 0.4s ease;">
            <div class="bill-header">
                <div style="font-size:36px; margin-bottom:8px;">🏖️</div>
                <h2>Ocean View Resort</h2>
                <p style="color: var(--text-secondary); font-size: 13px;">Galle, Sri Lanka</p>
                <p style="color: var(--text-muted); font-size: 12px; margin-top:4px;">Tax Invoice</p>
            </div>
            <div style="margin-bottom: 24px;">
                <div class="bill-row"><span class="label">Invoice No.</span><span class="value">${bill.reservationId}</span></div>
                <div class="bill-row"><span class="label">Guest</span><span class="value">${bill.guestName}</span></div>
                <div class="bill-row"><span class="label">Room</span><span class="value">Room ${bill.roomNumber} (${bill.roomType})</span></div>
                <div class="bill-row"><span class="label">Check-In</span><span class="value">${formatDate(bill.checkInDate)}</span></div>
                <div class="bill-row"><span class="label">Check-Out</span><span class="value">${formatDate(bill.checkOutDate)}</span></div>
            </div>
            <div style="border-top: 1px solid var(--border-color); padding-top: 16px;">
                <div class="bill-row"><span class="label">Room Rate (per night)</span><span class="value">${formatCurrency(bill.roomRate)}</span></div>
                <div class="bill-row"><span class="label">Number of Nights</span><span class="value">${bill.nights}</span></div>
                <div class="bill-row"><span class="label">Subtotal</span><span class="value">${formatCurrency(bill.subtotal)}</span></div>
                <div class="bill-row"><span class="label">Tax (${bill.taxRate}%)</span><span class="value">${formatCurrency(bill.taxAmount)}</span></div>
                <div class="bill-row bill-total">
                    <span class="label" style="font-size:16px;font-weight:700;">TOTAL AMOUNT</span>
                    <span class="value">${formatCurrency(bill.totalAmount)}</span>
                </div>
            </div>
            <div style="text-align: center; margin-top: 24px;">
                <button class="btn btn-primary" onclick="window.print()">🖨️ Print Bill</button>
            </div>
        </div>
    `;
}

// ============================================
// Reports Page
// ============================================

async function initReports() {
    if (!checkAuth()) return;
    setupSidebar('reports');

    try {
        const [occupancy, revenue] = await Promise.all([
            apiRequest('/reports/occupancy'),
            apiRequest('/reports/revenue')
        ]);

        // Occupancy Stats
        document.getElementById('rptTotalRooms').textContent = occupancy.totalRooms || 0;
        document.getElementById('rptOccupied').textContent = occupancy.occupiedRooms || 0;
        document.getElementById('rptAvailable').textContent = occupancy.availableRooms || 0;
        document.getElementById('rptOccupancyRate').textContent = (occupancy.occupancyRate || 0) + '%';

        // Reservation Stats
        document.getElementById('rptTotal').textContent = occupancy.totalReservations || 0;
        document.getElementById('rptConfirmed').textContent = occupancy.confirmedReservations || 0;
        document.getElementById('rptPending').textContent = occupancy.pendingReservations || 0;
        document.getElementById('rptCheckedIn').textContent = occupancy.checkedInReservations || 0;
        document.getElementById('rptCheckedOut').textContent = occupancy.checkedOutReservations || 0;
        document.getElementById('rptCancelled').textContent = occupancy.cancelledReservations || 0;

        // Revenue Stats
        document.getElementById('rptRevenue').textContent = formatCurrency(revenue.totalRevenue || 0);
        document.getElementById('rptTax').textContent = formatCurrency(revenue.totalTaxCollected || 0);
        document.getElementById('rptBills').textContent = revenue.totalBillsGenerated || 0;
        document.getElementById('rptAvgBill').textContent = formatCurrency(revenue.averageBillAmount || 0);

    } catch (err) {
        showToast('Failed to load reports: ' + err.message, 'error');
    }
}

// ============================================
// Common Sidebar Setup
// ============================================

function setupSidebar(activePage) {
    // Set user info
    if (currentUser) {
        const userNameEl = document.getElementById('userName');
        const userRoleEl = document.getElementById('userRole');
        const userAvatarEl = document.getElementById('userAvatar');
        if (userNameEl) userNameEl.textContent = currentUser.fullName;
        if (userRoleEl) userRoleEl.textContent = currentUser.role;
        if (userAvatarEl) userAvatarEl.textContent = currentUser.fullName.charAt(0);
    }

    // Set active nav item
    document.querySelectorAll('.nav-item').forEach(item => {
        if (item.dataset.page === activePage) {
            item.classList.add('active');
        }
    });
}

// ============================================
// Page Initialization
// ============================================

document.addEventListener('DOMContentLoaded', () => {
    const page = document.body.dataset.page;

    switch (page) {
        case 'login': initLoginPage(); break;
        case 'dashboard': initDashboard(); break;
        case 'add-reservation': initAddReservation(); break;
        case 'view-reservation': initViewReservation(); break;
        case 'billing': initBilling(); break;
        case 'reports': initReports(); break;
        case 'help': setupSidebar('help'); break;
    }
});
