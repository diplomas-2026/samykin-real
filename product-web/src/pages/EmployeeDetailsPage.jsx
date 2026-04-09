import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { employeesApi } from '../api/services';
import { ErrorBanner } from '../components/ui/ErrorBanner';
import { Loader } from '../components/ui/Loader';
import { PageHeader } from '../components/ui/PageHeader';
import { UserAvatar } from '../components/ui/UserAvatar';
import { getApiErrorMessage } from '../utils/api';
import { formatCurrency, formatDate, statusLabels } from '../utils/formatters';

export function EmployeeDetailsPage() {
  const { id } = useParams();
  const [details, setDetails] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    employeesApi.getById(id)
      .then(setDetails)
      .catch((requestError) => {
        setError(getApiErrorMessage(requestError, 'Не удалось загрузить карточку сотрудника'));
      });
  }, [id]);

  if (!details) {
    if (error) {
      return (
        <div className="stack">
          <PageHeader
            eyebrow="Сотрудник"
            title="Карточка сотрудника"
            description="Подробная информация о сотруднике и его денежных выплатах."
          />
          <ErrorBanner message={error} />
        </div>
      );
    }
    return <Loader text="Загрузка карточки сотрудника..." />;
  }

  const { employee, payouts } = details;

  return (
    <div className="stack">
      <PageHeader
        eyebrow="Сотрудник"
        title={employee.fullName}
        description="Подробная информация о сотруднике и его денежных выплатах."
        actions={<Link className="ghost-button" to="/employees">Назад к списку</Link>}
      />
      <ErrorBanner message={error} />

      <section className="dashboard-grid">
        <div className="panel stack-sm">
          <div className="profile-header">
            <UserAvatar user={employee} size="lg" />
            <div className="stack-xs">
              <strong>{employee.fullName}</strong>
              <span className="card-meta">{employee.position}</span>
            </div>
          </div>
          <div className="detail-row"><span>Подразделение</span><strong>{employee.department}</strong></div>
          <div className="detail-row"><span>Email</span><strong>{employee.email}</strong></div>
          <div className="detail-row"><span>Табельный номер</span><strong>{employee.employeeCode}</strong></div>
        </div>

        <div className="panel stack-sm">
          <div className="detail-row"><span>Всего выплат</span><strong>{payouts.length}</strong></div>
          <div className="detail-row"><span>Выдано</span><strong>{payouts.filter((payout) => payout.status === 'PAID').length}</strong></div>
          <div className="detail-row"><span>Подготовлено</span><strong>{payouts.filter((payout) => payout.status === 'PREPARED').length}</strong></div>
          <div className="detail-row"><span>Ожидают обработки</span><strong>{payouts.filter((payout) => payout.status === 'CREATED').length}</strong></div>
        </div>
      </section>

      <div className="panel">
        <h3>Выплаты сотрудника</h3>
        {payouts.length === 0 ? (
          <div className="empty-state">
            <strong>У сотрудника пока нет выплат</strong>
            <p>Когда бухгалтер создаст выплату, она появится в этой таблице.</p>
          </div>
        ) : (
          <table className="table">
            <thead>
              <tr>
                <th>Код</th>
                <th>Тип</th>
                <th>Сумма</th>
                <th>Дата</th>
                <th>Статус</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {payouts.map((payout) => (
                <tr key={payout.id}>
                  <td>{payout.payoutCode}</td>
                  <td>{payout.payoutType}</td>
                  <td>{formatCurrency(payout.amount)}</td>
                  <td>{formatDate(payout.payoutDate)}</td>
                  <td><span className={`status-badge status-${payout.status.toLowerCase()}`}>{statusLabels[payout.status]}</span></td>
                  <td><Link to={`/payouts/${payout.id}`}>Открыть выплату</Link></td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
