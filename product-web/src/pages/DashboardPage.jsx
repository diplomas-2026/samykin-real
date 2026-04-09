import { useEffect, useState } from 'react';
import { Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis, CartesianGrid, Bar, BarChart } from 'recharts';
import { aiApi, dashboardApi, reportsApi } from '../api/services';
import { MetricCard } from '../components/ui/MetricCard';
import { PageHeader } from '../components/ui/PageHeader';
import { Loader } from '../components/ui/Loader';
import { downloadBlob, formatCurrency, formatDateTime, statusLabels } from '../utils/formatters';
import { useAuth } from '../hooks/useAuth';

export function DashboardPage() {
  const { user } = useAuth();
  const [dashboard, setDashboard] = useState(null);
  const [usage, setUsage] = useState(null);
  const [settings, setSettings] = useState(null);
  const [form, setForm] = useState({ styleName: '', styleInstruction: '' });

  useEffect(() => {
    Promise.all([dashboardApi.get(), aiApi.getUsage(), aiApi.getSettings()]).then(([dashboardData, usageData, settingsData]) => {
      setDashboard(dashboardData);
      setUsage(usageData);
      setSettings(settingsData);
      setForm({
        styleName: settingsData.styleName,
        styleInstruction: settingsData.styleInstruction,
      });
    });
  }, []);

  if (!dashboard || !usage || !settings) {
    return <Loader text="Загрузка дашборда..." />;
  }

  const saveSettings = async (event) => {
    event.preventDefault();
    const next = await aiApi.updateSettings(form);
    setSettings(next);
  };

  const exportReport = async (type, fileName, params) => {
    const blob = await reportsApi.download(type, params);
    downloadBlob(blob, fileName);
  };

  return (
    <div className="stack">
      <PageHeader
        eyebrow="Дашборд"
        title="Финансовая панель бухгалтера"
        description="Обзор статусов выплат, сумм и использования AI-функций системы."
        actions={(
          <div className="button-row">
            <button className="ghost-button" type="button" onClick={() => exportReport('employees', 'otchet-po-sotrudnikam.xlsx')}>
              Excel по сотрудникам
            </button>
            <button className="primary-button" type="button" onClick={() => exportReport('summary', 'svodnyy-otchet.xlsx')}>
              Сводный Excel
            </button>
          </div>
        )}
      />

      <section className="metrics-grid">
        <MetricCard label="Всего выплат" value={dashboard.totalPayouts} />
        <MetricCard label="Выдано" value={dashboard.paidPayouts} accent="green" />
        <MetricCard label="Подготовлено" value={dashboard.preparedPayouts} accent="sand" />
        <MetricCard label="Общая сумма" value={formatCurrency(dashboard.totalAmount)} accent="blue" />
      </section>

      <section className="dashboard-grid">
        <div className="panel">
          <h3>Динамика сумм по месяцам</h3>
          <ResponsiveContainer width="100%" height={320}>
            <BarChart data={dashboard.monthlyTotals}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="label" />
              <YAxis />
              <Tooltip />
              <Bar dataKey="amount" fill="#1338be" radius={[12, 12, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div className="panel">
          <h3>Распределение по статусам</h3>
          <ResponsiveContainer width="100%" height={320}>
            <PieChart>
              <Pie data={dashboard.statusDistribution.map((item) => ({ ...item, label: statusLabels[item.status] }))} dataKey="count" nameKey="label" outerRadius={120} fill="#bb9457" />
              <Tooltip />
            </PieChart>
          </ResponsiveContainer>
          <div className="status-inline">
            {dashboard.statusDistribution.map((item) => (
              <span key={item.status} className="status-chip">{statusLabels[item.status]}: {item.count}</span>
            ))}
          </div>
        </div>
      </section>

      <section className="dashboard-grid">
        <div className="panel">
          <h3>AI-лимит на сегодня</h3>
          <div className="usage-list">
            <div><span>Потрачено</span><strong>{usage.usedTokens}</strong></div>
            <div><span>Осталось</span><strong>{usage.remainingTokens}</strong></div>
            <div><span>Сброс лимита</span><strong>{formatDateTime(usage.resetsAt)}</strong></div>
          </div>
        </div>

        <div className="panel">
          <h3>Стиль AI-комментариев</h3>
          <p>Системный prompt скрыт. Администратор может менять только стиль общения модели.</p>
          {user.role === 'ADMIN' ? (
            <form className="stack-sm" onSubmit={saveSettings}>
              <label>
                Название стиля
                <input value={form.styleName} onChange={(event) => setForm((current) => ({ ...current, styleName: event.target.value }))} />
              </label>
              <label>
                Инструкция по стилю
                <textarea rows="4" value={form.styleInstruction} onChange={(event) => setForm((current) => ({ ...current, styleInstruction: event.target.value }))} />
              </label>
              <button className="primary-button" type="submit">Сохранить стиль</button>
            </form>
          ) : (
            <div className="usage-list">
              <div><span>Текущий стиль</span><strong>{settings.styleName}</strong></div>
              <div><span>Инструкция</span><strong>{settings.styleInstruction}</strong></div>
            </div>
          )}
        </div>
      </section>
    </div>
  );
}
