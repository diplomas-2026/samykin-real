import { Navigate } from 'react-router-dom';
import { Loader } from '../components/ui/Loader';
import { useAuth } from '../hooks/useAuth';

export function ProtectedRoute({ roles, children }) {
  const { user, loading } = useAuth();
  if (loading) {
    return <Loader />;
  }
  if (!user) {
    return <Navigate to="/login" replace />;
  }
  if (roles && !roles.includes(user.role)) {
    return <Navigate to={user.role === 'EMPLOYEE' ? '/my-payouts' : '/dashboard'} replace />;
  }
  return children;
}
