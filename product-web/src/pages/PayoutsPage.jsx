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
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [sortBy, setSortBy] = useState('date-desc');

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

  const normalizedSearch = search.trim().toLowerCase();
  const filteredPayouts = payouts
    .filter((payout) => {
      if (statusFilter !== 'ALL' && payout.status !== statusFilter) {
        return false;
      }

      if (!normalizedSearch) {
        return true;
      }

      return [payout.payoutCode, payout.employeeName, payout.payoutType]
        .filter(Boolean)
        .some((value) => String(value).toLowerCase().includes(normalizedSearch));
    })
    .sort((left, right) => {
      switch (sortBy) {
        case 'date-asc':
          return new Date(left.payoutDate) - new Date(right.payoutDate);
        case 'amount-desc':
          return Number(right.amount) - Number(left.amount);
        case 'amount-asc':
          return Number(left.amount) - Number(right.amount);
        case 'employee-asc':
          return left.employeeName.localeCompare(right.employeeName, 'ru');
        case 'date-desc':
        default:
          return new Date(right.payoutDate) - new Date(left.payoutDate);
      }
    });

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
        <div className="filters-bar">
          <label className="filter-field filter-field--search">
            Поиск
            <input
              type="search"
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              placeholder="Код, сотрудник или тип выплаты"
            />
          </label>

          <label className="filter-field">
            Статус
            <select value={statusFilter} onChange={(event) => setStatusFilter(event.target.value)}>
              <option value="ALL">Все статусы</option>
              {Object.entries(statusLabels).map(([status, label]) => (
                <option key={status} value={status}>{label}</option>
              ))}
            </select>
          </label>

          <label className="filter-field">
            Сортировка
            <select value={sortBy} onChange={(event) => setSortBy(event.target.value)}>
              <option value="date-desc">Сначала новые</option>
              <option value="date-asc">Сначала ранние</option>
              <option value="amount-desc">По сумме: больше</option>
              <option value="amount-asc">По сумме: меньше</option>
              <option value="employee-asc">По сотруднику А-Я</option>
            </select>
          </label>
        </div>

        {filteredPayouts.length === 0 ? (
          <div className="empty-state">
            <strong>Ничего не найдено</strong>
            <p>Попробуйте изменить строку поиска, фильтр по статусу или выбранную сортировку.</p>
          </div>
        ) : null}

        {filteredPayouts.length > 0 ? (
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
              {filteredPayouts.map((payout) => (
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
        ) : null}
      </div>
    </div>
  );
}
