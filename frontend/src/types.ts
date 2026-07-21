export type Coordinates = { latitude: number; longitude: number }

export type RideRequest = {
  riderId: string
  pickupLatitude: number
  pickupLongitude: number
  pickupAddress: string
  dropLatitude: number
  dropLongitude: number
  dropAddress: string
}

export type Ride = RideRequest & {
  id: string
  driverId?: string
  status: 'REQUESTED' | 'MATCHING' | 'ACCEPTED' | 'DRIVER_ARRIVING' | 'RIDE_STARTED' | 'COMPLETED' | 'CANCELLED'
  estimatedFare?: number
  actualFare?: number
  createdAt?: string
  updatedAt?: string
  startedAt?: string
  completedAt?: string
}

export type NearbyDriver = Coordinates & { driverId: string; distanceInKm: number }
