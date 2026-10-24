/**
 * Enterprise Dashboard Controller (dashboard.js)
 * Validates session authentication, loads employee profile,
 * displays leave quotas, renders recent leave history, and manages logout.
 */

document.addEventListener('DOMContentLoaded', async () => {
  const token = sessionStorage.getItem('authToken');

  // Guard: Protect page from unauthorized direct access
  if (!token) {
    window.location.href = 'index.html?error=unauthorized';
    return;
  }

  const navUserName = document.getElementById('nav-user-name');
  const navUserRole = document.getElementById('nav-user-role');
  const userAvatar = document.getElementById('user-avatar-initials');
  const headerUserName = document.getElementById('header-user-name');
  const empIdVal = document.getElementById('employee-id-val');
  const empDeptVal = document.getElementById('employee-dept-val');
  const leaveBalanceVal = document.getElementById('leave-balance-val');

  const profileName = document.getElementById('profile-name');
  const profileDesignation = document.getElementById('profile-designation');
  const profileEmail = document.getElementById('profile-email');
  const profileRole = document.getElementById('profile-role');
  const profileJoinDate = document.getElementById('profile-joindate');

  const recentLeavesBody = document.getElementById('recent-leaves-body');
  const logoutBtn = document.getElementById('btn-logout');

  // 1. Fetch authenticated employee profile
  try {
    const response = await fetch('/api/employees/me', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });

    if (response.status === 401) {
      sessionStorage.clear();
      window.location.href = 'index.html?error=unauthorized';
      return;
    }

    const data = await response.json();
    if (!data.success || !data.employee) {
      throw new Error('Failed to load employee profile');
    }

    const emp = data.employee;

    // Update UI elements
    const initials = emp.name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
    userAvatar.textContent = initials;
    navUserName.textContent = emp.name;
    navUserRole.textContent = emp.role;
    headerUserName.textContent = emp.name;

    empIdVal.textContent = emp.employeeId;
    empDeptVal.textContent = emp.department;
    leaveBalanceVal.textContent = `${emp.leaveBalance} Days`;

    profileName.textContent = emp.name;
    profileDesignation.textContent = emp.designation;
    profileEmail.textContent = emp.email;
    profileRole.textContent = emp.role;
    profileJoinDate.textContent = emp.joinDate;

  } catch (err) {
    console.error('Error fetching employee data:', err);
    empIdVal.textContent = 'Error';
    empDeptVal.textContent = 'Error loading data';
    leaveBalanceVal.textContent = 'N/A';
  }

  // 2. Fetch recent leave requests
  try {
    const leavesRes = await fetch('/api/leaves', {
      headers: { 'Authorization': `Bearer ${token}` }
    });

    if (leavesRes.ok) {
      const leavesData = await leavesRes.json();
      const leaves = leavesData.leaves || [];

      if (leaves.length === 0) {
        recentLeavesBody.innerHTML = `<tr><td colspan="4" style="text-align: center; color: var(--text-muted);">No leave requests found.</td></tr>`;
      } else {
        // Show up to 3 most recent
        const recent = leaves.slice(-3).reverse();
        recentLeavesBody.innerHTML = recent.map(l => {
          const badgeClass = l.status === 'APPROVED' ? 'badge-approved' : (l.status === 'REJECTED' ? 'badge-rejected' : 'badge-pending');
          return `
            <tr data-testid="leave-row-${l.id}">
              <td><strong>${l.leaveType}</strong></td>
              <td>${l.startDate} to ${l.endDate}</td>
              <td>${l.days} day(s)</td>
              <td><span class="badge ${badgeClass}" data-testid="leave-status-${l.id}">${l.status}</span></td>
            </tr>
          `;
        }).join('');
      }
    }
  } catch (err) {
    console.error('Error fetching recent leaves:', err);
    recentLeavesBody.innerHTML = `<tr><td colspan="4" style="text-align: center; color: var(--danger);">Failed to load recent leaves.</td></tr>`;
  }

  // 3. Logout action
  logoutBtn.addEventListener('click', async () => {
    try {
      await fetch('/api/auth/logout', {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}` }
      });
    } catch (e) {
      // Continue client-side logout even if server error
    }
    sessionStorage.clear();
    window.location.href = 'index.html?message=logged_out';
  });
});
