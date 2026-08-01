import { useEffect, useMemo, useState } from 'react'
import { ArrowRight, CarFront, Check, ChevronDown, CircleUserRound, Crosshair, LocateFixed, MapPin, Menu, Navigation, Radio, Route, Search, ShieldCheck, X } from 'lucide-react'
import { api, authApi } from './api'
import { AuthDialog } from './AuthDialog'
import { VerifyEmailPage } from './VerifyEmailPage'
import type { AuthSession, Ride, RideRequest, UserProfile } from './types'

type Mode = 'rider' | 'driver'
type Notice = { type: 'success' | 'error'; text: string } | null
const SESSION_KEY = 'ride-dispatch-session'

const areas = {
  Seattle: { pickupAddress: 'Pike Place Market', pickupLatitude: 47.6097, pickupLongitude: -122.3425, dropAddress: 'Space Needle', dropLatitude: 47.6205, dropLongitude: -122.3493 },
  Redmond: { pickupAddress: 'Redmond Town Center', pickupLatitude: 47.6709, pickupLongitude: -122.1215, dropAddress: 'Marymoor Park', dropLatitude: 47.6639, dropLongitude: -122.1257 },
  'San Francisco': { pickupAddress: 'Ferry Building', pickupLatitude: 37.7955, pickupLongitude: -122.3937, dropAddress: 'Golden Gate Park', dropLatitude: 37.7694, dropLongitude: -122.4862 },
  'Mountain View': { pickupAddress: 'Downtown Mountain View', pickupLatitude: 37.3947, pickupLongitude: -122.0783, dropAddress: 'Shoreline Amphitheatre', dropLatitude: 37.4267, dropLongitude: -122.0807 },
} as const

type Area = keyof typeof areas

const initialForm: RideRequest = {
  riderId: 'rider-001',
  ...areas.Seattle,
}

const statusLabel: Record<string, string> = {
  REQUESTED: 'Request received', MATCHING: 'Finding your driver', ACCEPTED: 'Driver confirmed',
  DRIVER_ARRIVING: 'Driver is arriving', RIDE_STARTED: 'On the way', COMPLETED: 'Ride complete', CANCELLED: 'Ride cancelled',
}

function App() {
  if (window.location.pathname === '/verify-email') return <VerifyEmailPage />

  const [mode, setMode] = useState<Mode>('rider')
  const [area, setArea] = useState<Area>('Seattle')
  const [form, setForm] = useState(initialForm)
  const [ride, setRide] = useState<Ride | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [notice, setNotice] = useState<Notice>(null)
  const [driverId, setDriverId] = useState('driver-101')
  const [driverOnline, setDriverOnline] = useState(false)
  const [driverCoords, setDriverCoords] = useState({ latitude: 1.3007, longitude: 103.8399 })
  const [session, setSession] = useState<AuthSession | null>(() => {
    try { return JSON.parse(localStorage.getItem(SESSION_KEY) ?? 'null') as AuthSession | null }
    catch { return null }
  })
  const [profile, setProfile] = useState<UserProfile | null>(null)
  const [accountOpen, setAccountOpen] = useState(false)

  useEffect(() => {
    if (!session) return setProfile(null)
    authApi.getMyProfile(session.accessToken).then(setProfile).catch(() => setProfile(null))
    setForm(current => ({ ...current, riderId: session.userId }))
    setDriverId(session.userId)
  }, [session])

  const handleAuthenticated = (nextSession: AuthSession, nextProfile: UserProfile) => {
    localStorage.setItem(SESSION_KEY, JSON.stringify(nextSession))
    setSession(nextSession); setProfile(nextProfile)
    setNotice({ type: 'success', text: `Signed in as ${nextProfile.displayName}.` })
  }

  const handleSignedOut = () => {
    localStorage.removeItem(SESSION_KEY)
    setSession(null); setProfile(null); setDriverOnline(false)
    setNotice({ type: 'success', text: 'You are signed out.' })
  }

  useEffect(() => {
    if (!ride?.id || ['COMPLETED', 'CANCELLED'].includes(ride.status)) return
    const timer = window.setInterval(() => api.getRide(ride.id).then(setRide).catch(() => {}), 5000)
    return () => window.clearInterval(timer)
  }, [ride?.id, ride?.status])

  useEffect(() => {
    if (!driverOnline) return
    const timer = window.setInterval(() => {
      api.updateDriverLocation(driverId, driverCoords.latitude, driverCoords.longitude)
        .catch(() => setNotice({ type: 'error', text: 'Could not refresh your driver location.' }))
    }, 3000)
    return () => window.clearInterval(timer)
  }, [driverOnline, driverId, driverCoords.latitude, driverCoords.longitude])

  const distance = useMemo(() => {
    const dx = (form.dropLongitude - form.pickupLongitude) * 111 * Math.cos(form.pickupLatitude * Math.PI / 180)
    const dy = (form.dropLatitude - form.pickupLatitude) * 111
    return Math.max(0, Math.sqrt(dx * dx + dy * dy))
  }, [form])

  const update = (key: keyof RideRequest, value: string) => setForm(current => ({
    ...current, [key]: key.includes('Latitude') || key.includes('Longitude') ? Number(value) : value,
  }))

  const changeArea = (nextArea: Area) => {
    const next = areas[nextArea]
    setArea(nextArea)
    setForm(current => ({ ...current, ...next }))
    setDriverCoords({ latitude: next.pickupLatitude, longitude: next.pickupLongitude })
    setRide(null)
    setNotice({ type: 'success', text: `Current area changed to ${nextArea}.` })
  }

  const locate = (target: 'pickup' | 'driver') => {
    if (!navigator.geolocation) return setNotice({ type: 'error', text: 'Location is not supported by this browser.' })
    navigator.geolocation.getCurrentPosition(({ coords }) => {
      if (target === 'pickup') setForm(current => ({ ...current, pickupLatitude: coords.latitude, pickupLongitude: coords.longitude, pickupAddress: 'Current location' }))
      else setDriverCoords({ latitude: coords.latitude, longitude: coords.longitude })
      setNotice({ type: 'success', text: 'Current location captured.' })
    }, () => setNotice({ type: 'error', text: 'Location permission was not granted.' }))
  }

  const submitRide = async (event: React.FormEvent) => {
    event.preventDefault()
    if (!session) {
      setAccountOpen(true)
      return setNotice({ type: 'error', text: 'Sign in before requesting a ride.' })
    }
    setSubmitting(true); setNotice(null)
    try {
      const result = await api.requestRide({ ...form, riderId: session.userId })
      setRide(result); setNotice({ type: 'success', text: 'Ride requested. We are finding your driver.' })
    } catch (error) {
      setNotice({ type: 'error', text: error instanceof Error ? error.message : 'Could not request the ride.' })
    } finally { setSubmitting(false) }
  }
  const toggleDriver = async () => {
    if (!session) {
      setAccountOpen(true)
      return setNotice({ type: 'error', text: 'Sign in before going online.' })
    }
    if (!profile?.roles.includes('DRIVER')) {
      return setNotice({ type: 'error', text: 'A driver account is required.' })
    }
    setSubmitting(true); setNotice(null)
    try {
      if (driverOnline) await api.removeDriver(session.userId)
      else await api.updateDriverLocation(session.userId, driverCoords.latitude, driverCoords.longitude)
      setDriverOnline(!driverOnline)
      setNotice({ type: 'success', text: driverOnline ? 'You are now offline.' : 'You are online and ready for requests.' })
    } catch (error) {
      setNotice({ type: 'error', text: error instanceof Error ? error.message : 'Could not update driver status.' })
    } finally { setSubmitting(false) }
  }
  return <div className="app-shell">
    <header className="topbar">
      <a className="brand" href="#"><span className="brand-mark"><Navigation size={18} fill="currentColor" /></span><span>ride dispatch</span></a>
      <div className="header-actions">
        <label className="city">
          <select value={area} onChange={event => changeArea(event.target.value as Area)} aria-label="Current area">
            {Object.keys(areas).map(name => <option key={name} value={name}>{name}</option>)}
          </select>
          <ChevronDown size={15} aria-hidden="true" />
        </label>
        <button
          type="button"
          className="avatar"
          aria-haspopup="dialog"
          aria-expanded={accountOpen}
          onClick={() => setAccountOpen(true)}
        >
          <CircleUserRound size={19} />
          <span>Account</span>
        </button>
        <button type="button" className="menu" aria-label="Open menu"><Menu /></button>
      </div>
    </header>

    <main>
      <section className="hero" id="ride">
        <div className="hero-copy">
          <div className="eyebrow"><span></span> MOVE YOUR WAY</div>
          <h1>Your city.<br/><em>Within reach.</em></h1>
          <p>Reliable rides, matched in moments. Wherever the day takes you, Relay gets you there.</p>
          <div className="trust-row"><span><ShieldCheck size={18}/> Verified drivers</span><span><Radio size={18}/> Live dispatch</span><span><Route size={18}/> Transparent fares</span></div>
        </div>

        <div className="booking-card">
          <div className="mode-tabs">
            <button className={mode === 'rider' ? 'active' : ''} onClick={() => { setMode('rider'); setNotice(null) }}><MapPin size={18}/> Book a ride</button>
            <button className={mode === 'driver' ? 'active' : ''} onClick={() => { setMode('driver'); setNotice(null) }}><CarFront size={19}/> Drive</button>
          </div>
          {notice && <div className={`notice ${notice.type}`}><span>{notice.type === 'success' ? <Check size={16}/> : <X size={16}/>}</span>{notice.text}</div>}
          {mode === 'rider' ? (ride ? <RideStatus ride={ride} onDone={() => setRide(null)} onAction={async action => { try { setSubmitting(true); setNotice(null); setRide(await action(ride.id)) } catch (e) { setNotice({ type: 'error', text: e instanceof Error ? e.message : 'Could not update ride.' }) } finally { setSubmitting(false) } }} submitting={submitting} /> :
            <form onSubmit={submitRide}>
              <label>RIDER ID<input value={session?.userId ?? form.riderId} onChange={e => update('riderId', e.target.value)} disabled={!!session} required /></label>
              <div className="route-fields">
                <span className="route-line"><i></i><b></b></span>
                <label><span>Pickup</span><div className="input-wrap"><input value={form.pickupAddress} onChange={e => update('pickupAddress', e.target.value)} placeholder="Where from?" required/><button type="button" onClick={() => locate('pickup')} title="Use current location"><Crosshair size={18}/></button></div></label>
                <label><span>Drop-off</span><div className="input-wrap"><input value={form.dropAddress} onChange={e => update('dropAddress', e.target.value)} placeholder="Where to?" required/><Search size={18}/></div></label>
              </div>
              <details><summary>Coordinates <ChevronDown size={15}/></summary><div className="coordinates"><input type="number" step="any" value={form.pickupLatitude} onChange={e => update('pickupLatitude', e.target.value)} aria-label="Pickup latitude"/><input type="number" step="any" value={form.pickupLongitude} onChange={e => update('pickupLongitude', e.target.value)} aria-label="Pickup longitude"/><input type="number" step="any" value={form.dropLatitude} onChange={e => update('dropLatitude', e.target.value)} aria-label="Drop latitude"/><input type="number" step="any" value={form.dropLongitude} onChange={e => update('dropLongitude', e.target.value)} aria-label="Drop longitude"/></div></details>
              <div className="estimate"><span>Estimated trip</span><strong>{distance.toFixed(1)} km</strong><small>Fare confirmed after matching</small></div>
              <button className="primary" disabled={submitting}>{submitting ? 'Requesting...' : <>Find a ride <ArrowRight size={19}/></>}</button>
            </form>) : <DriverPanel driverId={driverId} setDriverId={setDriverId} coords={driverCoords} setCoords={setDriverCoords} online={driverOnline} submitting={submitting} locate={() => locate('driver')} toggle={toggleDriver}/>} 
        </div>
      </section>
    </main>
    <AuthDialog
      open={accountOpen}
      session={session}
      profile={profile}
      onClose={() => setAccountOpen(false)}
      onAuthenticated={handleAuthenticated}
      onProfileChanged={setProfile}
      onSignedOut={handleSignedOut}
    />
  </div>
}

function RideStatus({ ride, onDone, onAction, submitting }: { ride: Ride; onDone: () => void; onAction: (action: (id: string) => Promise<Ride>) => void; submitting: boolean }) {
  const progress = ['REQUESTED', 'MATCHING', 'ACCEPTED', 'DRIVER_ARRIVING', 'RIDE_STARTED', 'COMPLETED'].indexOf(ride.status)
  const primaryAction = ride.status === 'ACCEPTED' ? { label: 'Start ride', call: api.startRide } : ride.status === 'RIDE_STARTED' ? { label: 'Complete ride', call: api.completeRide } : null
  return <div className="ride-status">
    <div className="status-icon"><CarFront size={28}/><span/></div>
    <p className="status-kicker">RIDE #{ride.id}</p><h2>{statusLabel[ride.status] ?? ride.status}</h2>
    <p>{ride.driverId ? `Driver ${ride.driverId} is assigned to your trip.` : 'The dispatch network is checking nearby drivers.'}</p>
    <div className="progress">{[0,1,2,3,4,5].map(n => <i key={n} className={n <= progress ? 'done' : ''}/>)}</div>
    <div className="trip-mini"><MapPin size={17}/><span><small>FROM</small>{ride.pickupAddress}</span><ArrowRight size={17}/><span><small>TO</small>{ride.dropAddress}</span></div>
    {ride.estimatedFare != null && <div className="estimate"><span>Estimated fare</span><strong>${ride.estimatedFare.toFixed(2)}</strong></div>}
    {['COMPLETED','CANCELLED'].includes(ride.status) ? <button className="primary" onClick={onDone}>Book another ride</button> : <>
      {primaryAction && <button className="primary" disabled={submitting} onClick={() => onAction(primaryAction.call)}>{submitting ? 'Updating...' : primaryAction.label}</button>}
      <button className="secondary danger" disabled={submitting} onClick={() => onAction(api.cancelRide)}>Cancel ride</button>
    </>}
  </div>
}
function DriverPanel({ driverId, setDriverId, coords, setCoords, online, submitting, locate, toggle }: { driverId: string; setDriverId: (v:string)=>void; coords:{latitude:number;longitude:number}; setCoords:(v:{latitude:number;longitude:number})=>void; online:boolean; submitting:boolean; locate:()=>void; toggle:()=>void }) {
  return <div className="driver-panel">
    <div className={`availability ${online ? 'online' : ''}`}><span><i/>{online ? 'You are online' : 'You are offline'}</span><small>{online ? 'Visible to nearby riders' : 'Go online to receive requests'}</small></div>
    <label>DRIVER ID<input value={driverId} onChange={event => setDriverId(event.target.value)} disabled={online}/></label>
    <div className="driver-location"><div><LocateFixed size={20}/><span><small>CURRENT LOCATION</small>{coords.latitude.toFixed(5)}, {coords.longitude.toFixed(5)}</span></div><button type="button" onClick={locate}>Update</button></div>
    <div className="coordinates"><input type="number" step="any" value={coords.latitude} onChange={event => setCoords({...coords, latitude:Number(event.target.value)})}/><input type="number" step="any" value={coords.longitude} onChange={event => setCoords({...coords, longitude:Number(event.target.value)})}/></div>
    <button className={online ? 'secondary danger full' : 'primary'} disabled={submitting || !driverId} onClick={toggle}>{submitting ? 'Updating...' : online ? 'Go offline' : <>Go online <ArrowRight size={19}/></>}</button>
  </div>
}
export default App
