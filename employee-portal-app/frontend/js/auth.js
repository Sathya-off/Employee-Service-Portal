/**
 * Enterprise Authentication Controller (auth.js)
 * Handles client-side validation, login requests, token persistence,
 * and unauthorized redirect banners.
 */

document.addEventListener('DOMContentLoaded', () => {
  const loginForm = document.getElementById('login-form');
  const usernameInput = document.getElementById('username');
  const passwordInput = document.getElementById('password');
  const alertContainer = document.getElementById('alert-container');
  const loginBtn = document.getElementById('btn-login');

  // Check URL query parameters for redirect messages
  const params = new URLSearchParams(window.location.search);
  if (params.get('error') === 'unauthorized') {
    showAlert('You must log in before accessing protected pages.', 'danger');
  } else if (params.get('message') === 'logged_out') {
    showAlert('You have been logged out successfully.', 'info');
  }

  function showAlert(message, type = 'danger') {
    alertContainer.className = `alert alert-${type}`;
    alertContainer.textContent = message;
    alertContainer.style.display = 'block';
  }

  function hideAlert() {
    alertContainer.style.display = 'none';
    alertContainer.textContent = '';
  }

  function clearFieldErrors() {
    usernameInput.classList.remove('is-invalid');
    passwordInput.classList.remove('is-invalid');
    document.getElementById('username-feedback').style.display = 'none';
    document.getElementById('password-feedback').style.display = 'none';
  }

  loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    hideAlert();
    clearFieldErrors();

    const username = usernameInput.value.trim();
    const password = passwordInput.value.trim();

    // Client-side validation: Empty fields
    let hasError = false;
    if (!username && !password) {
      usernameInput.classList.add('is-invalid');
      passwordInput.classList.add('is-invalid');
      showAlert('Username and password are required');
      return;
    }

    if (!username) {
      usernameInput.classList.add('is-invalid');
      document.getElementById('username-feedback').textContent = 'Please enter your username';
      document.getElementById('username-feedback').style.display = 'block';
      showAlert('Username is required');
      hasError = true;
    }

    if (!password) {
      passwordInput.classList.add('is-invalid');
      document.getElementById('password-feedback').textContent = 'Please enter your password';
      document.getElementById('password-feedback').style.display = 'block';
      if (!hasError) showAlert('Password is required');
      hasError = true;
    }

    if (hasError) return;

    // Send authentication request
    loginBtn.disabled = true;
    loginBtn.textContent = 'Authenticating...';

    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
      });

      const data = await response.json();

      if (response.ok && data.success) {
        // Persist session
        sessionStorage.setItem('authToken', data.token);
        sessionStorage.setItem('currentUser', JSON.stringify(data.user));
        // Redirect to dashboard
        window.location.href = 'dashboard.html';
      } else {
        showAlert(data.message || 'Invalid username or password');
        usernameInput.classList.add('is-invalid');
        passwordInput.classList.add('is-invalid');
      }
    } catch (err) {
      console.error('Login error:', err);
      showAlert('Unable to connect to the authentication server. Please try again later.');
    } finally {
      loginBtn.disabled = false;
      loginBtn.textContent = 'Sign In';
    }
  });
});
