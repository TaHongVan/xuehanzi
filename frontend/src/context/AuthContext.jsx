import { createContext, useContext, useState, useEffect } from 'react'
import { authApi } from '../api/services'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('user')
    return saved ? JSON.parse(saved) : null
  })
  const [loading, setLoading] = useState(false)

  const saveAuth = (auth) => {
    localStorage.setItem('token', auth.token)
    const userData = {
      username: auth.username,
      email: auth.email,
      displayName: auth.displayName,
      role: auth.role,
      userId: auth.userId,
    }
    localStorage.setItem('user', JSON.stringify(userData))
    setUser(userData)
    return auth
  }

  const login = async (username, password) => {
    setLoading(true)
    try {
      const { data } = await authApi.login({ username, password })
      return saveAuth(data.data)
    } finally {
      setLoading(false)
    }
  }

  const requestRegistrationOtp = async (name, email, password) => {
    setLoading(true)
    try {
      const { data } = await authApi.register({ name, email, password })
      return data
    } finally {
      setLoading(false)
    }
  }

  const verifyRegistration = async (email, otp) => {
    setLoading(true)
    try {
      const { data } = await authApi.verifyRegistration({ email, otp })
      return saveAuth(data.data)
    } finally {
      setLoading(false)
    }
  }

  const verifyRegistrationLink = async (token) => {
    setLoading(true)
    try {
      const { data } = await authApi.verifyRegistrationLink({ token })
      return saveAuth(data.data)
    } finally {
      setLoading(false)
    }
  }

  const resendRegistration = async (email) => {
    setLoading(true)
    try {
      const { data } = await authApi.resendRegistration({ email })
      return data
    } finally {
      setLoading(false)
    }
  }

  const googleLogin = async (idToken) => {
    setLoading(true)
    try {
      const { data } = await authApi.googleLogin({ idToken })
      return saveAuth(data.data)
    } finally {
      setLoading(false)
    }
  }

  const logout = () => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    setUser(null)
  }

  const isAdmin = user?.role === 'ADMIN'

  return (
    <AuthContext.Provider value={{
      user,
      loading,
      login,
      requestRegistrationOtp,
      verifyRegistration,
      verifyRegistrationLink,
      resendRegistration,
      googleLogin,
      logout,
      isAdmin,
      isAuthenticated: !!user,
    }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)
