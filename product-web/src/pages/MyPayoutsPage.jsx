import { useEffect, useState } from 'react';
import { payoutsApi } from '../api/services';
import { Loader } from '../components/ui/Loader';
import { PageHeader } from '../components/ui/PageHeader';
import { formatCurrency, formatDate, statusLabels } from '../utils/formatters';

export function MyPayoutsPage() {
  const [payouts, setPayouts] = useState(null);

  useEffect(() => {
    payoutsApi.getMine().then(setPayouts);
  }, []);

  if (!payouts) return <Loader text="Загрузка моих выплат..." />;

  return (
    <div className="stack">
      <PageHeader
        eyebrow="Мои выплаты"
        title="Личный журнал выплат"
        description="Сотрудник видит только свои начисления, статусы и даты выдачи."
      />
      <div className="card-grid">
        {payouts.map((payout) => (
          <div key={payout.id} className="employee-card">
            <strong>{payout.payoutType}</strong>
            <span>{formatCurrency(payout.amount)}</span>
            <span>{formatDate(payout.payoutDate)}</span>
            <span className={`status-badge status-${payout.status.toLowerCase()}`}>{statusLabels[payout.status]}</span>
            <p>{payout.comment}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
