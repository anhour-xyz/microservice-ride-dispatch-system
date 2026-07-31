import { useEffect, useState } from 'react'
import { CheckCircle2, LoaderCircle, XCircle } from 'lucide-react'
import { authApi } from './api'

type Status = 'verifying' | 'success' | 'error'

export function VerifyEmailPage() {
  const [status, setStatus] = useState<Status>('verifying')
  const [message, setMessage] = useState('Verifying your email address...')

  useEffect(() => {
    const token = new URLSearchParams(window.location.search).get('token')
    if (!token) {
      setStatus('error')
      setMessage('The verification token is missing.')
      return
    }

    authApi.verifyEmail(token)
      .then(() => {
        setStatus('success')
        setMessage('Your email has been verified. You can now sign in.')
      })
      .catch((error: unknown) => {
        setStatus('error')
        setMessage(error instanceof Error ? error.message : 'Email verification failed.')
      })
  }, [])

  return <main className="verification-page">
    <section className="verification-panel">
      {status === 'verifying' && <LoaderCircle className="verification-spinner" size={36}/>} 
      {status === 'success' && <CheckCircle2 className="verification-success" size={36}/>} 
      {status === 'error' && <XCircle className="verification-error" size={36}/>} 
      <h1>{status === 'verifying' ? 'Verifying email' : status === 'success' ? 'Email verified' : 'Verification failed'}</h1>
      <p>{message}</p>
      {status !== 'verifying' && <a className="primary verification-action" href="/">Return to sign in</a>}
    </section>
  </main>
}
