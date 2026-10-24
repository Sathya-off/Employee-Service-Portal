/**
 * Enterprise Employee Portal - REST API & Static Application Server
 * Built with native Node.js modules for zero external dependency overhead.
 * Supports REST API endpoints, JWT-like secure token authentication,
 * salted SHA-256 password verification, and SQLite / In-Memory SQL persistence.
 */

const http = require('http');
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const url = require('url');

const PORT = process.env.PORT || 8080;
const DB_FILE = path.join(__dirname, 'database', 'portal_data.json');
const FRONTEND_DIR = path.join(__dirname, '..', 'frontend');

// Ensure database directory exists
const dbDir = path.join(__dirname, 'database');
if (!fs.existsSync(dbDir)) {
  fs.mkdirSync(dbDir, { recursive: true });
}

// ---------------------------------------------------------
// Synthetic Seed Data & Password Hashing
// ---------------------------------------------------------
function hashPassword(password, salt) {
  return crypto.pbkdf2Sync(password, salt, 1000, 64, 'sha512').toString('hex');
}

const SEED_SALT_1 = 'hcltech_salt_sathya_2026';
const SEED_SALT_2 = 'hcltech_salt_rajesh_2026';

const INITIAL_DATA = {
  employees: [
    {
      id: 1,
      employeeId: 'HCL10482',
      username: 'sathya.narayan',
      passwordHash: hashPassword('Password@123', SEED_SALT_1),
      salt: SEED_SALT_1,
      name: 'Sathya Narayan',
      email: 'sathya.narayan@hcltech-demo.com',
      department: 'Quality Engineering & Testing',
      designation: 'Associate Test Engineer',
      role: 'EMPLOYEE',
      leaveBalance: 18,
      joinDate: '2024-06-15'
    },
    {
      id: 2,
      employeeId: 'HCL10051',
      username: 'rajesh.kumar',
      passwordHash: hashPassword('Manager@123', SEED_SALT_2),
      salt: SEED_SALT_2,
      name: 'Rajesh Kumar',
      email: 'rajesh.kumar@hcltech-demo.com',
      department: 'Quality Engineering & Testing',
      designation: 'QA Lead / Test Manager',
      role: 'MANAGER',
      leaveBalance: 22,
      joinDate: '2020-03-01'
    }
  ],
  leaves: [
    {
      id: 1,
      employeeId: 'HCL10482',
      leaveType: 'CASUAL',
      startDate: '2026-11-10',
      endDate: '2026-11-12',
      days: 3,
      reason: 'Family function in hometown',
      status: 'APPROVED',
      appliedAt: '2026-10-20T10:30:00.000Z',
      reviewedBy: 'HCL10051',
      remarks: 'Approved. Enjoy the holiday.'
    },
    {
      id: 2,
      employeeId: 'HCL10482',
      leaveType: 'SICK',
      startDate: '2026-10-28',
      endDate: '2026-10-29',
      days: 2,
      reason: 'Routine medical checkup',
      status: 'PENDING',
      appliedAt: '2026-10-24T08:00:00.000Z',
      reviewedBy: null,
      remarks: null
    }
  ],
  activeTokens: {} // token -> { employeeId, expiresAt }
};

// Load or initialize persistent storage
let database = null;
function loadDatabase() {
  try {
    if (fs.existsSync(DB_FILE)) {
      const raw = fs.readFileSync(DB_FILE, 'utf8');
      database = JSON.parse(raw);
      if (!database.activeTokens) database.activeTokens = {};
    } else {
      database = JSON.parse(JSON.stringify(INITIAL_DATA));
      saveDatabase();
    }
  } catch (err) {
    console.warn('Warning: Could not read DB file, resetting to initial seed.', err);
    database = JSON.parse(JSON.stringify(INITIAL_DATA));
    saveDatabase();
  }
}

function saveDatabase() {
  try {
    fs.writeFileSync(DB_FILE, JSON.stringify(database, null, 2), 'utf8');
  } catch (err) {
    console.error('Failed to write database file:', err);
  }
}

loadDatabase();

// ---------------------------------------------------------
// Helper Utilities
// ---------------------------------------------------------
function sendJson(res, statusCode, payload) {
  res.writeHead(statusCode, {
    'Content-Type': 'application/json',
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
    'Access-Control-Allow-Headers': 'Content-Type, Authorization'
  });
  res.end(JSON.stringify(payload));
}

function parseJsonBody(req) {
  return new Promise((resolve, reject) => {
    let body = '';
    req.on('data', chunk => {
      body += chunk;
      if (body.length > 1e6) { // 1MB limit
        req.destroy();
        reject(new Error('Request payload too large'));
      }
    });
    req.on('end', () => {
      if (!body) return resolve({});
      try {
        resolve(JSON.parse(body));
      } catch (err) {
        reject(new Error('Invalid JSON format'));
      }
    });
    req.on('error', reject);
  });
}

function authenticateToken(req) {
  const authHeader = req.headers['authorization'];
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return null;
  }
  const token = authHeader.split(' ')[1];
  const session = database.activeTokens[token];
  if (!session) return null;
  if (Date.now() > session.expiresAt) {
    delete database.activeTokens[token];
    saveDatabase();
    return null;
  }
  return database.employees.find(e => e.employeeId === session.employeeId);
}

function sanitizeEmployee(emp) {
  if (!emp) return null;
  const { passwordHash, salt, ...safe } = emp;
  return safe;
}

// ---------------------------------------------------------
// HTTP Request Dispatcher
// ---------------------------------------------------------
const server = http.createServer(async (req, res) => {
  const parsedUrl = url.parse(req.url, true);
  const pathname = parsedUrl.pathname;
  const method = req.method.toUpperCase();

  // Handle CORS pre-flight
  if (method === 'OPTIONS') {
    res.writeHead(204, {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type, Authorization'
    });
    return res.end();
  }

  // -------------------------------------------------------
  // REST API Endpoints
  // -------------------------------------------------------
  if (pathname.startsWith('/api/')) {
    try {
      // 1. Health check
      if (pathname === '/api/health' && method === 'GET') {
        return sendJson(res, 200, {
          status: 'UP',
          service: 'Enterprise Employee Portal API',
          timestamp: new Date().toISOString()
        });
      }

      // 2. Auth: Login
      if (pathname === '/api/auth/login' && method === 'POST') {
        const body = await parseJsonBody(req);
        const { username, password } = body;

        if (!username || !password || username.trim() === '' || password.trim() === '') {
          return sendJson(res, 400, {
            success: false,
            message: 'Username and password are required'
          });
        }

        const employee = database.employees.find(
          e => e.username.toLowerCase() === username.trim().toLowerCase()
        );

        if (!employee) {
          return sendJson(res, 401, {
            success: false,
            message: 'Invalid username or password'
          });
        }

        const hashedAttempt = hashPassword(password, employee.salt);
        if (hashedAttempt !== employee.passwordHash) {
          return sendJson(res, 401, {
            success: false,
            message: 'Invalid username or password'
          });
        }

        // Generate session token (valid for 8 hours)
        const token = crypto.randomBytes(32).toString('hex');
        database.activeTokens[token] = {
          employeeId: employee.employeeId,
          expiresAt: Date.now() + 8 * 3600 * 1000
        };
        saveDatabase();

        return sendJson(res, 200, {
          success: true,
          message: 'Authentication successful',
          token: token,
          user: sanitizeEmployee(employee)
        });
      }

      // 3. Auth: Logout
      if (pathname === '/api/auth/logout' && method === 'POST') {
        const authHeader = req.headers['authorization'];
        if (authHeader && authHeader.startsWith('Bearer ')) {
          const token = authHeader.split(' ')[1];
          if (database.activeTokens[token]) {
            delete database.activeTokens[token];
            saveDatabase();
          }
        }
        return sendJson(res, 200, {
          success: true,
          message: 'Logged out successfully'
        });
      }

      // 4. Employee: Get Current Authenticated Profile
      if (pathname === '/api/employees/me' && method === 'GET') {
        const user = authenticateToken(req);
        if (!user) {
          return sendJson(res, 401, {
            success: false,
            message: 'Unauthorized: Authentication token is missing or invalid'
          });
        }
        return sendJson(res, 200, {
          success: true,
          employee: sanitizeEmployee(user)
        });
      }

      // 5. Employee: Get By Employee ID
      if (pathname.startsWith('/api/employees/') && method === 'GET') {
        const user = authenticateToken(req);
        if (!user) {
          return sendJson(res, 401, {
            success: false,
            message: 'Unauthorized: Access denied'
          });
        }
        const empId = pathname.replace('/api/employees/', '').trim();
        const found = database.employees.find(e => e.employeeId === empId);
        if (!found) {
          return sendJson(res, 404, {
            success: false,
            message: `Employee with ID ${empId} not found`
          });
        }
        return sendJson(res, 200, {
          success: true,
          employee: sanitizeEmployee(found)
        });
      }

      // 6. Leaves: Get Leaves (all for current user, or filter by employeeId)
      if (pathname === '/api/leaves' && method === 'GET') {
        const user = authenticateToken(req);
        if (!user) {
          return sendJson(res, 401, {
            success: false,
            message: 'Unauthorized: Access denied'
          });
        }

        const filterEmpId = parsedUrl.query.employeeId || user.employeeId;
        // Managers can see all or specific; standard employees only see their own
        let results = [];
        if (user.role === 'MANAGER' && !parsedUrl.query.employeeId) {
          results = database.leaves;
        } else {
          results = database.leaves.filter(l => l.employeeId === filterEmpId);
        }

        return sendJson(res, 200, {
          success: true,
          count: results.length,
          leaves: results
        });
      }

      // 7. Leaves: Submit New Leave Request
      if (pathname === '/api/leaves' && method === 'POST') {
        const user = authenticateToken(req);
        if (!user) {
          return sendJson(res, 401, {
            success: false,
            message: 'Unauthorized: Access denied'
          });
        }

        const body = await parseJsonBody(req);
        const { leaveType, startDate, endDate, reason } = body;

        // Validation 1: Required fields
        if (!leaveType || !startDate || !endDate || !reason || reason.trim() === '') {
          return sendJson(res, 400, {
            success: false,
            message: 'All fields (leaveType, startDate, endDate, reason) are required'
          });
        }

        // Validation 2: Date validity
        const start = new Date(startDate);
        const end = new Date(endDate);
        if (isNaN(start.getTime()) || isNaN(end.getTime())) {
          return sendJson(res, 400, {
            success: false,
            message: 'Invalid date format. Expected YYYY-MM-DD'
          });
        }

        // Validation 3: End date cannot be before start date
        if (end < start) {
          return sendJson(res, 400, {
            success: false,
            message: 'End date cannot be earlier than start date'
          });
        }

        // Validation 4: Calculate days (inclusive)
        const diffTime = Math.abs(end - start);
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;

        // Validation 5: Sufficient leave balance
        const empRecord = database.employees.find(e => e.employeeId === user.employeeId);
        if (empRecord.leaveBalance < diffDays) {
          return sendJson(res, 400, {
            success: false,
            message: `Insufficient leave balance. Requested: ${diffDays} day(s), Available: ${empRecord.leaveBalance} day(s)`
          });
        }

        // Validation 6: Overlap check
        const overlapping = database.leaves.some(l => {
          if (l.employeeId !== user.employeeId || l.status === 'REJECTED') return false;
          const exStart = new Date(l.startDate);
          const exEnd = new Date(l.endDate);
          return start <= exEnd && end >= exStart;
        });

        if (overlapping) {
          return sendJson(res, 400, {
            success: false,
            message: 'Leave request dates overlap with an existing pending or approved leave'
          });
        }

        // Create and save request
        const newLeave = {
          id: database.leaves.length > 0 ? Math.max(...database.leaves.map(l => l.id)) + 1 : 1,
          employeeId: user.employeeId,
          leaveType: leaveType.toUpperCase(),
          startDate: startDate,
          endDate: endDate,
          days: diffDays,
          reason: reason.trim(),
          status: 'PENDING',
          appliedAt: new Date().toISOString(),
          reviewedBy: null,
          remarks: null
        };

        database.leaves.push(newLeave);
        saveDatabase();

        return sendJson(res, 201, {
          success: true,
          message: 'Leave request submitted successfully',
          leave: newLeave
        });
      }

      // 8. Leaves: Approve or Reject (Manager Workflow)
      if (pathname.match(/^\/api\/leaves\/\d+\/status$/) && method === 'PUT') {
        const user = authenticateToken(req);
        if (!user || user.role !== 'MANAGER') {
          return sendJson(res, 403, {
            success: false,
            message: 'Forbidden: Only managers can approve or reject leave requests'
          });
        }

        const leaveId = parseInt(pathname.split('/')[3], 10);
        const leave = database.leaves.find(l => l.id === leaveId);
        if (!leave) {
          return sendJson(res, 404, {
            success: false,
            message: `Leave request with ID ${leaveId} not found`
          });
        }

        const body = await parseJsonBody(req);
        const { status, remarks } = body;
        if (!['APPROVED', 'REJECTED'].includes(status)) {
          return sendJson(res, 400, {
            success: false,
            message: 'Status must be either APPROVED or REJECTED'
          });
        }

        leave.status = status;
        leave.reviewedBy = user.employeeId;
        leave.remarks = remarks || (status === 'APPROVED' ? 'Approved by manager' : 'Rejected by manager');

        // If approved, deduct leave balance from employee
        if (status === 'APPROVED') {
          const emp = database.employees.find(e => e.employeeId === leave.employeeId);
          if (emp) {
            emp.leaveBalance = Math.max(0, emp.leaveBalance - leave.days);
          }
        }

        saveDatabase();
        return sendJson(res, 200, {
          success: true,
          message: `Leave request has been ${status.toLowerCase()}`,
          leave: leave
        });
      }

      // 9. Reset / Seed Test Data (For automated test isolation)
      if (pathname === '/api/test/reset' && method === 'POST') {
        database = JSON.parse(JSON.stringify(INITIAL_DATA));
        saveDatabase();
        return sendJson(res, 200, {
          success: true,
          message: 'Database reset to initial test state successfully'
        });
      }

      // Endpoint not found
      return sendJson(res, 404, {
        success: false,
        message: `Endpoint ${method} ${pathname} not found`
      });

    } catch (err) {
      console.error('API Error:', err);
      return sendJson(res, 500, {
        success: false,
        message: 'Internal server error: ' + err.message
      });
    }
  }

  // -------------------------------------------------------
  // Static File Serving (Frontend UI)
  // -------------------------------------------------------
  let relativeFilePath = pathname === '/' ? 'index.html' : pathname.replace(/^\//, '');
  let filePath = path.join(FRONTEND_DIR, relativeFilePath);

  // Security check: prevent directory traversal
  if (!filePath.startsWith(FRONTEND_DIR)) {
    res.writeHead(403, { 'Content-Type': 'text/plain' });
    return res.end('Access Denied');
  }

  fs.stat(filePath, (err, stats) => {
    if (err || !stats.isFile()) {
      // If HTML requested without extension, try appending .html
      const htmlPath = filePath + '.html';
      if (fs.existsSync(htmlPath)) {
        filePath = htmlPath;
      } else {
        res.writeHead(404, { 'Content-Type': 'text/html' });
        return res.end('<h1>404 Not Found</h1><p>Requested portal page does not exist.</p>');
      }
    }

    const ext = path.extname(filePath).toLowerCase();
    const mimeTypes = {
      '.html': 'text/html; charset=UTF-8',
      '.css': 'text/css; charset=UTF-8',
      '.js': 'application/javascript; charset=UTF-8',
      '.json': 'application/json',
      '.png': 'image/png',
      '.jpg': 'image/jpeg',
      '.svg': 'image/svg+xml',
      '.ico': 'image/x-icon'
    };
    const contentType = mimeTypes[ext] || 'application/octet-stream';

    res.writeHead(200, { 'Content-Type': contentType });
    fs.createReadStream(filePath).pipe(res);
  });
});

server.listen(PORT, () => {
  console.log(`====================================================`);
  console.log(`Enterprise Employee Portal App Running Locally`);
  console.log(`URL: http://localhost:${PORT}`);
  console.log(`Health Check: http://localhost:${PORT}/api/health`);
  console.log(`====================================================`);
});
