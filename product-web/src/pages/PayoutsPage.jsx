import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { payoutsApi, reportsApi } from '../api/services';
import { Loader } from '../components/ui/Loader';
import { PageHeader } from '../components/ui/PageHeader';
import { downloadBlob, formatCurrency, formatDate, statusLabels } from '../utils/formatters';

export function PayoutsPage() {
  const [payouts, setPayouts] = useState(null);

  useEffect(() => {
    payoutsApi.getAll().then(setPayouts);
  }, []);

  if (!payouts) return <Loader text="Загрузка выплат..." />;

  const exportPeriod = async () => {
    const blob = await reportsApi.download('payouts-period', { from: '2026-04-01', to: '2026-04-30' });
    downloadBlob(blob, 'viplaty-za-aprel.xlsx');
  };

  const exportStatus = async () => {
    const blob = await reportsApi.download('status');
    downloadBlob(blob, 'status-vyplat.xlsx');
  };

  return (
    <div className="stack">
      <PageHeader
        eyebrow="Выплаты"
        title="Журнал денежных выплат"
        description="Бухгалтер создает, подготавливает и закрывает выплаты сотрудникам."
        actions={(
          <div className="button-row">
            <button className="ghost-button" type="button" onClick={exportPeriod}>Excel за период</button>
            <button className="ghost-button" type="button" onClick={exportStatus}>Excel по статусам</button>
            <Link className="primary-button" to="/payouts/new">Создать выплату</Link>
          </div>
        )}
      />
      <div className="panel">
        <table className="table">
          <thead>
            <tr>
              <th>Код</th>
              <th>Сотрудник</th>
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
                <td>{payout.employeeName}</td>
                <td>{payout.payoutType}</td>
                <td>{formatCurrency(payout.amount)}</td>
                <td>{formatDate(payout.payoutDate)}</td>
                <td><span className={`status-badge status-${payout.status.toLowerCase()}`}>{statusLabels[payout.status]}</span></td>
                <td><Link to={`/payouts/${payout.id}`}>Открыть</Link></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
