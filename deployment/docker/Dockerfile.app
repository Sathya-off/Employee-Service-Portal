# ========================================================
# Dockerfile: Enterprise Employee Portal (Application Tier)
# Lightweight, secure, multi-stage Node.js container
# ========================================================

FROM node:20-alpine AS runtime

# Install curl for container healthcheck probes
RUN apk add --no-cache curl

WORKDIR /app

# Create non-root user for enterprise container security compliance
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy application source code
COPY --chown=appuser:appgroup employee-portal-app/ ./employee-portal-app/

# Environment defaults
ENV PORT=8080
ENV NODE_ENV=production

EXPOSE 8080

# Healthcheck probe to verify readiness before routing traffic
HEALTHCHECK --interval=10s --timeout=5s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/api/health || exit 1

USER appuser

WORKDIR /app/employee-portal-app/backend

CMD ["node", "server.js"]
