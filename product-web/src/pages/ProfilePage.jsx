import { useState } from 'react';
import { usersApi } from '../api/services';
import { ErrorBanner } from '../components/ui/ErrorBanner';
import { PageHeader } from '../components/ui/PageHeader';
import { UserAvatar } from '../components/ui/UserAvatar';
import { useAuth } from '../hooks/useAuth';
import { getApiErrorMessage } from '../utils/api';
import { roleLabels } from '../utils/formatters';

export function ProfilePage() {
  const { user, setUser } = useAuth();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handlePhotoChange = async (event) => {
    const file = event.target.files?.[0];
    if (!file) {
      return;
    }
    if (!file.type.startsWith('image/')) {
      setError('Можно загрузить только изображение');
      return;
    }

    const photoUrl = await new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => resolve(String(reader.result));
      reader.onerror = () => reject(new Error('Не удалось прочитать файл'));
      reader.readAsDataURL(file);
    }).catch(() => null);

    if (!photoUrl) {
      setError('Не удалось подготовить фотографию');
      return;
    }

    setLoading(true);
    setError('');
    try {
      const nextUser = await usersApi.updateOwnPhoto({ photoUrl });
      setUser(nextUser);
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, 'Не удалось сохранить фотографию профиля'));
    } finally {
      setLoading(false);
      event.target.value = '';
    }
  };

  return (
    <div className="stack">
      <PageHeader
        eyebrow="Профиль"
        title={user.fullName}
        description="Информация о текущем пользователе системы."
      />
      <ErrorBanner message={error} />
      <div className="panel stack-sm">
        <div className="profile-header">
          <UserAvatar user={user} size="lg" />
          <div className="stack-xs">
            <strong>{user.fullName}</strong>
            <span className="card-meta">{roleLabels[user.role]}</span>
          </div>
        </div>
        {user.role === 'EMPLOYEE' ? (
          <label className="profile-upload">
            <span>Фотография профиля</span>
            <input type="file" accept="image/*" disabled={loading} onChange={handlePhotoChange} />
            <small>{loading ? 'Сохраняем фотографию...' : 'Сотрудник может установить свою фотографию. Если фото не выбрано, показывается заглушка.'}</small>
          </label>
        ) : null}
        <div className="detail-row"><span>Email</span><strong>{user.email}</strong></div>
        <div className="detail-row"><span>Роль</span><strong>{roleLabels[user.role]}</strong></div>
        <div className="detail-row"><span>Подразделение</span><strong>{user.department}</strong></div>
        <div className="detail-row"><span>Должность</span><strong>{user.position}</strong></div>
        <div className="detail-row"><span>Табельный номер</span><strong>{user.employeeCode}</strong></div>
      </div>
    </div>
  );
}
