#!/usr/bin/env bash
# ========================================================
# Bash Automated Local Deployment & Verification Script
# ========================================================
set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
APP_DIR="${PROJECT_ROOT}/employee-portal-app/backend"
TEST_DIR="${PROJECT_ROOT}/automation-testing-framework"
PORT=8080
HEALTH_URL="http://localhost:${PORT}/api/health"

echo "=========================================================="
echo " Enterprise Portal Deployment & Verification (Unix/Linux)"
echo "=========================================================="

echo "[1/3] Checking if Application is running..."
if curl -s -f "${HEALTH_URL}" > /dev/null 2>&1; then
    echo " Application is ALREADY healthy on port ${PORT}."
else
    echo " Starting Portal Server in background..."
    cd "${APP_DIR}"
    nohup node server.js > portal.log 2>&1 &
    APP_PID=$!
    echo " Spawned PID: ${APP_PID}"

    echo "[2/3] Waiting for Healthcheck Readiness..."
    RETRIES=15
    while [ $RETRIES -gt 0 ]; do
        sleep 1
        if curl -s -f "${HEALTH_URL}" > /dev/null 2>&1; then
            echo " Application is UP and healthy!"
            break
        fi
        RETRIES=$((RETRIES - 1))
    done
fi

echo "[3/3] Triggering Automated Verification Suite..."
cd "${TEST_DIR}"
mvn test -DsuiteXmlFile=testng-smoke.xml -Dheadless=true

echo "=========================================================="
echo " DEPLOYMENT VERIFICATION COMPLETED SUCCESSFULLY"
echo "=========================================================="
