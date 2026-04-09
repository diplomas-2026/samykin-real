import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { aiApi, employeesApi, payoutsApi } from '../api/services';
import { ErrorBanner } from '../components/ui/ErrorBanner';
import { Loader } from '../components/ui/Loader';
import { PageHeader } from '../components/ui/PageHeader';
import { getApiErrorMessage } from '../utils/api';

export function PayoutFormPage() {
  const navigate = useNavigate();
  const [employees, setEmployees] = useState(null);
  const [usage, setUsage] = useState(null);
  const [error, setError] = useState('');
  const [aiError, setAiError] = useState('');
  const [commentLoading, setCommentLoading] = useState(false);
  const [submitLoading, setSubmitLoading] = useState(false);
  const [form, setForm] = useState({
    employeeId: '',
    payoutType: 'Премия',
    amount: '',
    payoutDate: '',
    basis: '',
    comment: '',
    payoutNote: '',
  });

  useEffect(() => {
    Promise.all([employeesApi.getAll(), aiApi.getUsage()])
      .then(([employeesData, usageData]) => {
        setEmployees(employeesData);
        setUsage(usageData);
      })
      .catch((requestError) => {
        setError(getApiErrorMessage(requestError, 'Не удалось подготовить форму выплаты'));
      });
  }, []);

  if (!employees || !usage) {
    if (error) {
      return (
        <div className="stack">
          <PageHeader
            eyebrow="Новая выплата"
            title="Оформление денежной выплаты"
            description="Форма создания выплаты с генерацией комментария через GigaChat."
          />
          <ErrorBanner message={error} />
        </div>
      );
    }
    return <Loader text="Подготовка формы выплаты..." />;
  }

  const canGenerateAiComment = Boolean(form.employeeId && form.basis.trim() && Number(form.amount) > 0);

  const generateComment = async () => {
    const employee = employees.find((item) => String(item.id) === String(form.employeeId));
    if (!employee) return;
    setCommentLoading(true);
    setAiError('');
    try {
      const result = await payoutsApi.generateComment({
        employeeName: employee.fullName,
        payoutType: form.payoutType,
        amount: Number(form.amount),
        basis: form.basis,
        existingComment: form.comment,
      });
      setForm((current) => ({ ...current, comment: result.comment }));
      setUsage((current) => ({ ...current, usedTokens: current.usedTokens + result.usedTokens, remainingTokens: result.remainingTokens }));
    } catch (requestError) {
      setAiError(getApiErrorMessage(requestError, 'Не удалось сгенерировать AI-комментарий'));
    } finally {
      setCommentLoading(false);
    }
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSubmitLoading(true);
    setError('');
    setAiError('');
    try {
      await payoutsApi.create({
        ...form,
        employeeId: Number(form.employeeId),
        amount: Number(form.amount),
      });
      navigate('/payouts');
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, 'Не удалось сохранить выплату'));
    } finally {
      setSubmitLoading(false);
    }
  };

  return (
    <div className="stack">
      <PageHeader
        eyebrow="Новая выплата"
        title="Оформление денежной выплаты"
        description="Форма создания выплаты с генерацией комментария через GigaChat."
      />
      <ErrorBanner message={error} />
      <div className="dashboard-grid">
        <form className="panel form-grid" onSubmit={handleSubmit}>
          <label>Сотрудник
            <select value={form.employeeId} onChange={(event) => setForm((current) => ({ ...current, employeeId: event.target.value }))}>
              <option value="">Выберите сотрудника</option>
              {employees.map((employee) => (
                <option key={employee.id} value={employee.id}>{employee.fullName}</option>
              ))}
            </select>
          </label>
          <label>Тип выплаты<input value={form.payoutType} onChange={(event) => setForm((current) => ({ ...current, payoutType: event.target.value }))} /></label>
          <label>Сумма<input type="number" value={form.amount} onChange={(event) => setForm((current) => ({ ...current, amount: event.target.value }))} /></label>
          <label>Дата выплаты<input type="date" value={form.payoutDate} onChange={(event) => setForm((current) => ({ ...current, payoutDate: event.target.value }))} /></label>
          <label className="full-width">Основание<input value={form.basis} onChange={(event) => setForm((current) => ({ ...current, basis: event.target.value }))} /></label>
          <label className="full-width">Комментарий
            <textarea rows="4" value={form.comment} onChange={(event) => setForm((current) => ({ ...current, comment: event.target.value }))} />
          </label>
          <div className="full-width ai-feedback">
            <div className="ai-feedback__hint">
              AI-генерация доступна после выбора сотрудника, ввода суммы и основания выплаты.
            </div>
            {commentLoading ? <div className="ai-feedback__status">AI подбирает формулировку комментария...</div> : null}
            <ErrorBanner message={aiError} />
          </div>
          <label className="full-width">Примечание
            <textarea rows="3" value={form.payoutNote} onChange={(event) => setForm((current) => ({ ...current, payoutNote: event.target.value }))} />
          </label>
          <div className="button-row full-width">
            <button className="ghost-button" type="button" disabled={commentLoading || !canGenerateAiComment} onClick={generateComment}>
              {commentLoading ? 'Генерируем...' : 'Сгенерировать комментарий AI'}
            </button>
            <button className="primary-button" type="submit" disabled={submitLoading}>
              {submitLoading ? 'Сохраняем...' : 'Сохранить выплату'}
            </button>
          </div>
        </form>
        <div className="panel">
          <h3>Лимиты AI</h3>
          <div className="usage-list">
            <div><span>Потрачено</span><strong>{usage.usedTokens}</strong></div>
            <div><span>Осталось</span><strong>{usage.remainingTokens}</strong></div>
            <div><span>Сброс</span><strong>{usage.resetsAt}</strong></div>
          </div>
        </div>
      </div>
    </div>
  );
}
