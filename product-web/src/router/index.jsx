import { createBrowserRouter } from 'react-router-dom';
import { AppShell } from '../components/layout/AppShell';
import { DashboardPage } from '../pages/DashboardPage';
import { EmployeesPage } from '../pages/EmployeesPage';
import { LandingPage } from '../pages/LandingPage';
import { LoginPage } from '../pages/LoginPage';
import { MyPayoutsPage } from '../pages/MyPayoutsPage';
import { PayoutDetailsPage } from '../pages/PayoutDetailsPage';
import { PayoutFormPage } from '../pages/PayoutFormPage';
import { PayoutsPage } from '../pages/PayoutsPage';
import { ProfilePage } from '../pages/ProfilePage';
import { UserFormPage } from '../pages/UserFormPage';
import { UsersPage } from '../pages/UsersPage';
import { ProtectedRoute } from './ProtectedRoute';

export const router = createBrowserRouter([
  { path: '/', element: <LandingPage /> },
  { path: '/login', element: <LoginPage /> },
  {
    path: '/',
    element: (
      <ProtectedRoute>
        <AppShell />
      </ProtectedRoute>
    ),
    children: [
      {
        path: 'dashboard',
        element: (
          <ProtectedRoute roles={['ADMIN', 'ACCOUNTANT']}>
            <DashboardPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'users',
        element: (
          <ProtectedRoute roles={['ADMIN']}>
            <UsersPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'users/new',
        element: (
          <ProtectedRoute roles={['ADMIN']}>
            <UserFormPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'users/:id',
        element: (
          <ProtectedRoute roles={['ADMIN']}>
            <UserFormPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'employees',
        element: (
          <ProtectedRoute roles={['ADMIN', 'ACCOUNTANT']}>
            <EmployeesPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'payouts',
        element: (
          <ProtectedRoute roles={['ADMIN', 'ACCOUNTANT']}>
            <PayoutsPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'payouts/new',
        element: (
          <ProtectedRoute roles={['ADMIN', 'ACCOUNTANT']}>
            <PayoutFormPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'payouts/:id',
        element: (
          <ProtectedRoute roles={['ADMIN', 'ACCOUNTANT']}>
            <PayoutDetailsPage />
          </ProtectedRoute>
        ),
      },
      {
        path: 'my-payouts',
        element: (
          <ProtectedRoute roles={['EMPLOYEE']}>
            <MyPayoutsPage />
          </ProtectedRoute>
        ),
      },
      { path: 'profile', element: <ProfilePage /> },
    ],
  },
]);
