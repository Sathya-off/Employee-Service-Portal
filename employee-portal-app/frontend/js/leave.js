/**
 * Enterprise Leave Management Controller (leave.js)
 * Implements form validation, submission, overlapping and date-range rules,
 * history table rendering, and manager approval/rejection actions.
 */

document.addEventListener('DOMContentLoaded', async () => {
  const token = sessionStorage.getItem('authToken');

  // Guard: Redirect if not logged in
  if (!token) {
    window.location.href = 'index.html?error=unauthorized';
    return;
  }

  let currentUser = null;

  const navUserName = document.getElementById('nav-user-name');
  const navUserRole = document.getElementById('nav-user-role');
  const userAvatar = document.getElementById('user-avatar-initials');
  const headerBalanceVal = document.getElementById('header-balance-val');
  const logoutBtn = document.getElementById('btn-logout');

  const leaveForm = document.getElementById('leave-form');
  const leaveTypeSelect = document.getElementById('leave-type');
  const startDateInput = document.getElementById('start-date');
  const endDateInput = document.getElementById('end-date');
  const reasonInput = document.getElementById('leave-reason');
  const submitBtn = document.getElementById('btn-submit-leave');
  const resetBtn = document.getElementById('btn-reset-leave');
  const refreshBtn = document.getElementById('btn-refresh-history');
  const alertContainer = document.getElementById('leave-alert');

  const thActions = document.getElementById('th-actions');
  const historyBody = document.getElementById('leave-history-body');

  function showAlert(message, type = 'danger') {
    alertContainer.className = `alert alert-${type}`;
    alertContainer.textContent = message;
    alertContainer.style.display = 'block';
    alertContainer.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
  }

  function hideAlert() {
    alertContainer.style.display = 'none';
    alertContainer.textContent = '';
  }

  function clearErrors() {
    [leaveTypeSelect, startDateInput, endDateInput, reasonInput].forEach(el => {
      el.classList.remove('is-invalid');
    });
    document.getElementById('leave-type-error').style.display = 'none';
    document.getElementById('start-date-error').style.display = 'none';
    document.getElementById('end-date-error').style.display = 'none';
    document.getElementById('leave-reason-error').style.display = 'none';
  }

  // 1. Load Current User Profile
  async function loadUserProfile() {
    try {
      const res = await fetch('/api/employees/me', {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (res.status === 401) {
        sessionStorage.clear();
        window.location.href = 'index.html?error=unauthorized';
        return;
      }
      const data = await res.json();
      currentUser = data.employee;

      const initials = currentUser.name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
      userAvatar.textContent = initials;
      navUserName.textContent = currentUser.name;
      navUserRole.textContent = currentUser.role;
      headerBalanceVal.textContent = currentUser.leaveBalance;

      // Show action column if user is a manager
      if (currentUser.role === 'MANAGER') {
        thActions.style.display = 'table-cell';
      }
    } catch (err) {
      console.error('Error loading profile:', err);
    }
  }

  // 2. Load Leave History Table
  async function loadLeaveHistory() {
    try {
      const res = await fetch('/api/leaves', {
        headers: { 'Authorization': `Bearer ${token}` }
      });

      if (!res.ok) throw new Error('Failed to fetch leave history');

      const data = await res.json();
      const leaves = data.leaves || [];

      if (leaves.length === 0) {
        const cols = currentUser && currentUser.role === 'MANAGER' ? 9 : 8;
        historyBody.innerHTML = `<tr><td colspan="${cols}" style="text-align: center; color: var(--text-muted);">No leave records found.</td></tr>`;
        return;
      }

      historyBody.innerHTML = leaves.map(l => {
        const badgeClass = l.status === 'APPROVED' ? 'badge-approved' : (l.status === 'REJECTED' ? 'badge-rejected' : 'badge-pending');
        const appliedDate = new Date(l.appliedAt).toLocaleDateString();

        let actionCell = '';
        if (currentUser && currentUser.role === 'MANAGER') {
          if (l.status === 'PENDING') {
            actionCell = `
              <td>
                <div style="display: flex; gap: 0.5rem;">
                  <button class="btn btn-sm btn-primary" onclick="window.updateLeaveStatus(${l.id}, 'APPROVED')" data-testid="btn-approve-${l.id}">
                    Approve
                  </button>
                  <button class="btn btn-sm btn-danger" onclick="window.updateLeaveStatus(${l.id}, 'REJECTED')" data-testid="btn-reject-${l.id}">
                    Reject
                  </button>
                </div>
              </td>
            `;
          } else {
            actionCell = `<td><span style="font-size: 0.8rem; color: var(--text-muted);">${l.status}</span></td>`;
          }
        }

        return `
          <tr data-testid="leave-record-${l.id}">
            <td><strong>#${l.id}</strong></td>
            <td>${l.leaveType}</td>
            <td>${l.startDate}</td>
            <td>${l.endDate}</td>
            <td>${l.days} day(s)</td>
            <td style="max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;" title="${l.reason}">${l.reason}</td>
            <td><span class="badge ${badgeClass}" data-testid="status-badge-${l.id}">${l.status}</span></td>
            <td>${appliedDate}</td>
            ${actionCell}
          </tr>
        `;
      }).join('');

    } catch (err) {
      console.error('Error loading leaves:', err);
      historyBody.innerHTML = `<tr><td colspan="9" style="text-align: center; color: var(--danger);">Failed to load leave history.</td></tr>`;
    }
  }

  // 3. Manager Action: Update Leave Status (Exposed on window)
  window.updateLeaveStatus = async function(leaveId, status) {
    if (!confirm(`Are you sure you want to mark leave request #${leaveId} as ${status}?`)) {
      return;
    }
    try {
      const res = await fetch(`/api/leaves/${leaveId}/status`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ status })
      });
      const data = await res.json();
      if (res.ok && data.success) {
        showAlert(`Leave request #${leaveId} has been successfully ${status.toLowerCase()}.`, 'success');
        await loadUserProfile();
        await loadLeaveHistory();
      } else {
        showAlert(data.message || 'Failed to update leave status', 'danger');
      }
    } catch (err) {
      console.error('Update status error:', err);
      showAlert('Error contacting server to update status', 'danger');
    }
  };

  // 4. Form Submission & Client Validations
  leaveForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    hideAlert();
    clearErrors();

    const leaveType = leaveTypeSelect.value.trim();
    const startDate = startDateInput.value.trim();
    const endDate = endDateInput.value.trim();
    const reason = reasonInput.value.trim();

    let hasError = false;

    if (!leaveType) {
      leaveTypeSelect.classList.add('is-invalid');
      document.getElementById('leave-type-error').style.display = 'block';
      hasError = true;
    }

    if (!startDate) {
      startDateInput.classList.add('is-invalid');
      document.getElementById('start-date-error').style.display = 'block';
      hasError = true;
    }

    if (!endDate) {
      endDateInput.classList.add('is-invalid');
      document.getElementById('end-date-error').style.display = 'block';
      hasError = true;
    }

    if (!reason) {
      reasonInput.classList.add('is-invalid');
      document.getElementById('leave-reason-error').style.display = 'block';
      hasError = true;
    }

    if (hasError) {
      showAlert('Please complete all required fields.', 'danger');
      return;
    }

    // Business Rule: End date cannot be before start date
    const start = new Date(startDate);
    const end = new Date(endDate);
    if (end < start) {
      endDateInput.classList.add('is-invalid');
      document.getElementById('end-date-error').textContent = 'End date cannot be earlier than start date';
      document.getElementById('end-date-error').style.display = 'block';
      showAlert('End date cannot be earlier than start date', 'danger');
      return;
    }

    submitBtn.disabled = true;
    submitBtn.textContent = 'Submitting...';

    try {
      const response = await fetch('/api/leaves', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ leaveType, startDate, endDate, reason })
      });

      const result = await response.json();

      if (response.status === 201 && result.success) {
        showAlert('Leave request submitted successfully! Pending manager approval.', 'success');
        leaveForm.reset();
        await loadUserProfile();
        await loadLeaveHistory();
      } else {
        showAlert(result.message || 'Failed to submit leave request.', 'danger');
      }
    } catch (err) {
      console.error('Submission error:', err);
      showAlert('Network error while submitting leave request. Please check your connection.', 'danger');
    } finally {
      submitBtn.disabled = false;
      submitBtn.textContent = 'Submit Request';
    }
  });

  // 5. Reset & Refresh
  resetBtn.addEventListener('click', () => {
    leaveForm.reset();
    clearErrors();
    hideAlert();
  });

  refreshBtn.addEventListener('click', async () => {
    await loadUserProfile();
    await loadLeaveHistory();
  });

  // 6. Logout
  logoutBtn.addEventListener('click', async () => {
    try {
      await fetch('/api/auth/logout', {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}` }
      });
    } catch (e) {}
    sessionStorage.clear();
    window.location.href = 'index.html?message=logged_out';
  });

  // Initial load
  await loadUserProfile();
  await loadLeaveHistory();
});
