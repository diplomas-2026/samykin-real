import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { usersApi } from '../api/services';
import { Loader } from '../components/ui/Loader';
import { PageHeader } from '../components/ui/PageHeader';
import { UserAvatar } from '../components/ui/UserAvatar';
import { formatDateTime, roleLabels } from '../utils/formatters';

export function UsersPage() {
  const [users, setUsers] = useState(null);

  useEffect(() => {
    usersApi.getAll().then(setUsers);
  }, []);

  if (!users) return <Loader text="Загрузка пользователей..." />;

  return (
    <div className="stack">
      <PageHeader
        eyebrow="Администрирование"
        title="Пользователи системы"
        description="Создание и настройка ролей администратора, бухгалтера и сотрудника."
        actions={<Link className="primary-button" to="/users/new">Создать пользователя</Link>}
      />
      <div className="panel">
        <table className="table">
          <thead>
            <tr>
              <th>ФИО</th>
              <th>Email</th>
              <th>Роль</th>
              <th>Подразделение</th>
              <th>Дата создания</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {users.map((user) => (
              <tr key={user.id}>
                <td>
                  <div className="table-user-cell">
                    <UserAvatar user={user} size="xs" />
                    <span>{user.fullName}</span>
                  </div>
                </td>
                <td>{user.email}</td>
                <td>{roleLabels[user.role]}</td>
                <td>{user.department}</td>
                <td>{formatDateTime(user.createdAt)}</td>
                <td><Link to={`/users/${user.id}`}>Редактировать</Link></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
