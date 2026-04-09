import { Link, NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { roleLabels } from '../../utils/formatters';

export function AppShell() {
  const { user, logout } = useAuth();

  const links = [
    { to: '/dashboard', label: 'Дашборд', roles: ['ADMIN', 'ACCOUNTANT'] },
    { to: '/users', label: 'Пользователи', roles: ['ADMIN'] },
    { to: '/employees', label: 'Сотрудники', roles: ['ADMIN', 'ACCOUNTANT'] },
    { to: '/payouts', label: 'Выплаты', roles: ['ADMIN', 'ACCOUNTANT'] },
    { to: '/my-payouts', label: 'Мои выплаты', roles: ['EMPLOYEE'] },
    { to: '/profile', label: 'Профиль', roles: ['ADMIN', 'ACCOUNTANT', 'EMPLOYEE'] },
  ];

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <Link to="/" className="brand">
          Samykin Pay
        </Link>
        <div className="sidebar-user">
          <strong>{user.fullName}</strong>
          <span>{roleLabels[user.role]}</span>
        </div>
        <nav>
          {links
            .filter((link) => link.roles.includes(user.role))
            .map((link) => (
              <NavLink key={link.to} to={link.to} className="nav-link">
                {link.label}
              </NavLink>
            ))}
        </nav>
        <button type="button" className="ghost-button" onClick={logout}>
          Выйти
        </button>
      </aside>
      <main className="main-content">
        <Outlet />
      </main>
    </div>
  );
}
