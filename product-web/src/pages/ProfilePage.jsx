import { PageHeader } from '../components/ui/PageHeader';
import { useAuth } from '../hooks/useAuth';
import { roleLabels } from '../utils/formatters';

export function ProfilePage() {
  const { user } = useAuth();

  return (
    <div className="stack">
      <PageHeader
        eyebrow="Профиль"
        title={user.fullName}
        description="Информация о текущем пользователе системы."
      />
      <div className="panel stack-sm">
        <div className="detail-row"><span>Email</span><strong>{user.email}</strong></div>
        <div className="detail-row"><span>Роль</span><strong>{roleLabels[user.role]}</strong></div>
        <div className="detail-row"><span>Подразделение</span><strong>{user.department}</strong></div>
        <div className="detail-row"><span>Должность</span><strong>{user.position}</strong></div>
        <div className="detail-row"><span>Табельный номер</span><strong>{user.employeeCode}</strong></div>
      </div>
    </div>
  );
}
