# Relay frontend

React + TypeScript frontend for the ride dispatch microservices.

## Run locally

```bash
npm install
npm run dev
```

Open `http://localhost:5173`. Vite proxies ride calls to `localhost:8083` and location calls to `localhost:8082`, matching the Spring service configuration.

For deployed environments, copy `.env.example` to `.env.local` and set the two API origins. The ride UI expects `POST /api/v1/rides`, `GET /api/v1/rides/{id}`, and `PATCH /api/v1/rides/{id}/cancel`. The driver UI uses the location endpoints already represented in the backend.

## Production build

```bash
npm run build
npm run preview
```
