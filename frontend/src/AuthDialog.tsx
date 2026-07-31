import { useEffect, useState } from 'react'
import { Check, LogIn, LogOut, UserPlus, X } from 'lucide-react'
import { ApiError, authApi } from './api'
import type { AuthSession, UserProfile, UserRole } from './types'

type Props = {
  open: boolean
  session: AuthSession | null
  profile: UserProfile | null
  onClose: () => void
  onAuthenticated: (session: AuthSession, profile: UserProfile) => void
  onProfileChanged: (profile: UserProfile) => void
  onSignedOut: () => void
}

type View = 'login' | 'register' | 'profile' | 'account'

export function AuthDialog({ open, session, profile, onClose, onAuthenticated, onProfileChanged, onSignedOut }: Props) {
  const [view, setView] = useState<View>('login')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [displayName, setDisplayName] = useState('')
  const [phoneNumber, setPhoneNumber] = useState('')
  const [role, setRole] = useState<UserRole>('RIDER')
  const [pendingSession, setPendingSession] = useState<AuthSession | null>(null)
  const [error, setError] = useState('')
  const [confirmation, setConfirmation] = useState('')
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    if (!open) return
    setView(session && profile ? 'account' : 'login')
    setDisplayName(profile?.displayName ?? '')
    setPhoneNumber(profile?.phoneNumber ?? '')
    setError('')
    setConfirmation('')
  }, [open, session, profile])

  if (!open) return null

  const message = (value: unknown) => value instanceof Error ? value.message : 'Request failed.'

  const login = async (event: React.FormEvent) => {
    event.preventDefault(); setBusy(true); setError('')
    try {
      const nextSession = await authApi.login({ email, password })
      try {
        const nextProfile = await authApi.getMyProfile(nextSession.accessToken)
        onAuthenticated(nextSession, nextProfile); onClose()
      } catch (profileError) {
        if (profileError instanceof ApiError && profileError.status === 404) {
          setPendingSession(nextSession); setView('profile')
        } else throw profileError
      }
    } catch (value) { setError(message(value)) }
    finally { setBusy(false) }
  }

  const register = async (event: React.FormEvent) => {
    event.preventDefault(); setBusy(true); setError(''); setConfirmation('')
    try {
      const response = await authApi.register({ email, password, role })
      setConfirmation(response.message)
      setView('login')
    } catch (value) { setError(message(value)) }
    finally { setBusy(false) }
  }

  const resendVerification = async () => {
    if (!email) return setError('Enter your email address first.')
    setBusy(true); setError(''); setConfirmation('')
    try {
      await authApi.resendVerification(email)
      setConfirmation('If the account is unverified, a new verification link has been created.')
    } catch (value) { setError(message(value)) }
    finally { setBusy(false) }
  }

  const completeProfile = async (event: React.FormEvent) => {
    event.preventDefault(); if (!pendingSession) return
    setBusy(true); setError('')
    try {
      const nextProfile = await authApi.createMyProfile(pendingSession.accessToken, { displayName, phoneNumber })
      onAuthenticated(pendingSession, nextProfile); onClose()
    } catch (value) { setError(message(value)) }
    finally { setBusy(false) }
  }

  const saveProfile = async (event: React.FormEvent) => {
    event.preventDefault(); if (!session) return
    setBusy(true); setError('')
    try {
      const nextProfile = await authApi.updateMyProfile(session.accessToken, { displayName, phoneNumber })
      onProfileChanged(nextProfile); onClose()
    } catch (value) { setError(message(value)) }
    finally { setBusy(false) }
  }

  const signOut = async () => {
    if (!session) return
    setBusy(true); setError('')
    try { await authApi.logout(session) }
    catch { /* Clear local state even if the session already expired. */ }
    finally { setBusy(false); onSignedOut(); onClose() }
  }

  return <div className="auth-overlay" role="presentation" onMouseDown={event => event.target === event.currentTarget && onClose()}>
    <section className="auth-dialog" role="dialog" aria-modal="true" aria-labelledby="auth-title">
      <button className="auth-close" onClick={onClose} aria-label="Close"><X size={19}/></button>
      {view === 'account' && profile ? <>
        <p className="auth-kicker">SIGNED IN</p><h2 id="auth-title">Your account</h2>
        <div className="account-identity"><span>{profile.displayName.slice(0, 1).toUpperCase()}</span><div><strong>{profile.displayName}</strong><small>{profile.roles.join(' · ')}</small></div></div>
        <form onSubmit={saveProfile}><Field label="DISPLAY NAME" value={displayName} setValue={setDisplayName}/><Field label="PHONE NUMBER" value={phoneNumber} setValue={setPhoneNumber} type="tel"/>{error && <ErrorMessage text={error}/>}<button className="primary" disabled={busy}><Check size={18}/> {busy ? 'Saving...' : 'Save profile'}</button></form>
        <button className="auth-signout" onClick={signOut} disabled={busy}><LogOut size={17}/> Sign out</button>
      </> : <>
        <p className="auth-kicker">RIDE DISPATCH</p><h2 id="auth-title">{view === 'login' ? 'Welcome back' : view === 'register' ? 'Create your account' : 'Complete your profile'}</h2>
        {view !== 'profile' && <div className="auth-tabs"><button className={view === 'login' ? 'active' : ''} onClick={() => { setView('login'); setError('') }}>Sign in</button><button className={view === 'register' ? 'active' : ''} onClick={() => { setView('register'); setError('') }}>Register</button></div>}
        <form onSubmit={view === 'login' ? login : view === 'register' ? register : completeProfile}>
          {view !== 'profile' && <><Field label="EMAIL" value={email} setValue={setEmail} type="email"/><Field label="PASSWORD" value={password} setValue={setPassword} type="password" minLength={8}/></>}
          {view !== 'login' && <><Field label="DISPLAY NAME" value={displayName} setValue={setDisplayName}/><Field label="PHONE NUMBER" value={phoneNumber} setValue={setPhoneNumber} type="tel"/>
            {view === 'register' && <div className="role-selector"><button type="button" className={role === 'RIDER' ? 'active' : ''} onClick={() => setRole('RIDER')}>Rider</button><button type="button" className={role === 'DRIVER' ? 'active' : ''} onClick={() => setRole('DRIVER')}>Driver</button></div>}</>}
          {confirmation && <div className="notice success"><Check size={16}/>{confirmation}</div>}
          {error && <ErrorMessage text={error}/>}<button className="primary" disabled={busy}>{view === 'login' ? <LogIn size={18}/> : <UserPlus size={18}/>} {busy ? 'Please wait...' : view === 'login' ? 'Sign in' : view === 'register' ? 'Create account' : 'Save profile'}</button>
          {view === 'login' && <button className="auth-resend" type="button" disabled={busy} onClick={resendVerification}>Resend verification link</button>}
        </form>
      </>}
    </section>
  </div>
}

function Field({ label, value, setValue, type = 'text', minLength }: { label: string; value: string; setValue: (value: string) => void; type?: string; minLength?: number }) {
  return <label>{label}<input type={type} value={value} minLength={minLength} onChange={event => setValue(event.target.value)} required autoComplete={type === 'password' ? 'current-password' : undefined}/></label>
}

function ErrorMessage({ text }: { text: string }) {
  return <div className="notice error"><X size={16}/>{text}</div>
}
