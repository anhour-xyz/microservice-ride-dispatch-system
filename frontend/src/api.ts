import type { AuthSession, NearbyDriver, Ride, RideRequest, UserProfile, UserRole } from './types'

import type { RegisterResponse } from './types'

const trimTrailingSlash = (value: string) => value.replace(/\/$/, '')
const rideBase = trimTrailingSlash(import.meta.env.VITE_RIDE_API_URL ?? '')
const locationBase = trimTrailingSlash(import.meta.env.VITE_LOCATION_API_URL ?? '')
const authBase = trimTrailingSlash(import.meta.env.VITE_AUTH_API_URL ?? 'http://localhost:8081')
const userBase = trimTrailingSlash(import.meta.env.VITE_USER_API_URL ?? 'http://localhost:8085')

export class ApiError extends Error {
  constructor(message: string, public readonly status: number) {
    super(message)
  }
}

async function request<T>(url: string, options?: RequestInit): Promise<T> {
  const response = await fetch(url, {
    ...options,
    headers: { 'Content-Type': 'application/json', ...options?.headers },
  })
  if (!response.ok) {
    const detail = await response.text().catch(() => '')
    let message = detail || `Request failed (${response.status})`
    try {
      const parsed = JSON.parse(detail) as Record<string, unknown>
      const validation = parsed.validationErrors && typeof parsed.validationErrors === 'object'
        ? Object.values(parsed.validationErrors as Record<string, string>).join(', ')
        : ''
      message = String(parsed.message || parsed.error || validation || message)
    } catch { /* Preserve the text response. */ }
    throw new ApiError(message, response.status)
  }
  if (response.status === 204) return undefined as T
  const text = await response.text()
  if (!text) return undefined as T
  return (response.headers.get('content-type')?.includes('application/json') ? JSON.parse(text) : text) as T
}

const bearer = (accessToken: string) => ({ Authorization: `Bearer ${accessToken}` })

export const authApi = {
  register: (payload: { email: string; password: string; role: UserRole }) =>
    request<RegisterResponse>(`${authBase}/api/v1/auth/register`, { method: 'POST', body: JSON.stringify(payload) }),
  verifyEmail: (token: string) =>
    request<void>(`${authBase}/api/v1/auth/verify-email`, { method: 'POST', body: JSON.stringify({ token }) }),
  resendVerification: (email: string) =>
    request<void>(`${authBase}/api/v1/auth/resend-verification`, { method: 'POST', body: JSON.stringify({ email }) }),
  login: (payload: { email: string; password: string }) =>
    request<AuthSession>(`${authBase}/api/v1/auth/login`, { method: 'POST', body: JSON.stringify(payload) }),
  logout: (session: AuthSession) =>
    request<void>(`${authBase}/api/v1/auth/logout`, { method: 'POST', headers: bearer(session.accessToken), body: JSON.stringify({ refreshToken: session.refreshToken }) }),
  getMyProfile: (accessToken: string) =>
    request<UserProfile>(`${userBase}/api/v1/users/me`, { headers: bearer(accessToken) }),
  createMyProfile: (accessToken: string, payload: { displayName: string; phoneNumber: string }) =>
    request<UserProfile>(`${userBase}/api/v1/users/me`, { method: 'POST', headers: bearer(accessToken), body: JSON.stringify(payload) }),
  updateMyProfile: (accessToken: string, payload: { displayName?: string; phoneNumber?: string }) =>
    request<UserProfile>(`${userBase}/api/v1/users/me`, { method: 'PATCH', headers: bearer(accessToken), body: JSON.stringify(payload) }),
}

export const api = {
  requestRide: (payload: RideRequest) => request<Ride>(`${rideBase}/api/v1/rides/request`, { method: 'POST', body: JSON.stringify(payload) }),
  getRide: (id: string) => request<Ride>(`${rideBase}/api/v1/rides/${encodeURIComponent(id)}`),
  getRiderRides: (riderId: string) => request<Ride[]>(`${rideBase}/api/v1/rides/rider/${encodeURIComponent(riderId)}`),
  startRide: (id: string) => request<Ride>(`${rideBase}/api/v1/rides/${encodeURIComponent(id)}/start`, { method: 'PUT' }),
  completeRide: (id: string) => request<Ride>(`${rideBase}/api/v1/rides/${encodeURIComponent(id)}/complete`, { method: 'PUT' }),
  cancelRide: (id: string) => request<Ride>(`${rideBase}/api/v1/rides/${encodeURIComponent(id)}/cancel`, { method: 'PATCH' }),
  updateDriverLocation: (driverId: string, latitude: number, longitude: number) =>
    request<void>(`${locationBase}/api/v1/locations/drivers/update`, { method: 'POST', body: JSON.stringify({ driverId, latitude, longitude }) }),
  removeDriver: (driverId: string) => request<void>(`${locationBase}/api/v1/locations/drivers/${encodeURIComponent(driverId)}`, { method: 'DELETE' }),
  nearbyDrivers: (latitude: number, longitude: number, radius = 5) =>
    request<NearbyDriver[]>(`${locationBase}/api/v1/locations/drivers/nearby?${new URLSearchParams({ latitude: String(latitude), longitude: String(longitude), radius: String(radius) })}`),
}
