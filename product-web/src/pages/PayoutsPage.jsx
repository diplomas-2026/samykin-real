import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { payoutsApi, reportsApi } from '../api/services';
import { ErrorBanner } from '../components/ui/ErrorBanner';
import { Loader } from '../components/ui/Loader';
import { PageHeader } from '../components/ui/PageHeader';
import { downloadBlob, formatCurrency, formatDate, statusLabels } from '../utils/formatters';
import { getApiErrorMessage } from '../utils/api';

export function PayoutsPage() {
  const [payouts, setPayouts] = useState(null);
  const [error, setError] = useState('');
  const [reportLoading, setReportLoading] = useState('');

  useEffect(() => {
    payoutsApi.getAll()
      .then(setPayouts)
      .catch((requestError) => {
        setError(getApiErrorMessage(requestError, 'Не удалось загрузить журнал выплат'));
      });
  }, []);

  if (!payouts) {
    if (error) {
      return (
        <div className="stack">
          <PageHeader
            eyebrow="Выплаты"
            title="Журнал денежных выплат"
            description="Бухгалтер создает, подготавливает и закрывает выплаты сотрудникам."
          />
          <ErrorBanner message={error} />
        </div>
      );
    }
    return <Loader text="Загрузка выплат..." />;
  }

  const exportPeriod = async () => {
    setReportLoading('payouts-period');
    setError('');
    try {
      const blob = await reportsApi.download('payouts-period', { from: '2026-04-01', to: '2026-04-30' });
      downloadBlob(blob, 'viplaty-za-aprel.xlsx');
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, 'Не удалось сформировать Excel за период'));
    } finally {
      setReportLoading('');
    }
  };

  const exportStatus = async () => {
    setReportLoading('status');
    setError('');
    try {
      const blob = await reportsApi.download('status');
      downloadBlob(blob, 'status-vyplat.xlsx');
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, 'Не удалось сформировать Excel по статусам'));
    } finally {
      setReportLoading('');
    }
  };

  return (
    <div className="stack">
      <PageHeader
        eyebrow="Выплаты"
        title="Журнал денежных выплат"
        description="Бухгалтер создает, подготавливает и закрывает выплаты сотрудникам."
        actions={(
          <div className="button-row">
            <button className="ghost-button" type="button" disabled={reportLoading === 'payouts-period'} onClick={exportPeriod}>
              {reportLoading === 'payouts-period' ? 'Формируем Excel...' : 'Excel за период'}
            </button>
            <button className="ghost-button" type="button" disabled={reportLoading === 'status'} onClick={exportStatus}>
              {reportLoading === 'status' ? 'Формируем Excel...' : 'Excel по статусам'}
            </button>
            <Link className="primary-button" to="/payouts/new">Создать выплату</Link>
          </div>
        )}
      />
      <ErrorBanner message={error} />
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
