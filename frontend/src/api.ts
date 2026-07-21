import type { NearbyDriver, Ride, RideRequest } from './types'

const trimTrailingSlash = (value: string) => value.replace(/\/$/, '')
const rideBase = trimTrailingSlash(import.meta.env.VITE_RIDE_API_URL ?? '')
const locationBase = trimTrailingSlash(import.meta.env.VITE_LOCATION_API_URL ?? '')

async function request<T>(url: string, options?: RequestInit): Promise<T> {
  const response = await fetch(url, {
    ...options,
    headers: { 'Content-Type': 'application/json', ...options?.headers },
  })
  if (!response.ok) {
    const detail = await response.text().catch(() => '')
    try {
      const parsed = JSON.parse(detail) as Record<string, string>
      const message = parsed.message || parsed.error || Object.values(parsed).join(', ')
      throw new Error(message || `Request failed (${response.status})`)
    } catch (error) {
      if (error instanceof SyntaxError) throw new Error(detail || `Request failed (${response.status})`)
      throw error
    }
  }
  if (response.status === 204) return undefined as T
  const text = await response.text()
  if (!text) return undefined as T
  const contentType = response.headers.get('content-type') ?? ''
  return (contentType.includes('application/json') ? JSON.parse(text) : text) as T
}

export const api = {
  requestRide: (payload: RideRequest) => request<Ride>(`${rideBase}/api/v1/rides`, { method: 'POST', body: JSON.stringify(payload) }),
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
