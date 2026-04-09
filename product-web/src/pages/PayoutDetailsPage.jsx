import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { payoutsApi } from '../api/services';
import { Loader } from '../components/ui/Loader';
import { PageHeader } from '../components/ui/PageHeader';
import { formatCurrency, formatDate, formatDateTime, statusLabels } from '../utils/formatters';

const transitions = ['PREPARED', 'PAID', 'CANCELLED'];
const statusActionLabels = {
  PREPARED: 'Подготовлена',
  PAID: 'Выдана',
  CANCELLED: 'Отменена',
};

export function PayoutDetailsPage() {
  const { id } = useParams();
  const [payout, setPayout] = useState(null);

  useEffect(() => {
    payoutsApi.getById(id).then(setPayout);
  }, [id]);

  if (!payout) return <Loader text="Загрузка карточки выплаты..." />;

  const updateStatus = async (status) => {
    if (status === payout.status) {
      return;
    }

    const isConfirmed = window.confirm(
      `Подтвердите изменение статуса выплаты ${payout.payoutCode} на «${statusLabels[status]}».`,
    );
    if (!isConfirmed) {
      return;
    }

    const updated = await payoutsApi.updateStatus(id, status);
    setPayout(updated);
  };

  return (
    <div className="stack">
      <PageHeader
        eyebrow="Карточка выплаты"
        title={payout.payoutCode}
        description="Просмотр деталей, статуса и служебных комментариев."
      />
      <div className="dashboard-grid">
        <div className="panel stack-sm">
          <div className="detail-row"><span>Сотрудник</span><strong>{payout.employeeName}</strong></div>
          <div className="detail-row"><span>Тип выплаты</span><strong>{payout.payoutType}</strong></div>
          <div className="detail-row"><span>Сумма</span><strong>{formatCurrency(payout.amount)}</strong></div>
          <div className="detail-row"><span>Дата выплаты</span><strong>{formatDate(payout.payoutDate)}</strong></div>
          <div className="detail-row"><span>Статус</span><strong>{statusLabels[payout.status]}</strong></div>
          <div className="detail-row"><span>Создал</span><strong>{payout.createdByName}</strong></div>
          <div className="detail-row"><span>Подготовлена</span><strong>{formatDateTime(payout.preparedAt)}</strong></div>
          <div className="detail-row"><span>Выдана</span><strong>{formatDateTime(payout.paidAt)}</strong></div>
        </div>
        <div className="panel stack-sm">
          <div><strong>Основание</strong><p>{payout.basis}</p></div>
          <div><strong>Комментарий</strong><p>{payout.comment}</p></div>
          <div><strong>Примечание</strong><p>{payout.payoutNote || '—'}</p></div>
          <div className="status-actions-header">
            <strong>Изменение статуса выплаты</strong>
            <p>Используйте кнопки ниже, чтобы перевести выплату в следующий рабочий статус.</p>
          </div>
          <div className="button-row">
            {transitions.map((status) => (
              <button
                key={status}
                type="button"
                className="ghost-button"
                disabled={status === payout.status}
                onClick={() => updateStatus(status)}
              >
                Отметить как {statusActionLabels[status]}
              </button>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
