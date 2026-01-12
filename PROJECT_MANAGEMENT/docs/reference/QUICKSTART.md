# DANXILS Enterprise System - Quick Start Guide

## 🚀 Starting the System

### One-Command Startup (Recommended)

```bash
./start-danxils.sh
```

This script will:
1. Check prerequisites (Docker, Java, Maven, Node.js)
2. Start infrastructure (Kafka, PostgreSQL)
3. Build and start all backend services
4. Start web dashboard
5. Display service URLs and logs

---

## 🛑 Stopping the System

```bash
./stop-danxils.sh
```

---

## 📊 Checking System Status

```bash
./status-danxils.sh
```

---

## 🌐 Service URLs

### Frontend
- **Customer Portal**: http://localhost:5173/customer
- **Internal Dashboard**: http://localhost:5173/internal

### Backend APIs
- **Order Service**: http://localhost:8091/api/orders
- **Planning Service**: http://localhost:8092/api/planning
- **Swagger UI**: http://localhost:8091/swagger-ui.html

---

## 📝 Demo Credentials

**Customer**: `demo@customer.com` / `demo123`  
**Dispatcher**: `dispatcher@danxils.com` / `admin123`  
**Driver**: `driver@danxils.com` / `driver123`

---

## 📋 Prerequisites

- Docker & Docker Compose
- Java 17+
- Maven
- Node.js 18+

---

## 🔧 Troubleshooting

**Check logs**:
```bash
tail -f logs/order-service.log
```

**Restart service**:
```bash
./stop-danxils.sh
./start-danxils.sh
```

---

**Version**: 1.0.0
